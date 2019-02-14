package danbroid.touchprompt.material

import danbroid.touchprompt.TouchPrompt
import danbroid.touchprompt.TouchPromptImpl
import danbroid.touchprompt.TouchPromptState
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class MaterialTouchPromptImpl(prompt: TouchPrompt) :
  TouchPromptImpl<MaterialTapTargetPrompt.Builder>(prompt) {

  lateinit var builder: MaterialTapTargetPrompt.Builder

  override fun showNow() {
    log.debug("showNow()")

    if (prompt.fragment != null)
      builder = MaterialTapTargetPrompt.Builder(prompt.fragment!!)
    else
      builder = MaterialTapTargetPrompt.Builder(prompt.activity!!)

    prompt.targetLocation?.run { builder.setTarget(first, second) }
      ?: resolveTarget()?.run { builder.setTarget(this) }
      ?: return onError("Failed to resolve target")

    if (prompt.primaryText != null)
      builder.setPrimaryText(prompt.primaryText)
    else if (prompt.primaryTextID != null)
      builder.setPrimaryText(prompt.primaryTextID!!)

    if (prompt.secondaryText != null)
      builder.secondaryText = prompt.secondaryText
    else if (prompt.secondaryTextID != null)
      builder.setSecondaryText(prompt.secondaryTextID!!)

    builder.setPromptStateChangeListener { _, state ->
      when (state) {
        MaterialTapTargetPrompt.STATE_NOT_SHOWN -> {
          onDone()
          "STATE_NOT_SHOWN"
        }
        MaterialTapTargetPrompt.STATE_REVEALING -> "STATE_REVEALING"
        MaterialTapTargetPrompt.STATE_REVEALED -> {
          prompt.state = TouchPromptState.SHOWING
          "STATE_REVEALED"
        }
        MaterialTapTargetPrompt.STATE_FOCAL_PRESSED -> {
          "STATE_FOCAL_PRESSED"
        }
        MaterialTapTargetPrompt.STATE_FINISHED -> {
          "STATE_NOT_SHOWN"
        }
        MaterialTapTargetPrompt.STATE_DISMISSING -> "STATE_DISMISSING"
        MaterialTapTargetPrompt.STATE_DISMISSED -> {
          onDone()
          "STATE_DISMISSED"
        }
        MaterialTapTargetPrompt.STATE_FINISHING -> {
          onDone()
          "STATE_FINISHING"
        }
        MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED -> "STATE_NON_FOCAL_PRESSED"
        MaterialTapTargetPrompt.STATE_SHOW_FOR_TIMEOUT -> "STATE_SHOW_FOR_TIMEOUT"
        MaterialTapTargetPrompt.STATE_BACK_BUTTON_PRESSED -> "STATE_BACK_BUTTON_PRESSED"

        else -> {
          log.error("ERROR UNKNOWN STATE")
          "ERROR UNKNOWN STATE"
        }
      }.run {
        log.debug(this)
      }
    }

    builder.create()?.show()
  }

}


private val log = org.slf4j.LoggerFactory.getLogger(MaterialTouchPromptImpl::class.java)