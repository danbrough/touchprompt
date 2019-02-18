package danbroid.touchprompt.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment

open class BaseFragment(val title: String) : Fragment() {

  protected lateinit var tests: ViewGroup

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = LinearLayoutCompat(context).also {
    it.orientation = LinearLayoutCompat.VERTICAL
    tests = it
  }

  protected fun addTest(title: String, action: ((View) -> Unit)? = null): View =
    Button(context).apply {
      text = title
      setOnClickListener {
        action?.invoke(it)
      }
    }.also {
      tests.addView(it)
    }

  override fun onResume() {
    super.onResume()
    activity?.title = title
  }

}

private val log = org.slf4j.LoggerFactory.getLogger(BaseFragment::class.java)