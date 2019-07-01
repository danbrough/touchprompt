package danbroid.touchprompt.demo

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import danbroid.touchprompt.fragmentTouchPrompt
import danbroid.touchprompt.touchPrompt

class SecondFragment : BaseFragment("Second Fragment") {
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    tests.setOnTouchListener { _, event ->
      fragmentTouchPrompt {
        primaryText = "Fragment prompt at %.2f,%.2f".format(event.x, event.y)
        setTargetPosition(event.x, event.y)
      }
      true
    }

    TextView(context).run {
      layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120)
      text = "The Second Fragment"
      tests.addView(this)
    }

    touchPrompt(SingleShot.FRAGMENT2_TOUCH_HERE) {
      target = tests
      primaryText = "Touch anywhere here to display a prompt"
    }

  }
}

private val log = org.slf4j.LoggerFactory.getLogger(SecondFragment::class.java)