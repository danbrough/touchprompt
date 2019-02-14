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
import danbroid.touchprompt.TouchPrompt.Companion.getPrefs
import danbroid.touchprompt.material.MaterialTouchPromptImpl
import java.lang.ref.WeakReference

const val PREFS_FILE = "touch_prompt"


class TouchPrompt(
  internal var singleShotID: Any? = null,
  internal var activity: ComponentActivity? = null,
  internal var fragment: Fragment? = null,
  internal var serial: Boolean = true,
  internal var init: (TouchPrompt.() -> Unit)? = null
) {

  internal val context: Context = fragment?.context ?: activity!!
  internal var state: TouchPromptState = TouchPromptState.INITIAL

  internal val lifecycle: Lifecycle
    get() = activity?.lifecycle ?: fragment!!.lifecycle

  var primaryText: CharSequence? = null

  @StringRes
  var primaryTextID: Int? = null

  var secondaryText: CharSequence? = null

  @StringRes
  var secondaryTextID: Int? = null

  @IdRes
  var targetID: Int? = null

  var target: View? = null

  var targetLocation: Pair<Float, Float>? = null

  var targetProvider: (() -> View?)? = null

  var initialDelay: Long? = null

  internal val impl = MaterialTouchPromptImpl(this)

  companion object {
    internal fun getPrefs(context: Context) =
      context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    fun clearPrefs(context: Context) = getPrefs(context).edit().clear().apply()
  }

  fun show(): TouchPrompt {
    impl.show()
    return this
  }

  fun cancel() = impl.cancel()

  fun setShortInitialDelay() {
    initialDelay = 300L
  }


}

internal enum class TouchPromptState {
  INITIAL, SHOWING, ERROR, DONE
}


abstract class TouchPromptImpl<T>(val prompt: TouchPrompt) {

  companion object {
    var currentPrompt: WeakReference<TouchPromptImpl<*>>? = null
  }


  private var delayedShow: (() -> Unit)? = null

  var nativePrompt: T? = null

  private var next: TouchPromptImpl<*>? = null

  fun show() {
    log.debug("show()")

    if (prompt.serial)
      currentPrompt?.get()?.let { current ->
        //append this to the queue
        log.warn("adding to the prompt queue")
        var p: TouchPromptImpl<*> = current
        while (p.next != null) p = p.next as TouchPromptImpl<*>
        p.next = this
        return //show() will get called again when the current prompt is done
      }


    prompt.singleShotID?.run {
      if (TouchPrompt.getPrefs(prompt.context).contains(this.toString())) {
        return onDone()
      }
    }

    // log.trace("past dont show")
    if (prompt.serial) currentPrompt = WeakReference(this)

    //initialise the prompt
    try {
      prompt.init?.invoke(prompt)
    } catch (err: Exception) {
      return onError(err.message ?: "Error initialising prompt")
    }

    prompt.initialDelay?.also { delay ->

      delayedShow = {
        if (prompt.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
          showNow()
        else
          onError("Invalid lifecycle state: ${prompt.lifecycle.currentState}")
      }

      val view: View = (prompt.target ?: prompt.fragment?.view ?: prompt.activity!!.findViewById(
        android.R.id.content
      ))

      view.postDelayed(delayedShow, delay)

    } ?: showNow()
  }


  protected abstract fun showNow()

  open fun cancel() {
    delayedShow?.run {
      (prompt.activity?.findViewById(android.R.id.content) ?: prompt.fragment?.view)
        ?.removeCallbacks(this)
      delayedShow = null
    }
    onDone()
  }


  protected open fun onError(msg: String) {
    log.error(msg)
    prompt.state = TouchPromptState.ERROR
    onDone()
  }

  protected open fun resolveTarget(): View? {
    //if target return that else if targetID then resolve that else invoke the targetProvider
    // else return null
    return prompt.target
      ?: prompt.targetID?.let { targetID ->
        prompt.run {
          fragment?.view?.findViewById(targetID) ?: activity?.findViewById<View>(targetID)
        }
      }

      //if we have a targetProvider then invoke that
      ?: prompt.targetProvider?.invoke()

      //if we are bound to a fragment then use its view
      ?: prompt.fragment?.view
  }


  open fun onDone() {
    log.debug("onDone()")

    if (prompt.state == TouchPromptState.SHOWING) {
      prompt.singleShotID?.run {
        //dont show this prompt again
        getPrefs(prompt.context).edit().putString(toString(), "").apply()
      }
      prompt.state = TouchPromptState.DONE
    }

    currentPrompt = null


    next?.show()
  }

}


private val log = org.slf4j.LoggerFactory.getLogger(TouchPrompt::class.java)


fun ComponentActivity.touchPrompt(
  singleShotID: Any? = null,
  serial: Boolean = true,
  init: (TouchPrompt.() -> Unit)? = null
) =
  TouchPrompt(singleShotID, this, null, serial, init).show()

fun Fragment.touchPrompt(
  singleShotID: Any? = null,
  serial: Boolean = true,
  init: (TouchPrompt.() -> Unit)? = null
) =
  TouchPrompt(singleShotID, activity!!, null, serial, init).show()

fun Fragment.fragmentTouchPrompt(
  singleShotID: Any? = null,
  serial: Boolean = true,
  init: (TouchPrompt.() -> Unit)? = null
) =
  TouchPrompt(singleShotID, null, this, serial, init).show()


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