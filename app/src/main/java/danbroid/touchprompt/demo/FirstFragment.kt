package danbroid.touchprompt.demo

import android.os.Bundle
import android.view.View
import danbroid.touchprompt.fragmentTouchPrompt
import danbroid.touchprompt.touchPrompt


/**
 * A placeholder fragment containing a simple view.
 */
class FirstFragment : BaseFragment("First Fragment") {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    addTest("Test 1") {
      touchPrompt {
        target = it
        primaryText = "Its a button"
      }
    }



    addTest("Fragment Prompt") {
      fragmentTouchPrompt {
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
    fragmentTouchPrompt {
      initialDelay = 2000
      primaryTextID = R.string.msg_delayed_prompt
    }
  }
}


private val log = org.slf4j.LoggerFactory.getLogger(FirstFragment::class.java)