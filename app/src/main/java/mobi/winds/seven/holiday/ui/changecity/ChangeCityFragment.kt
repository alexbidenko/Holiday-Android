package mobi.winds.seven.holiday.ui.changecity

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_change_city.view.*
import mobi.winds.seven.holiday.R
import mobi.winds.seven.holiday.tasks.GetCitiesTask
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.change_city_card.view.*
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.objects.City
import mobi.winds.seven.holiday.ui.profile.ProfileFragment
import mobi.winds.seven.holiday.ui.settings.SettingsFragment

class ChangeCityFragment : Fragment() {

    lateinit var sPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_change_city, container, false)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sPref = context!!.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)

        if(GetCitiesTask.citiesData.isNotEmpty()) {
            addCities(view, GetCitiesTask.citiesData)
        } else {
            GetCitiesTask.getCities()

            GetCitiesTask.citiesObserver.subscribe(object : Observer<ArrayList<City>> {
                override fun onComplete() {}

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("InflateParams")
                override fun onNext(list: ArrayList<City>) {
                    activity?.runOnUiThread {
                        addCities(view, list)
                    }
                }

                override fun onError(e: Throwable) {}
            })
        }

        if(sPref.contains(MainActivity.KEY_USER_LATITUDE) &&
            sPref.contains(MainActivity.KEY_USER_LONGITUDE)) {
            view.change_city_back_button.visibility = View.VISIBLE
            view.change_city_back_button.setOnClickListener {
                if (sPref.contains(MainActivity.KEY_USER_DATA)) {
                    MainActivity.get.changeFragment(ProfileFragment())
                } else {
                    MainActivity.get.changeFragment(SettingsFragment())
                }
            }
        } else {
            view.change_city_back_button.visibility = View.GONE
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun addCities(view: View, list: ArrayList<City>) {
        view.change_city_cities_list.removeAllViews()

        val citiesText = ArrayList<String>()
        list.forEach {
            citiesText.add(it.city)

            val cityCard = layoutInflater.inflate(R.layout.change_city_card, null)
            cityCard.change_city_card_city.text = it.city
            cityCard.setOnClickListener {_ ->
                sPref.edit()
                    .putFloat(MainActivity.KEY_USER_LATITUDE, it.latitude.toFloat())
                    .putFloat(MainActivity.KEY_USER_LONGITUDE, it.longitude.toFloat())
                    .putString(SettingsFragment.KEY_USER_CITY, it.city)
                    .apply()

                if(sPref.contains(MainActivity.KEY_USER_DATA)) {
                    MainActivity.get.changeFragment(ProfileFragment())
                } else {
                    MainActivity.get.changeFragment(SettingsFragment())
                }
            }
            view.change_city_cities_list.addView(cityCard)
        }


        view.change_city_input.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                for(i in 0 until citiesText.size) {
                    if(citiesText[i].toLowerCase().contains(s.toString().toLowerCase())) {
                        view.change_city_cities_list.getChildAt(i).visibility = View.VISIBLE
                    } else {
                        view.change_city_cities_list.getChildAt(i).visibility = View.GONE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}
