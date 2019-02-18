package danbroid.touchprompt

import danbroid.touchprompt.mtt.MTTImpl

fun install() = installCreator {
  MTTImpl(it)
}