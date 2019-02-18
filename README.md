touchprompt
============

A kotlin facade for touchprompts.

Currently implemented by [MaterialTapTargetPrompt](https://github.com/sjwall/MaterialTapTargetPrompt)


Add:

`maven { url 'https://jitpack.io' }`

to your maven repositories.


Include: 

`implementation "com.github.danbrough.touchprompt:mtt:v0.004"`

in your build.gradle.

Create the prompt when your android component is initialised:


```kotline
 override fun onCreate(savedInstanceState: Bundle?) {
  touchPrompt(HelpCodes.SEARCH_BUTTON) {
        setShortInitialDelay()
        primaryTextID = R.string.lbl_search_button
        secondaryTextID = R.string.msg_search_button
        targetID = R.id.action_search
  }
 }
 ```
 
 


