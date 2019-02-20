package danbroid.touchprompt.demo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import danbroid.touchprompt.TouchPrompt
import danbroid.touchprompt.touchPrompt
import danbroid.touchprompt.touchSequence


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

    addTest("Showing a fragment prompt") {
      touchPrompt {
        primaryText = "Fragment prompt"
        setTargetPosition(100f, 40f)
      }
    }

    addTest("Reset single show prompts") {
      TouchPrompt.clearPrefs(context!!)
      activity?.run {
        finish()
        startActivity(intent)
      }
    }

    val doOnClick: (View.() -> View) = {
      this
    }
    val b1 = addTest("B1").doOnClick()
    val b2 = addTest("B2").doOnClick()

    touchPrompt {
      primaryText = "B1"
      target = b1
    }.touchPrompt {
      primaryText = "B2"
      target = b2
    }
  }


}


private val log = org.slf4j.LoggerFactory.getLogger(FirstFragment::class.java)