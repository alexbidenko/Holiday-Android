package mobi.winds.seven.holiday.ui

import android.net.ConnectivityManager
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Handler
import io.reactivex.subjects.PublishSubject

class OnlineReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        if(netInfo != null && netInfo.isConnected) {
            internetObserver.onNext(true)
            nextTasks.forEach {
                Handler().post(it)
            }
            nextTasks.clear()
        } else {
            internetObserver.onNext(false)
        }
    }

    companion object {

        val nextTasks = ArrayList<Runnable>()
        val internetObserver = PublishSubject.create<Boolean>()
    }
}