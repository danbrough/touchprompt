package danbroid.touchprompt.demo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import danbroid.touchprompt.touchPrompt
import java.util.concurrent.TimeUnit


/**
 * A placeholder fragment containing a simple view.
 */
class FirstFragment : BaseFragment("First Fragment") {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    addTest("Test 1") {
      Toast.makeText(context, "Test1", Toast.LENGTH_SHORT).show()
    }.run {
      touchPrompt {
        target = this@run
        primaryText = "TEst1"
        initialDelay = TimeUnit.SECONDS.toMillis(2)
      }
    }

    addTest("Fragment Prompt") {
      touchPrompt {
        target = it
        primaryText = "Fragment Touch Prompt"
      }
    }

    addTest("Chain Test") {

      touchPrompt(SingleShot.TEST1) {
        target = it
        primaryText = "First Prompt"
      }

      touchPrompt {
        target = it
        primaryText = "Second Prompt"
      }

    }

    addTest("Second Fragment") {

      touchPrompt {
        target = it
        primaryText = "Going to second fragment"
      }

      activity!!.supportFragmentManager.beginTransaction().replace(R.id.fragment, SecondFragment())
        .addToBackStack(null)
        .commit()
    }
  }

  override fun onResume() {
    super.onResume()
    touchPrompt {
      initialDelay = 2000
      primaryTextID = R.string.msg_delayed_prompt
    }
  }
}


private val log = org.slf4j.LoggerFactory.getLogger(FirstFragment::class.java)