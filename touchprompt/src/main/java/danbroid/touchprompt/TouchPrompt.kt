package danbroid.touchprompt

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.lang.ref.WeakReference

fun Fragment.touchPrompt(
  singleShotID: Any? = null,
  mode: TouchPromptMode = TouchPromptMode.ACTIVITY,
  init: (TouchPrompt.() -> Unit)
): TouchPrompt? {
  if (TouchPrompt.hasSingleShotRun(singleShotID, this.context!!)) return null

  return TouchPrompt(
    mode,
    fragment = this,
    singleShotID = singleShotID?.toString(),
    lifecycle = this.lifecycle,
    init = init
  ).postInit()
}

fun ComponentActivity.touchPrompt(
  singleShotID: Any? = null,
  mode: TouchPromptMode = TouchPromptMode.ACTIVITY,
  init: (TouchPrompt.() -> Unit)
): TouchPrompt? {

  if (TouchPrompt.hasSingleShotRun(singleShotID, this)) return null

  return TouchPrompt(
    mode,
    activity = this,
    singleShotID = singleShotID?.toString(),
    lifecycle = this.lifecycle,
    init = init
  ).postInit()
}

enum class TouchPromptMode {
  FRAGMENT, ACTIVITY
}

class TouchPrompt(

  var mode: TouchPromptMode = TouchPromptMode.ACTIVITY,
  var activity: ComponentActivity? = null,
  var fragment: Fragment? = null,
  var singleShotID: String? = null,
  var lifecycle: Lifecycle,
  private var init: (TouchPrompt.() -> Unit)

) : LifecycleObserver {

  var primaryText: String? = null
  @StringRes
  var primaryTextID: Int? = null

  var secondaryText: String? = null
  @StringRes
  var secondaryTextID: Int? = null

  var initialDelay: Long? = null

  fun setShortInitialDelay() {
    initialDelay = 500L
  }

  var targetPos: Pair<Float, Float>? = null

  var target: View? = null

  @IdRes
  var targetID: Int? = null

  var targetFinder: ((TouchPrompt) -> View?)? = null

  /**
   * Invoked immediately before the prompt is to appear.
   * Return false to dismiss the showing
   */
  var onShow: (() -> Boolean)? = null

  /**
   * Only show this prompt if no other serial prompts are showing
   */
  var serial: Boolean = true

  private var nextPrompt: TouchPrompt? = null

  val context: Context
    get() = fragment?.context ?: activity!!

  var impl: TouchPromptImpl? = null

  var native: ((TouchPromptImpl) -> Unit)? = null

  private var cancelled = false

  companion object {
    var PREFS_FILE = "touch_prompt"

    fun getPrefs(context: Context) = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    fun clearPrefs(context: Context) = getPrefs(context).edit().clear().apply()

    fun hasSingleShotRun(singleShotID: Any?, context: Context) =
      singleShotID != null && getPrefs(context).contains(singleShotID.toString())

    fun markShown(context: Context, singleShotID: String) =
      getPrefs(context).run {
        if (!contains(singleShotID)) edit().putString(singleShotID, EMPTY_STRING).apply()
      }

    private var currentPrompt: WeakReference<TouchPrompt>? = null

    private fun showNext(context: Context) {
      log.trace("showNext()")
      currentPrompt?.get()?.also {
        log.trace("found current prompt")
        currentPrompt = null
        it.nextPrompt?.run {
          if (!hasSingleShotRun(singleShotID, context)) postInit()
          else
            nextPrompt?.postInit()
        }
      }
    }

  }

  internal fun postInit(): TouchPrompt {
    init.invoke(this)

    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
      startShow()
    } else {
      lifecycle.addObserver(this)
    }

    return this
  }


  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  fun startShow() {
    log.trace("startShow() currentPrompt:$currentPrompt")

    if (serial) {
      currentPrompt?.get()?.also {
        log.info("existing prompt found")
        var p = it
        while (p.nextPrompt != null) {
          p = p.nextPrompt!!
        }
        p.nextPrompt = this@TouchPrompt
        return
      } ?: run {
        log.debug("setting current prompt")
        currentPrompt = WeakReference(this@TouchPrompt)
      }
    }

    initialDelay?.also { delay ->
      val view = fragment?.view ?: activity!!.findViewById(android.R.id.content)
      view.postDelayed(::show, delay)
    } ?: show()
  }

  private fun show() {
    val state = lifecycle.currentState
    log.trace("show() cancelled:$cancelled state:$state")

    if (cancelled) return showNext()

    if (!state.isAtLeast(Lifecycle.State.RESUMED)) return showNext()

    if (onShow?.invoke() == false) return showNext()

    impl = createImpl(this)

    impl?.show()
  }

  private fun showNext() {
    TouchPrompt.showNext(context)
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun onStop() {
    currentPrompt = null
    if (!cancelled)
      cancel()
  }

  fun cancel(all: Boolean = false) {
    cancelled = true
    impl?.dismiss()?.also {
      impl = null
    }
    if (!all)
      showNext()
  }

  fun markShown() {

    log.trace("markShown()")
    singleShotID?.run {
      TouchPrompt.markShown(context, this)
    }

    showNext()
  }
}


private const val EMPTY_STRING = ""

abstract class TouchPromptImpl(val prompt: TouchPrompt) {

  abstract fun show()
  abstract fun dismiss()
}


private val log = org.slf4j.LoggerFactory.getLogger(TouchPrompt::class.java)

fun overflowButton(activity: Activity, @IdRes toolbarID: Int): View? =
  activity.findViewById<Toolbar>(toolbarID)?.let { it.getChildAt(it.childCount - 1) }?.let {
    if (it is ActionMenuView) it.getChildAt(it.childCount - 1) else null
  }

fun findViewRecursive(view: View?, selector: (View) -> Boolean): View? {
  if (view == null) return null
  if (view.visibility != View.VISIBLE) return null
  if (selector.invoke(view)) return view
  if (view is ViewGroup)
    for (n in 0 until view.childCount) view.getChildAt(n).let { child ->
      findViewRecursive(child, selector)?.also {
        return it
      }
    }
  return null
}