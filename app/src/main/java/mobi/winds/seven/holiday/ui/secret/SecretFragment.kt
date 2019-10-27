package mobi.winds.seven.holiday.ui.secret

import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_secret.view.*
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.R
import mobi.winds.seven.holiday.objects.User
import mobi.winds.seven.holiday.ui.redact.UpdateProfileTask
import mobi.winds.seven.holiday.ui.registration.RegistrationFragment
import mobi.winds.seven.holiday.ui.registration.RegistrationTask

class SecretFragment : Fragment() {

    private val repeatTimerFormat = "Повторная отправка кода возможна через: %d секунд"

    private val countDownTimer = object : CountDownTimer(60 * 1000, 1000) {
        override fun onFinish() {
            view?.secret_repeat_button?.isEnabled = true

            view?.secret_repeat_timer?.visibility = View.INVISIBLE
        }

        override fun onTick(millisUntilFinished: Long) {
            view?.secret_repeat_timer?.text = String.format(repeatTimerFormat, ((60 - millisUntilFinished) / 1000).toInt())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(user == null) {
            MainActivity.get.changeFragment(MainActivity.mainFragment)
        }

        return inflater.inflate(R.layout.fragment_secret, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.secret_back_button.setOnClickListener {
            MainActivity.get.changeFragment(RegistrationFragment())
        }

        view.secret_confirm_button.setOnClickListener {
            if(view.secret_input.text.isNotEmpty()) {
                view.secret_confirm_button.isEnabled = false
                if(isNewUser) {
                    RegistrationTask.singUp(activity!!, user!!, view.secret_input.text.toString(),
                        Runnable {
                            view.secret_confirm_button.isEnabled = true
                        }
                    )
                } else {
                    UpdateProfileTask.update(
                        activity!!,
                        user!!,
                        Runnable {
                            view.secret_confirm_button.isEnabled = true
                            Toast.makeText(context, "Вы не можете сейчас обновить свой профиль", Toast.LENGTH_LONG)
                                .show()
                        },
                        newPassword
                    )
                }
            }
        }

        view.secret_repeat_timer.text = String.format(repeatTimerFormat, 60)
        countDownTimer.start()

        view.secret_repeat_button.setOnClickListener {
            view.secret_repeat_button.isEnabled = false
            view.secret_repeat_timer.visibility = View.VISIBLE
            view.secret_repeat_timer.text = String.format(repeatTimerFormat, 60)

            countDownTimer.start()

            RegistrationTask.checkEmail(user!!.email)
        }
    }

    companion object {
        var user: User? = null

        var isNewUser = true
        var newPassword: String? = null
    }
}
