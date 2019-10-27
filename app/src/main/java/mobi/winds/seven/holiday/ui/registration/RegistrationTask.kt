package mobi.winds.seven.holiday.ui.registration

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.widget.Toast
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.datas.Server
import mobi.winds.seven.holiday.objects.User
import mobi.winds.seven.holiday.ui.profile.ProfileFragment
import mobi.winds.seven.holiday.ui.secret.SecretFragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object RegistrationTask {

    fun singUp(activity: Activity, user: User, secret: String?, finish: Runnable) {
        val mediaType = MediaType.parse("application/json; charset=utf-8")

        val client = OkHttpClient()

        val body = RequestBody.create(mediaType, User.toJSON(user).toString())
        val request = Request.Builder()
            .url(Server.url + "/users/registration")
            .post(body)
            .addHeader("Secret", secret ?: " ")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Выбранный вами логин уже занят", Toast.LENGTH_LONG).show()
                    Handler().post(finish)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity.runOnUiThread {
                    Handler().post(finish)
                    when (response.code()) {
                        409 -> {
                            Toast.makeText(activity, "Выбранный вами логин уже занят", Toast.LENGTH_LONG).show()
                        }
                        403 -> {
                            if(secret == null) {
                                checkEmail(user.email)
                                SecretFragment.user = user
                                SecretFragment.isNewUser = true
                                SecretFragment.newPassword = null
                                MainActivity.get.changeFragment(SecretFragment())
                            } else {
                                Toast.makeText(activity, "Неверный код подтверждения", Toast.LENGTH_LONG).show()
                            }
                        }
                        else -> response.body()?.string().let {
                            val sPref =
                                activity.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)
                            sPref.edit().putString(MainActivity.KEY_USER_TOKEN, JSONObject(it).getString("token"))
                                .putString(MainActivity.KEY_USER_DATA, User.toJSON(user).toString()).apply()

                            MainActivity.get.changeFragment(ProfileFragment())
                        }
                    }
                }
            }
        })
    }

    fun checkEmail(email: String) {
        val secretClient = OkHttpClient()

        val secretRequest = Request.Builder()
            .url(Server.url + "/users/check-email")
            .get()
            .addHeader("Email", email)
            .build()

        secretClient.newCall(secretRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {}
        })
    }
}