package mobi.winds.seven.holiday.ui.settings

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_settings.view.*
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.R
import mobi.winds.seven.holiday.ui.changecity.ChangeCityFragment
import mobi.winds.seven.holiday.ui.login.LoginFragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.settings_back_button.setOnClickListener {
            MainActivity.get.changeFragment(MainActivity.mainFragment)
        }

        val sPref = context!!.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)
        val userCity = sPref.getString(KEY_USER_CITY, null)
        if(userCity != null) {
            view.settings_user_city.text = userCity
        }
        view.settings_to_profile.setOnClickListener {
            MainActivity.get.changeFragment(LoginFragment())
        }

        view.settings_to_city.setOnClickListener {
            MainActivity.get.changeFragment(ChangeCityFragment())
        }
    }

    companion object {
        const val KEY_USER_CITY = "user_city"
    }
}
