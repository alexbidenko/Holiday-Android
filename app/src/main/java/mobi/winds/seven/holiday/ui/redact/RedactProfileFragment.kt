package mobi.winds.seven.holiday.ui.redact

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_redact_profile.view.*
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.R
import mobi.winds.seven.holiday.objects.User
import mobi.winds.seven.holiday.ui.profile.ProfileFragment
import mobi.winds.seven.holiday.ui.registration.RegistrationTask
import mobi.winds.seven.holiday.ui.secret.SecretFragment
import org.json.JSONObject

class RedactProfileFragment : Fragment() {

    private val emailRegexChecker = Regex("""(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])""")
    private val phoneRegexChecker = Regex("^[+]?[0-9]{10,13}\$")

    var isChangePassword = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_redact_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.redact_profile_back_button.setOnClickListener {
            MainActivity.get.changeFragment(ProfileFragment())
        }

        val sPref = context!!.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)
        val user = User.fromJSON(JSONObject(sPref.getString(MainActivity.KEY_USER_DATA, null)!!))

        view.redact_profile_user_login.text = user.login

        view.redact_profile_user_name_input.setText(user.name)
        view.redact_profile_user_email_input.setText(user.email)
        view.redact_profile_user_phone_input.setText(user.phone)
        view.redact_profile_user_new_password_input.setText(user.password)

        view.redact_profile_done_button.setOnClickListener {
            if(checkInputDate(view)) {
                view.redact_profile_done_button.isEnabled = false

                user.name = view.redact_profile_user_name_input.text.toString()
                user.phone = view.redact_profile_user_phone_input.text.toString()
                if(user.email == view.redact_profile_user_email_input.text.toString()) {
                    UpdateProfileTask.update(
                        activity!!, user,
                        Runnable {
                            view.redact_profile_done_button.isEnabled = true
                            Toast.makeText(context, "Вы не можете сейчас обновить свой профиль", Toast.LENGTH_LONG)
                                .show()
                        },
                        if (isChangePassword) view.redact_profile_user_new_password_input.text.toString()
                        else null
                    )
                } else {
                    user.email = view.redact_profile_user_email_input.text.toString()

                    RegistrationTask.checkEmail(user.email)

                    SecretFragment.user = user
                    SecretFragment.isNewUser = false
                    SecretFragment.newPassword =
                        if (isChangePassword) view.redact_profile_user_new_password_input.text.toString()
                        else null

                    MainActivity.get.changeFragment(SecretFragment())
                }
            }
        }

        view.redact_profile_user_new_password_input.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                isChangePassword = s.toString() != user.password
                view.redact_profile_check_password_layout.visibility = if(s.toString() == user.password) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun clearPhone(phone: String): String {
        return phone.replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .replace(" ", "")
    }

    private fun checkInputDate(view: View): Boolean {
        var isCorrect = true
        var messageError = ""
        if(view.redact_profile_user_name_input.text.length > 64 ||
            view.redact_profile_user_name_input.text.isEmpty()) {
            isCorrect = false
            messageError = "Имя не должно превышать 64 символа или быть пустым"
        } else if(!view.redact_profile_user_email_input.text.matches(emailRegexChecker)) {
            isCorrect = false
            messageError = "Неправильныей формат Email"
        } else if(!clearPhone(view.redact_profile_user_phone_input.text.toString()).matches(phoneRegexChecker)) {
            isCorrect = false
            messageError = "Неправильный формат телефона"
        } else if(view.redact_profile_user_new_password_input.text.length < 8 ||
            view.redact_profile_user_new_password_input.text.length > 32) {
            isCorrect = false
            messageError = "Пароль должен быть от 8 до 32 символов"
        } else if(view.redact_profile_user_new_password_input.text == view.redact_profile_user_new_password_repeat_input.text) {
            isCorrect = false
            messageError = "Пароли не совпадают"
        }

        if(!isCorrect) {
            Toast.makeText(context!!, messageError, Toast.LENGTH_LONG).show()
        }

        return isCorrect
    }
}
