package danbroid.touchprompt

import danbroid.touchprompt.mtt.MTTImpl

fun createImpl(prompt: TouchPrompt): TouchPromptImpl =
  MTTImpl(prompt)
