package danbroid.touchprompt.mtt

import android.view.View
import danbroid.touchprompt.TouchPrompt
import danbroid.touchprompt.TouchPromptImpl
import danbroid.touchprompt.TouchPromptMode
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt


class MTTImpl(prompt: TouchPrompt) : TouchPromptImpl(prompt),
  MaterialTapTargetPrompt.PromptStateChangeListener {


  var nativePrompt: MaterialTapTargetPrompt? = null
  var builder: MaterialTapTargetPrompt.Builder

  init {

    log.trace("init()")

    builder = when (prompt.mode) {
      TouchPromptMode.FRAGMENT -> MaterialTapTargetPrompt.Builder(prompt.fragment!!)
      TouchPromptMode.ACTIVITY -> MaterialTapTargetPrompt.Builder(
        prompt.activity ?: prompt.fragment?.activity!!
      )
    }


    builder.setAutoFinish(true)

    prompt.primaryText?.also {
      builder.primaryText = prompt.primaryText
    } ?: prompt.primaryTextID?.also {
      builder.setPrimaryText(it)
    }

    prompt.secondaryText?.run {
      builder.secondaryText = this
    } ?: prompt.secondaryTextID?.also {
      builder.setSecondaryText(it)
    }

    prompt.targetFinder?.run {
      invoke(prompt)?.run {
        log.info("set target!!!!!!!!!!!!!! $this using targetFinder")
        builder.setTarget(this)
      }
    } ?: prompt.targetPos?.run {

      builder.setTarget(first, second)

    } ?: prompt.target?.run {

      builder.setTarget(this)

    } ?: prompt.targetID?.let { id ->

      val view: View? = prompt.fragment?.view?.findViewById(id)
        ?: prompt.activity?.findViewById(id)
        ?: prompt.fragment?.activity?.findViewById(id)

      log.trace("found targetID view: $view")
      view?.run {
        builder.setTarget(this)
      }
    }

    builder.setPromptStateChangeListener(this)

    prompt.native?.invoke(this)

    nativePrompt = builder.create()
    log.trace("nativePrompt: $nativePrompt")
  }

  override fun dismiss() {
    nativePrompt?.dismiss()
  }

  override fun show() {
    log.debug("show() $nativePrompt")
    nativePrompt?.show()
  }

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
      }
      MaterialTapTargetPrompt.STATE_FINISHED -> {
        log.trace("STATE_FINISHED")
        prompt.onShown?.invoke()
        prompt.markShown()
      }
      MaterialTapTargetPrompt.STATE_DISMISSING -> {
        log.trace("STATE_DISMISSING")
      }
      MaterialTapTargetPrompt.STATE_DISMISSED -> {
        log.trace("STATE_DISMISSED")
        prompt.onShown?.invoke()
        prompt.markShown()

      }
    }
  }

}


private val log = org.slf4j.LoggerFactory.getLogger(MTTImpl::class.java)

