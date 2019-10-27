package mobi.winds.seven.holiday.tasks

import android.annotation.SuppressLint
import io.reactivex.subjects.ReplaySubject
import mobi.winds.seven.holiday.datas.Server
import mobi.winds.seven.holiday.objects.ActionDemo
import mobi.winds.seven.holiday.ui.OnlineReceiver
import mobi.winds.seven.holiday.ui.main.MainActionsAdapter
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

object GetActionsTask {

    val actionsObserver = ReplaySubject.create<ArrayList<ActionDemo>>()
    var isCanDoRequest = true
    var isAllActions = false
    var page = 0

    @SuppressLint("CheckResult")
    fun getActions(latitude: Double, longitude: Double, radius: Float) {
        if(!isAllActions && isCanDoRequest) {
            isCanDoRequest = false

            val client = OkHttpClient()

            val request = Request.Builder()
                .url("${Server.url}/actions/page?page=$page&userLatitude=$latitude&userLongitude=$longitude&radius=$radius")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    OnlineReceiver.nextTasks.add(Runnable {
                        isCanDoRequest = true
                        getActions(latitude, longitude, radius)
                    })
                }

                override fun onResponse(call: Call, response: Response) {
                    page++

                    response.body()!!.string().let {
                        if (JSONArray(it).length() < 10) {
                            isAllActions = true
                        }

                        val actionsDemo = ArrayList<ActionDemo>()
                        for (i in 0 until JSONArray(it).length()) {
                            actionsDemo.add(ActionDemo.fromJSON(JSONArray(it).getJSONObject(i)))
                        }
                        isCanDoRequest = true
                        MainActionsAdapter.isLoad = false
                        actionsObserver.onNext(actionsDemo)
                    }
                }
            })
        }
    }
}