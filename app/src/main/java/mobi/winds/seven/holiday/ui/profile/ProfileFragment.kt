package mobi.winds.seven.holiday.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_profile.view.*
import mobi.winds.seven.holiday.MainActivity

import mobi.winds.seven.holiday.R
import mobi.winds.seven.holiday.objects.User
import mobi.winds.seven.holiday.ui.changecity.ChangeCityFragment
import mobi.winds.seven.holiday.ui.redact.RedactProfileFragment
import mobi.winds.seven.holiday.ui.settings.SettingsFragment
import org.json.JSONObject

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.profile_back_button.setOnClickListener {
            MainActivity.get.changeFragment(MainActivity.mainFragment)
        }

        val sPref = context!!.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)

        val user = User.fromJSON(JSONObject(sPref.getString(MainActivity.KEY_USER_DATA, null)!!))

        view.profile_user_login.text = user.login
        view.profile_user_name.text = user.name
        view.profile_user_email.text = user.email
        view.profile_user_phone.text = user.phone

        val userCity = sPref.getString(SettingsFragment.KEY_USER_CITY, null)
        if(userCity != null) {
            view.profile_user_city.text = userCity
        }
        view.profile_city_layout.setOnClickListener {
            MainActivity.get.changeFragment(ChangeCityFragment())
        }

        view.profile_sing_out_button.setOnClickListener {
            sPref.edit()
                .remove(MainActivity.KEY_USER_DATA)
                .remove(MainActivity.KEY_USER_TOKEN)
                .apply()

            MainActivity.get.changeFragment(SettingsFragment())
        }

        view.profile_callback_button.setOnClickListener {
            if(view.profile_callback_input.text.length in 1..256) {
                view.profile_callback_button.isEnabled = false
                SendCallbackTask.send(activity!!, view.profile_callback_input.text.toString(),
                    Runnable {
                        view.profile_callback_button.isEnabled = true
                        view.profile_callback_input.text.clear()
                    },
                    Runnable {
                        view.profile_callback_button.isEnabled = true
                    }
                )
            } else if(view.profile_callback_input.text.length < 257) {
                Toast.makeText(context!!, "Сообщение не может превышать 256 символов", Toast.LENGTH_LONG).show()
            }
        }

        view.profile_redact_button.setOnClickListener {
            MainActivity.get.changeFragment(RedactProfileFragment())
        }
    }
}
