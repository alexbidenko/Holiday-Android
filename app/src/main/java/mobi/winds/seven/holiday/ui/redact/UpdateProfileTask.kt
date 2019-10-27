package mobi.winds.seven.holiday.ui.redact

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.widget.Toast
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.datas.Server
import mobi.winds.seven.holiday.objects.User
import mobi.winds.seven.holiday.ui.profile.ProfileFragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object UpdateProfileTask {

    fun update(activity: Activity, user: User, error: Runnable, newPassword: String?) {
        val sPref = activity.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)

        val mediaType = MediaType.parse("application/json; charset=utf-8")

        val client = OkHttpClient()

        val body = RequestBody.create(mediaType, User.toJSON(user).toString())
        val request = Request.Builder()
            .url(Server.url + "/users/update")
            .put(body)
            .addHeader("Token", sPref.getString(MainActivity.KEY_USER_TOKEN, null) ?: " ")
            .addHeader("Secret", " ")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    Handler().post(error)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity.runOnUiThread {
                    when (response.code()) {
                        200 -> {
                            if(newPassword != null) {
                                updatePassword(activity, newPassword, user, error)
                            } else {
                                sPref.edit()
                                    .putString(MainActivity.KEY_USER_DATA, User.toJSON(user).toString()).apply()

                                Toast.makeText(activity, "Данные профиля успешно изменены", Toast.LENGTH_LONG).show()
                                MainActivity.get.changeFragment(ProfileFragment())
                            }
                        }
                        else -> {
                            Handler().post(error)
                        }
                    }
                }
            }
        })
    }

    fun updatePassword(activity: Activity, newPassword: String, user: User, error: Runnable) {
        val sPref = activity.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)

        val mediaType = MediaType.parse("application/json; charset=utf-8")

        val client = OkHttpClient()

        val body = RequestBody.create(
            mediaType,
            JSONObject()
                .put("oldPassword", user.password)
                .put("newPassword", newPassword).toString()
        )
        val request = Request.Builder()
            .url(Server.url + "/users/password")
            .put(body)
            .addHeader("Token", sPref.getString(MainActivity.KEY_USER_TOKEN, null) ?: " ")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    Handler().post(error)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity.runOnUiThread {
                    when (response.code()) {
                        409, 404 -> Handler().post(error)
                        else -> {
                            user.password = newPassword
                            sPref.edit()
                                .putString(MainActivity.KEY_USER_DATA, User.toJSON(user).toString()).apply()

                            Toast.makeText(activity, "Данные профиля успешно изменены", Toast.LENGTH_LONG).show()
                            MainActivity.get.changeFragment(ProfileFragment())
                        }
                    }
                }
            }
        })
    }
}