package mobi.winds.seven.holiday.tasks

import io.reactivex.subjects.ReplaySubject
import mobi.winds.seven.holiday.datas.Server
import mobi.winds.seven.holiday.objects.City
import mobi.winds.seven.holiday.ui.OnlineReceiver
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

object GetCitiesTask {

    val citiesObserver = ReplaySubject.create<ArrayList<City>>()
    val citiesData = ArrayList<City>()

    fun getCities() {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("${Server.url}/cities/get")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                OnlineReceiver.nextTasks.add(Runnable {
                    getCities()
                })
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.string().let {
                    val cities = ArrayList<City>()
                    for(i in 0 until JSONArray(it).length()) {
                        cities.add(City.fromJSON(JSONArray(it).getJSONObject(i)))
                    }
                    citiesData.addAll(cities)
                    citiesObserver.onNext(cities)
                }
            }
        })
    }
}