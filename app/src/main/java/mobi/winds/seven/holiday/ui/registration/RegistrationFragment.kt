package mobi.winds.seven.holiday.ui.registration

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_registration.view.*
import mobi.winds.seven.holiday.MainActivity

import mobi.winds.seven.holiday.R
import mobi.winds.seven.holiday.ui.login.LoginFragment
import java.util.*
import android.text.format.DateUtils
import android.widget.Toast
import android.telephony.PhoneNumberFormattingTextWatcher
import mobi.winds.seven.holiday.objects.User
import mobi.winds.seven.holiday.ui.secret.SecretFragment

class RegistrationFragment : Fragment() {

    private val currentDate = Calendar.getInstance()
    private val emailRegexChecker = Regex("""(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])""")
    private val phoneRegexChecker = Regex("^[+]?[0-9]{10,13}\$")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentDate.add(Calendar.YEAR, -20)

        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.registration_back_button.setOnClickListener {
            MainActivity.get.changeFragment(LoginFragment())
        }

        view.registration_input_phone.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        view.registration_input_birthday.setOnClickListener {
            DatePickerDialog(context!!, d,
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        view.registration_sing_up_button.setOnClickListener {
            if(checkInputDate(view)) {
                view.registration_sing_up_button.isEnabled = false
                RegistrationTask.singUp(
                    activity!!,
                    User(
                        view.registration_input_name.text.toString(),
                        view.registration_input_login.text.toString(),
                        view.registration_input_email.text.toString(),
                        clearPhone(view.registration_input_phone.text.toString()),
                        currentDate.timeInMillis,
                        view.registration_input_password.text.toString()
                    ),
                    null,
                    Runnable {
                        view.registration_sing_up_button.isEnabled = true
                    }
                )
            }
        }

        if(SecretFragment.user != null) {
            val user = SecretFragment.user!!
            view.registration_input_name.setText(user.name)
            view.registration_input_login.setText(user.login)
            view.registration_input_email.setText(user.email)
            view.registration_input_phone.setText(user.phone)
            currentDate.timeInMillis = user.birthday
            view.registration_input_birthday?.text = DateUtils.formatDateTime(context,
                currentDate.timeInMillis,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
            )
            view.registration_input_password.setText(user.password)
            view.registration_input_repeat_password.setText(user.password)
        }
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
        if(view.registration_input_name.text.length > 64 ||
            view.registration_input_name.text.isEmpty()) {
            isCorrect = false
            messageError = "Имя не должно превышать 64 символа или быть пустым"
        } else if(view.registration_input_login.text.length < 8 ||
            view.registration_input_login.text.length > 32) {
            isCorrect = false
            messageError = "Логин должен быть от 8 до 32 символов"
        } else if(!view.registration_input_email.text.matches(emailRegexChecker)) {
            isCorrect = false
            messageError = "Неправильныей формат Email"
        } else if(!clearPhone(view.registration_input_phone.text.toString()).matches(phoneRegexChecker)) {
            isCorrect = false
            messageError = "Неправильный формат телефона"
        } else if(view.registration_input_birthday.text.isEmpty()) {
            isCorrect = false
            messageError = "Не указан день рождения"
        } else if(view.registration_input_password.text.length < 8 ||
            view.registration_input_password.text.length > 32) {
            isCorrect = false
            messageError = "Пароль должен быть от 8 до 32 символов"
        } else if(view.registration_input_password.text == view.registration_input_repeat_password.text) {
            isCorrect = false
            messageError = "Пароли не совпадают"
        }

        if(!isCorrect) {
            Toast.makeText(context!!, messageError, Toast.LENGTH_LONG).show()
        }

        return isCorrect
    }

    private val d = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        currentDate.set(Calendar.YEAR, year)
        currentDate.set(Calendar.MONTH, monthOfYear)
        currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        view?.registration_input_birthday?.text = DateUtils.formatDateTime(context,
            currentDate.timeInMillis,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
        )
    }
}
