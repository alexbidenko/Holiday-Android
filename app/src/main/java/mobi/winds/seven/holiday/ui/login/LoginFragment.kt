package mobi.winds.seven.holiday.ui.login

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_login.view.*
import mobi.winds.seven.holiday.MainActivity

import mobi.winds.seven.holiday.R
import mobi.winds.seven.holiday.ui.registration.RegistrationFragment
import mobi.winds.seven.holiday.ui.settings.SettingsFragment

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.login_back_button.setOnClickListener {
            MainActivity.get.changeFragment(SettingsFragment())
        }

        view.login_sing_in_button.setOnClickListener {
            view.login_sing_in_button.isEnabled = false
            LoginTask.singIn(
                activity!!,
                view.login_input_login.text.toString(),
                view.login_input_password.text.toString(),
                view.login_sing_in_button
            )
        }
        view.login_to_registration_button.setOnClickListener {
            MainActivity.get.changeFragment(RegistrationFragment())
        }
    }
}
