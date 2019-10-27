package mobi.winds.seven.holiday.ui.map

import io.reactivex.subjects.PublishSubject
import mobi.winds.seven.holiday.datas.Server
import mobi.winds.seven.holiday.objects.ActionDemo
import mobi.winds.seven.holiday.objects.ActionDetail
import mobi.winds.seven.holiday.ui.OnlineReceiver
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object GetMapActionsTask {

    val actionDetailObserver = PublishSubject.create<ActionDetail>()
    val actionsMapObserver = PublishSubject.create<ArrayList<ActionDemo>>()

    fun getDetail(id: Long) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("${Server.url}/actions/detail/$id")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {
                response.body()!!.string().let {
                    actionDetailObserver.onNext(ActionDetail.fromJSON(JSONObject(it)))
                }
            }
        })
    }

    fun getForMap(latitude: Double, longitude: Double, radius: Double) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("${Server.url}/actions/coordinates?latitude=$latitude&longitude=$longitude&radius=$radius")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                OnlineReceiver.nextTasks.add(Runnable {
                    getForMap(latitude, longitude, radius)
                })
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()!!.string().let {
                    val actionsDemo = ArrayList<ActionDemo>()
                    for (i in 0 until JSONArray(it).length()) {
                        actionsDemo.add(ActionDemo.fromJSON(JSONArray(it).getJSONObject(i)))
                    }
                    actionsMapObserver.onNext(actionsDemo)
                }
            }
        })
    }
}