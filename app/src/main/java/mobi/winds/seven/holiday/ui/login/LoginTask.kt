package mobi.winds.seven.holiday.ui.login

import android.app.Activity
import android.content.Context
import android.widget.Button
import android.widget.Toast
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.datas.Server
import mobi.winds.seven.holiday.objects.User
import mobi.winds.seven.holiday.ui.profile.ProfileFragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object LoginTask {

    fun singIn(activity: Activity, login: String, password: String, button: Button) {
        val mediaType = MediaType.parse("application/json; charset=utf-8")

        val client = OkHttpClient()

        val body = RequestBody.create(mediaType, JSONObject().put("login", login).put("password", password).toString())
        val request = Request.Builder()
            .url(Server.url + "/users/login")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    button.isEnabled = true
                    Toast.makeText(activity, "Неверный логин или пароль", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val textResponse = response.body()?.string()
                activity.runOnUiThread {
                    button.isEnabled = true
                    when {
                        response.code() == 404 -> {
                            Toast.makeText(activity, "Пользователь с таким логином не найден", Toast.LENGTH_LONG).show()
                        }
                        response.code() == 409 -> {
                            Toast.makeText(activity, "Неверный пароль", Toast.LENGTH_LONG).show()
                        }
                        else -> textResponse.let {
                            val sPref =
                                activity.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)
                            val user = User.fromJSON(JSONObject(it).getJSONObject("user"))
                            user.password = password
                            sPref.edit()
                                .putString(MainActivity.KEY_USER_TOKEN, JSONObject(it).getString("token"))
                                .putString(MainActivity.KEY_USER_DATA, User.toJSON(user).toString())
                                .apply()

                            MainActivity.get.changeFragment(ProfileFragment())
                        }
                    }
                }
            }
        })
    }
}