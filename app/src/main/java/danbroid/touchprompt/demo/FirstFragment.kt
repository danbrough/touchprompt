package danbroid.touchprompt.demo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import danbroid.touchprompt.TouchPrompt
import danbroid.touchprompt.TouchPromptMode
import danbroid.touchprompt.touchPrompt
import java.util.concurrent.TimeUnit


/**
 * A placeholder fragment containing a simple view.
 */
class FirstFragment : BaseFragment("First Fragment") {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    addTest("Prompt after 2 Seconds") {
      Toast.makeText(context, "Cheers", Toast.LENGTH_SHORT).show()
    }.also { testButton ->

      touchPrompt(SingleShot.TWO_SECOND_PROMPT) {
        target = testButton
        primaryText = "This is a button"
        secondaryText = "When you click it it will display a toast.\nIt will not display again"
        initialDelay = TimeUnit.SECONDS.toMillis(2)
      }

    }

    addTest("Fragment Prompt") {
      log.trace("showing fragment prompt")
      touchPrompt(mode = TouchPromptMode.FRAGMENT) {
        target = it
        primaryText = "Fragment Touch Prompt"
      }
    }

    addTest("Chain Test") {

      touchPrompt {
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

    addTest("Reset single shots") {
      TouchPrompt.clearPrefs(context!!)
      activity?.finish()
      activity?.findViewById<View>(android.R.id.content)?.post {
        activity?.startActivity(activity!!.intent)
      }
    }


  }

}


private val log = org.slf4j.LoggerFactory.getLogger(FirstFragment::class.java)