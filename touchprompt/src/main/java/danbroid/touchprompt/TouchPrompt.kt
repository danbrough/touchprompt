package danbroid.touchprompt

import android.annotation.SuppressLint
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
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence

typealias TouchPromptInit = TouchPrompt.(MaterialTapTargetPrompt.Builder) -> Unit


fun ComponentActivity.touchPrompt(
  singleShotID: Any? = null,
  serial: Boolean = true,
  init: TouchPromptInit
) =
  showTouchPrompt(this, singleShotID, activity = this, init = init)

fun Fragment.fragmentTouchPrompt(
  singleShotID: Any? = null,
  serial: Boolean = true,
  init: TouchPromptInit
) =
  showTouchPrompt(context!!, singleShotID, fragment = this, serial = serial, init = init)


fun Fragment.touchPrompt(singleShotID: Any? = null, serial: Boolean = true, init: TouchPromptInit) =
  showTouchPrompt(
    context!!,
    singleShotID,
    activity = this.activity,
    fragment = this,
    serial = serial,
    init = init
  )

var themeResourceID: Int = 0


fun promptSequence(init: (MaterialTapTargetSequence) -> Unit): MaterialTapTargetSequence {
  val sequence = MaterialTapTargetSequence()
  init(sequence)
  sequence.show()
  return sequence
}

fun showTouchPrompt(
  context: Context,
  singleShotID: Any? = null,
  activity: ComponentActivity? = null,
  fragment: Fragment? = null,
  serial: Boolean = true,
  sequence: MaterialTapTargetSequence? = null,
  init: TouchPromptInit
): TouchPrompt? {
  if (TouchPrompt.singleShotDone(context, singleShotID?.toString())) return null

  try {
    val builder = if (activity != null) MaterialTapTargetPrompt.Builder(activity, themeResourceID)
    else MaterialTapTargetPrompt.Builder(fragment!!)

    val prompt = TouchPrompt(
      singleShotID?.toString(),
      context,
      fragment?.lifecycle ?: activity!!.lifecycle,
      serial,
      sequence,
      builder,
      activity,
      fragment,
      init
    )

    prompt.show()
    return prompt

  } catch (err: Exception) {
    log.error(err.message, err)
  }
  return null
}


class TouchPrompt(
  var singleShotID: String?,
  var context: Context,
  var lifecycle: Lifecycle,
  var serial: Boolean = true,
  var sequence: MaterialTapTargetSequence? = null,
  var builder: MaterialTapTargetPrompt.Builder,
  var activity: ComponentActivity? = null,
  var fragment: Fragment? = null,

  var init: (TouchPrompt.(MaterialTapTargetPrompt.Builder) -> Unit)
) : MaterialTapTargetPrompt.PromptStateChangeListener {

  override fun onPromptStateChanged(nativePrompt: MaterialTapTargetPrompt, state: Int) {
    when (state) {
      MaterialTapTargetPrompt.STATE_REVEALING -> {
        log.trace("STATE_REVEALING")
      }
      MaterialTapTargetPrompt.STATE_REVEALED -> {
        log.trace("STATE_REVEALED")
      }
      MaterialTapTargetPrompt.STATE_FOCAL_PRESSED -> {
        log.trace("STATE_FOCAL_PRESSED")
        onFocalPressed?.invoke()
      }
      MaterialTapTargetPrompt.STATE_FINISHED -> {
        log.trace("STATE_FINISHED")
        markShown()
      }
      MaterialTapTargetPrompt.STATE_DISMISSING -> {
        log.trace("STATE_DISMISSING")
      }
      MaterialTapTargetPrompt.STATE_DISMISSED -> {
        log.trace("STATE_DISMISSED")
        markShown()
        onDismissed?.invoke()
      }
    }
  }

  private fun markShown() {
    markSingleShotDone(context, singleShotID)
    showNext()
  }

  var primaryText: CharSequence? = null
    set(value) {
      builder.setPrimaryText(value)
    }

  fun setPrimaryText(@StringRes textID: Int) = builder.setPrimaryText(textID)

  var primaryTextID: Int? = null
    set(value) {
      builder.setPrimaryText(value!!)
      field = value
    }

  var secondaryText: CharSequence? = null
    set(value) {
      builder.setSecondaryText(value)
    }

  var secondaryTextID: Int? = null
    set(value) {
      builder.setSecondaryText(value!!)
      field = value
    }

  fun setSecondaryText(@StringRes textID: Int) = builder.setSecondaryText(textID)

  var target: View? = null
    set(value) {
      builder.setTarget(value)
    }

  fun setTargetPosition(x: Float, y: Float) = builder.setTarget(x, y)

  var targetID: Int? = null


  var initialDelay: Long? = null

  var showFor: Long? = null

  var onBeforeShow: (() -> Unit)? = null

  var onDismissed: (() -> Unit)? = null

  var onFocalPressed: (() -> Unit)? = null

  companion object {
    const val PREFS_FILE = "touch_prompt"
    private const val EMPTY_STRING = ""

    fun getPrefs(context: Context) = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    fun singleShotDone(context: Context, shotID: String?) =
      shotID != null && getPrefs(context).contains(shotID)

    fun markSingleShotDone(context: Context, shotID: String?) =
      shotID?.run {
        getPrefs(context).edit().putString(this, EMPTY_STRING).apply()
      }

    @SuppressLint("ApplySharedPref")
    fun clearPrefs(context: Context) {
      getPrefs(context).edit().clear().commit()
    }

    @SuppressLint("StaticFieldLeak")
    var currentPrompt: TouchPrompt? = null
  }

  var prompt: MaterialTapTargetPrompt? = null

  fun show(): TouchPrompt {
    init(builder)
    builder.setPromptStateChangeListener(this)

    if (serial) {
      currentPrompt?.also {
        //append this prompt to the chain and return
        var p = it
        while (p.nextPrompt != null) p = p.nextPrompt!!
        p.nextPrompt = this@TouchPrompt
        return this@TouchPrompt
      }

      //otherwise set this as the current prompt
      currentPrompt = this
    }

    if (sequence == null) {
      (activity?.findViewById(android.R.id.content) ?: fragment!!.view)?.also { v ->
        initialDelay?.also { delay ->
          v.postDelayed(::nativeShow, delay)
        } ?: v.post(::nativeShow)
      }
    } else {
      nativeShow()
    }

    return this
  }

  private fun nativeShow() {
    log.trace("nativeShow() ${builder.primaryText}")

    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) ||
      fragment?.userVisibleHint == false ||
      fragment?.isVisible == false
    ) return showNext()


    targetID?.also {
      builder.setTarget(it)
    }

    onBeforeShow?.invoke()

    prompt = builder.create()

    if (prompt == null) {
      log.debug("failed to create prompt")
      return showNext()
    }

    prompt?.run {
      if (sequence != null) {
        if (showFor != null)
          sequence!!.addPrompt(this, showFor!!)
        else
          sequence!!.addPrompt(this)

      } else {
        if (showFor != null)
          showFor(showFor!!)
        else
          show()
      }
    }

  }

  private var nextPrompt: TouchPrompt? = null

  private fun showNext() {
    if (serial) {
      currentPrompt = null
      nextPrompt?.run {
        show()
      }
    }
  }

  fun dismiss() {
    prompt?.dismiss()
  }

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