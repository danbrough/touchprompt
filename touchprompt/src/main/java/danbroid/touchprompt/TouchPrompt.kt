package danbroid.touchprompt

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

typealias TouchPromptInit = TouchPrompt.(MaterialTapTargetPrompt.Builder) -> Unit


fun Activity.touchPrompt(singleShotID: Any? = null, init: TouchPromptInit) =
  showTouchPrompt(this, singleShotID, activity = this, init = init)

fun Fragment.touchPrompt(singleShotID: Any? = null, init: TouchPromptInit) =
  showTouchPrompt(context!!, singleShotID, fragment = this, init = init)

var themeResourceID: Int = 0

fun showTouchPrompt(
  context: Context,
  singleShotID: Any? = null,
  activity: Activity? = null,
  fragment: Fragment? = null,
  init: TouchPromptInit
): TouchPrompt? {
  if (TouchPrompt.singleShotDone(context, singleShotID?.toString())) return null

  val builder = if (activity != null) MaterialTapTargetPrompt.Builder(activity, themeResourceID)
  else MaterialTapTargetPrompt.Builder(fragment!!)

  val prompt = TouchPrompt(
    singleShotID?.toString(),
    context,
    builder,
    activity,
    fragment,
    init
  )

  val view: View = activity?.findViewById(android.R.id.content) ?: fragment!!.view!!
  view.post {
    prompt.show()
  }
  return prompt
}


class TouchPrompt(
  var singleShotID: String?,
  var context: Context,
  var builder: MaterialTapTargetPrompt.Builder,
  var activity: Activity? = null,
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
  }

  var primaryText: CharSequence? = null
    set(value) {
      builder.setPrimaryText(value)
    }

  fun setPrimaryText(@StringRes textID: Int) = builder.setPrimaryText(textID)

  var primaryTextID: Int? = null
    set(value) {
      setPrimaryText(value!!)
      field = value
    }

  var secondaryText: CharSequence? = null
    set(value) {
      builder.setSecondaryText(value)
    }

  var secondaryTextID: Int? = null
    set(value) {
      setSecondaryText(value!!)
      field = value
    }

  fun setSecondaryText(@StringRes textID: Int) = builder.setSecondaryText(textID)

  var target: View? = null
    set(value) {
      builder.setTarget(value)
    }

  fun setTargetPosition(x: Float, y: Float) = builder.setTarget(x, y)

  var targetID: Int? = null
    set(value) {
      if (value != null)
        builder.setTarget(value)
    }

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

  }

  var prompt: MaterialTapTargetPrompt? = null

  fun show(): TouchPrompt {
    init(builder)
    builder.setPromptStateChangeListener(this)

    if (initialDelay != null) {
      (activity?.findViewById(android.R.id.content) ?: fragment!!.view)?.run {
        postDelayed(::nativeShow, initialDelay!!)
      }
    } else {
      nativeShow()
    }

    return this
  }

  private fun nativeShow() {

    onBeforeShow?.invoke()

    prompt = showFor?.run {
      builder.showFor(this)
    } ?: builder.show()

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