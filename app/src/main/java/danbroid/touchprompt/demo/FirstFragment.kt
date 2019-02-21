package danbroid.touchprompt.demo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import danbroid.touchprompt.TouchPrompt
import danbroid.touchprompt.fragmentTouchPrompt
import danbroid.touchprompt.touchPrompt


/**
 * A placeholder fragment containing a simple view.
 */
class FirstFragment : BaseFragment("First Fragment") {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    addTest("Positioned prompt") {
      activity!!.touchPrompt {
        setTargetPosition(200f, 400f)
        primaryText = "Positioned Prompt"
        secondaryText = "Will show a toast when finished"
        onDismissed = {
          Toast.makeText(context, "Dismissed positioned prompt!", Toast.LENGTH_SHORT).show()
        }
      }
    }

    addTest("Show second fragment") {
      activity?.supportFragmentManager?.beginTransaction()?.run {
        replace(R.id.fragment, SecondFragment())
        addToBackStack(null)
        commit()
      }
    }

    var count = 0

    addTest("Showing a fragment prompt") {
      count++
      var title = "Fragment prompt: $count"
      Toast.makeText(context!!, "Showing fragment prompt:$count in 2 seconds", Toast.LENGTH_SHORT)
        .show()
      fragmentTouchPrompt {
        primaryText = title
        setTargetPosition(100f, 40f)
        initialDelay = 2000
      }
    }

    addTest("Activity Prompt") {
      Toast.makeText(context!!, "Showing activity prompt in 2 seconds", Toast.LENGTH_SHORT).show()
      touchPrompt {
        primaryText = "The search button"
        secondaryText = "In case you forgot about it"
        initialDelay = 2000
        targetID = R.id.action_search
      }
    }

    addTest("Serial prompts") { button ->
      Toast.makeText(context!!, "Showing 3 prompts in a row", Toast.LENGTH_SHORT).show()

      touchPrompt {
        primaryText = "First prompt"
        targetID = R.id.action_search
        initialDelay = 1000
      }

      //this will show unless the fragment is replaced
      touchPrompt {
        primaryText = "Second prompt"
        target = button
      }

      //this will always show
      activity!!.touchPrompt {
        primaryText = "Third prompt"
        targetID = R.id.action_search
      }
    }

    addTest("Reset single show prompts") {
      TouchPrompt.clearPrefs(context!!)
      activity?.run {
        finish()
        startActivity(intent)
      }
    }

  }


}


private val log = org.slf4j.LoggerFactory.getLogger(FirstFragment::class.java)