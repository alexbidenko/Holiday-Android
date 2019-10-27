package mobi.winds.seven.holiday.tasks

import android.annotation.SuppressLint
import io.reactivex.subjects.ReplaySubject
import mobi.winds.seven.holiday.datas.Server
import mobi.winds.seven.holiday.objects.Category
import mobi.winds.seven.holiday.objects.SubCategory
import mobi.winds.seven.holiday.ui.OnlineReceiver
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

object GetCategoriesTask {

    val categoriesObserver = ReplaySubject.create<ArrayList<Category>>()
    val subCategoriesObserver = ReplaySubject.create<ArrayList<SubCategory>>()
    val categories = ArrayList<Category>()
    val subCategories = ArrayList<SubCategory>()

    @SuppressLint("CheckResult")
    fun getCategories() {
        categoriesObserver.doOnError {  }

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("${Server.url}/categories/all")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                OnlineReceiver.nextTasks.add(Runnable {
                    getCategories()
                })
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.string().let {
                    val categoriesDemo = ArrayList<Category>()
                    for(i in 0 until JSONArray(it).length()) {
                        categoriesDemo.add(Category.fromJSON(JSONArray(it).getJSONObject(i)))
                    }
                    categories.addAll(categoriesDemo)
                    categoriesObserver.onNext(categoriesDemo)
                }
            }
        })
    }

    @SuppressLint("CheckResult")
    fun getSubCategories() {
        subCategoriesObserver.doOnError {  }

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("${Server.url}/categories/sub/all")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                OnlineReceiver.nextTasks.add(Runnable {
                    getSubCategories()
                })
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.string().let {
                    val subCategoriesDemo = ArrayList<SubCategory>()
                    for(i in 0 until JSONArray(it).length()) {
                        subCategoriesDemo.add(SubCategory.fromJSON(JSONArray(it).getJSONObject(i)))
                    }
                    subCategories.addAll(subCategoriesDemo)
                    subCategoriesObserver.onNext(subCategoriesDemo)
                }
            }
        })
    }
}