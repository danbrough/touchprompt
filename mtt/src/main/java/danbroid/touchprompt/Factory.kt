package danbroid.touchprompt

import danbroid.touchprompt.mtt.MTTImpl

class Factory : TouchPromptFactory {
  override fun create(prompt: TouchPrompt) = MTTImpl(prompt)
}
