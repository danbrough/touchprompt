package danbroid.touchprompt.demo

import android.os.Bundle
import android.view.View
import danbroid.touchprompt.touchPrompt

class SecondFragment : BaseFragment("Second Fragment") {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    tests.setOnTouchListener { v, event ->
      touchPrompt {
        primaryText = "Clicked at ${event.x},${event.y}"
        targetPos = Pair(event.x, event.y)
      }
      true
    }

    touchPrompt(SingleShot.FRAGMENT2_TOUCH_HERE) {
      target = tests
      primaryText = "Touch anywhere here to display a prompt"
    }

    addTest("Second Fragment") {

    }


  }
}

private val log = org.slf4j.LoggerFactory.getLogger(SecondFragment::class.java)