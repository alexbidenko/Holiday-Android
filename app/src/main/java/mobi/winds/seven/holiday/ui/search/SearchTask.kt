package mobi.winds.seven.holiday.ui.search

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.search_result_block.view.*
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.R
import mobi.winds.seven.holiday.datas.Server
import mobi.winds.seven.holiday.objects.ActionDemo
import mobi.winds.seven.holiday.ui.map.GetMapActionsTask
import mobi.winds.seven.holiday.ui.map.MapFragment
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

object SearchTask {

    fun search(text: String, activity: Activity, layout: LinearLayout) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("${Server.url}/actions/search/$text")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}

            @SuppressLint("InflateParams")
            override fun onResponse(call: Call, response: Response) {
                if(response.code() == 200) {
                    response.body()?.string().let {
                        activity.runOnUiThread {
                            layout.removeAllViews()

                            for (i in 0 until JSONArray(it).length()) {
                                val action = ActionDemo.fromJSON(JSONArray(it).getJSONObject(i))

                                val result = activity.layoutInflater.inflate(R.layout.search_result_block, null)
                                Glide.with(result.search_result_image)
                                    .load("${Server.url}/images/get/${action.image}")
                                    .circleCrop()
                                    .into(result.search_result_image)
                                result.search_result_title.text = action.title

                                result.setOnClickListener {
                                    GetMapActionsTask.getDetail(action.id)
                                    MapFragment.actionToOpen = action
                                    MainActivity.get.changeFragment(MainActivity.mapFragment)
                                }

                                layout.addView(result)
                            }
                        }
                    }
                } else {
                    activity.runOnUiThread {
                        layout.removeAllViews()
                    }
                }
            }
        })
    }
}