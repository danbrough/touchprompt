package danbroid.touchprompt.demo

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import danbroid.touchprompt.touchPrompt
import kotlinx.android.synthetic.main.activity_main.*

enum class SingleShot {
  TWO_SECOND_PROMPT, TEST1, FRAGMENT2_TOUCH_HERE, FAB
}


class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    log.info("onCreate()")
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)

    supportFragmentManager.beginTransaction().replace(R.id.fragment, FirstFragment()).commit()

    fab.setOnClickListener {
      touchPrompt {
        primaryText = "This is the FAB!"
        targetID = R.id.fab
      }
    }

    touchPrompt(SingleShot.FAB) {
      primaryText = "Click here for a prompt"
      secondaryTextID = R.string.msg_i_wont_appear_again
      targetID = R.id.fab
    }


  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    return when (item.itemId) {
      R.id.action_settings -> true
      else -> super.onOptionsItemSelected(item)
    }
  }

  override fun onBackPressed() {
    if (supportFragmentManager.backStackEntryCount > 1) {
      supportFragmentManager.popBackStack()
      return
    }
    super.onBackPressed()
  }
}

private val log = org.slf4j.LoggerFactory.getLogger(MainActivity::class.java)

