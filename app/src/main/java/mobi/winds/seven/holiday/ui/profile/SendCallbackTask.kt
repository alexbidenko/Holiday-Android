package mobi.winds.seven.holiday.ui.profile

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.widget.Toast
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.datas.Server
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object SendCallbackTask {

    fun send(activity: Activity, message: String, success: Runnable, error: Runnable) {
        val sPref = activity.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)
        val mediaType = MediaType.parse("application/json; charset=utf-8")

        val client = OkHttpClient()

        val body = RequestBody.create(mediaType, JSONObject().put("message", message).toString())
        val request = Request.Builder()
            .url(Server.url + "/callback/users/send")
            .post(body)
            .addHeader("Token", sPref.getString(MainActivity.KEY_USER_TOKEN, null)!!)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    Handler().post(error)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity.runOnUiThread {
                    when {
                        response.code() == 200 -> {
                            Handler().post(success)
                            Toast.makeText(activity, "Сообщение успешно отправлено", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Handler().post(error)
                            Toast.makeText(activity, "Извините, но вы не можете отправлять сообщения", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }
}