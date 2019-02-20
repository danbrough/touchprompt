touchprompt
============

A kotlin facade for touchprompts.

[![Release](https://jitpack.io/v/danbrough/touchprompt.svg)](https://jitpack.io/#danbrough/touchprompt)


Currently implemented by [MaterialTapTargetPrompt](https://github.com/sjwall/MaterialTapTargetPrompt)


Add:

`maven { url 'https://jitpack.io' }`

to your maven repositories.


Include: 

`implementation "com.github.danbrough.touchprompt:mtt:v0.005"`

in your build.gradle.

Declare a touch prompt:


```kotlin
 override fun onCreate(savedInstanceState: Bundle?) {
      touchPrompt(HelpCodes.SEARCH_BUTTON) {
            setShortInitialDelay()
            primaryTextID = R.string.lbl_search_button
            secondaryTextID = R.string.msg_search_button
            targetID = R.id.action_search
      }
 }
 ```
 
 


