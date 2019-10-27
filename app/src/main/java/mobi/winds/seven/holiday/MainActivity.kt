package mobi.winds.seven.holiday

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.arellomobile.mvp.MvpAppCompatActivity
import io.reactivex.subjects.BehaviorSubject
import mobi.winds.seven.holiday.ui.main.MainFragment
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.main_activity.*
import mobi.winds.seven.holiday.ui.OnlineReceiver
import mobi.winds.seven.holiday.ui.changecity.ChangeCityFragment
import mobi.winds.seven.holiday.ui.map.MapFragment
import android.animation.ObjectAnimator
import android.net.ConnectivityManager
import android.content.IntentFilter
import android.view.ViewGroup
import android.widget.RelativeLayout

class MainActivity : MvpAppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var internetReceiver: OnlineReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        get = this

        val sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        if(sPref.getFloat(KEY_USER_LATITUDE, 200f) == 200f || sPref.getFloat(KEY_USER_LONGITUDE, 200f) == 200f) {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            if (hasPermissions(this, RUNTIME_PERMISSIONS) && checkLocationPermission()) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000 * 10, 10f, locationListener
                )
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 1000 * 10, 10f,
                    locationListener
                )
                changeFragment(mainFragment)
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if(location != null) {
                        showLocation(location)
                    }
                }
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if(location != null) {
                        showLocation(location)
                    }
                }

                android.os.Handler().postDelayed(
                    {
                        if(sPref.getFloat(KEY_USER_LATITUDE, 200f) == 200f ||
                            sPref.getFloat(KEY_USER_LONGITUDE, 200f) == 200f)
                            changeFragment(ChangeCityFragment())
                    },
                    2000
                )
            } else {
                ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE_ASK_PERMISSIONS)
            }
        } else {
            changeFragment(mainFragment)
        }

        internetReceiver = OnlineReceiver()
        OnlineReceiver.internetObserver.subscribe(object : Observer<Boolean> {

            override fun onComplete() {}

            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: Boolean) {
                val animator = ObjectAnimator.ofInt(no_internet_label.layoutParams.height, if(t) 0 else 80)

                animator.addUpdateListener {
                    no_internet_label.layoutParams =
                        RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, it.animatedValue as Int)
                }

                animator.duration = 300L
                animator.start()
            }

            override fun onError(e: Throwable) {}
        })
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(internetReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        //locationManager.removeUpdates(locationListener)
        unregisterReceiver(internetReceiver)
    }

    fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private val locationListener = object: LocationListener {

        override fun onLocationChanged(location: Location) {
            showLocation(location)
        }

        override fun onProviderDisabled(provider: String) {
        }

        override fun onProviderEnabled(provider: String) {
            if (checkLocationPermission()) {
                showLocation(locationManager.getLastKnownLocation(provider))
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            if (checkLocationPermission()) {
                showLocation(locationManager.getLastKnownLocation(provider))
            }
        }
    }

    fun showLocation(location: Location) {
        val sPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        println(location.latitude.toFloat())
        sPref.edit()
            .putFloat(KEY_USER_LATITUDE, location.latitude.toFloat())
            .putFloat(KEY_USER_LONGITUDE, location.longitude.toFloat())
            .apply()
    }

    fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                var isToCity = false
                for (index in 0 until permissions.size) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        isToCity = true
                        changeFragment(ChangeCityFragment())
                    }
                }
                if(!isToCity) changeFragment(mainFragment)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        lateinit var get: MainActivity

        val mainFragment = MainFragment()
        val mapFragment = MapFragment()

        const val APP_PREFERENCES = "mobi.winds.seven.holiday"
        private const val REQUEST_CODE_ASK_PERMISSIONS = 1

        private val RUNTIME_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        const val KEY_USER_LATITUDE = "user_latitude"
        const val KEY_USER_LONGITUDE = "user_longitude"

        const val KEY_USER_TOKEN = "user_token"
        const val KEY_USER_DATA = "user_data"

        const val radiusGetActions = 4f
    }
}
