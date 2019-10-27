package mobi.winds.seven.holiday.ui.search

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_search.view.*
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.R
import android.app.Activity
import android.view.inputmethod.InputMethodManager

class SearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imm = context!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        view.search_back_button.setOnClickListener {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            MainActivity.get.changeFragment(MainActivity.mainFragment)
        }

        view.search_input.addTextChangedListener(object : TextWatcher {

            var lastInputTime = System.currentTimeMillis()

            override fun afterTextChanged(s: Editable?) {
                if(s?.isNotEmpty() == true) {
                    lastInputTime = System.currentTimeMillis()
                    val currentInputTime = lastInputTime
                    Handler().postDelayed(
                        {
                            if (lastInputTime == currentInputTime) {
                                SearchTask.search(s.toString(), activity!!, view.search_result_layout)
                            }
                        },
                        200
                    )
                } else {
                    view.search_result_layout.removeAllViews()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        view.search_input.requestFocus()
        imm.showSoftInput(view.search_input, 0)
    }
}
