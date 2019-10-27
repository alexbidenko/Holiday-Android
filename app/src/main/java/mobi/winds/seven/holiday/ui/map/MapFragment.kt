package mobi.winds.seven.holiday.ui.map

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v4.widget.NestedScrollView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.here.android.mpa.common.GeoCoordinate
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.map_fragment.view.*
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.R
import mobi.winds.seven.holiday.datas.Server
import mobi.winds.seven.holiday.objects.ActionDemo
import mobi.winds.seven.holiday.objects.ActionDetail
import mobi.winds.seven.holiday.ui.main.MainFragment
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.LinearLayout

class MapFragment : Fragment() {

    private lateinit var viewModel: MapViewModel
    private lateinit var actionDetails: BottomSheetBehavior<NestedScrollView>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.map_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.map_back_button.setOnClickListener {
            MainActivity.get.changeFragment(MainFragment())
        }

        actionDetails = BottomSheetBehavior.from(view.map_action_details)

        val mapFragment = MapFragmentView(this)

        if(actionToOpen == null) {
            actionDetails.state = BottomSheetBehavior.STATE_HIDDEN

            val sPref = context!!.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)
            mapFragment.startLocation = GeoCoordinate(
                sPref.getFloat(MainActivity.KEY_USER_LATITUDE, 0f).toDouble(),
                sPref.getFloat(MainActivity.KEY_USER_LONGITUDE, 0f).toDouble()
            )
            mapFragment.startZoom = 12.0
        } else {
            val actionDemo = actionToOpen!!
            openAction(view, actionDemo)

            if(actionDemo.latitude != null && actionDemo.longitude != null) {
                mapFragment.startLocation = GeoCoordinate(actionDemo.latitude - 0.002, actionDemo.longitude + 0.001)
                mapFragment.startZoom = 17.0
            } else {
                val sPref = context!!.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)
                mapFragment.startLocation = GeoCoordinate(
                    sPref.getFloat(MainActivity.KEY_USER_LATITUDE, 0f).toDouble(),
                    sPref.getFloat(MainActivity.KEY_USER_LONGITUDE, 0f).toDouble()
                )
                mapFragment.startZoom = 12.0
            }
        }
        mapFragment.initMapFragment()

        view.action_details_more_button.setOnClickListener {
            if(actionDetails.state == BottomSheetBehavior.STATE_EXPANDED) {
                actionDetails.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            } else {
                actionDetails.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        actionDetails.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {}

            override fun onStateChanged(p0: View, state: Int) {
                if(state == BottomSheetBehavior.STATE_EXPANDED) {
                    view.action_details_more.setImageResource(R.drawable.ic_expand_more_24dp)
                } else {
                    view.action_details_more.setImageResource(R.drawable.ic_expand_less_24dp)
                }
            }

        })

        mapFragment.mapSelectorObserver.subscribe(object : Observer<ActionDemo> {
            override fun onComplete() {}

            override fun onSubscribe(d: Disposable) {}

            override fun onNext(action: ActionDemo) {
                openAction(view, action)
            }

            override fun onError(e: Throwable) {}
        })
    }

    private fun openAction(view: View, actionDemo: ActionDemo) {
        val actionDetail = lastActionDetail
        if(actionDetail != null && actionDetail.actionId == actionDemo.id)
            showActionDetail(view, actionDetail)
        else {
            view.action_details_content.visibility = View.GONE
            view.action_details_loader.visibility = View.VISIBLE
            GetMapActionsTask.getDetail(actionDemo.id)
        }

        actionDetails.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        view.action_details_title.text = actionDemo.title
        Glide.with(view.action_details_image)
            .load(Server.url + "/images/get/" + actionDemo.image)
            .placeholder(R.drawable.image_preview)
            .centerCrop()
            .into(view.action_details_image)

        GetMapActionsTask.actionDetailObserver.subscribe(object : Observer<ActionDetail> {
            override fun onComplete() {}

            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: ActionDetail) {
                activity?.runOnUiThread {
                    if(t.actionId == actionDemo.id) showActionDetail(view, t)
                }
            }

            override fun onError(e: Throwable) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun showActionDetail(view: View, t: ActionDetail) {
        if(t.information != null) {
            view.action_details_information.text = t.information
        } else {
            view.action_details_information_layout.visibility = View.GONE
        }
        view.action_details_address.text = t.address

        view.action_details_work_time.text =
            "По будням: с ${t.workTime["weekdays"]?.get("start")} до ${t.workTime["weekdays"]?.get("end")}\n" +
                    "Суббота: с ${t.workTime["saturday"]?.get("start")} до ${t.workTime["saturday"]?.get("end")}\n" +
                    "Воскресенье: с ${t.workTime["sunday"]?.get("start")} до ${t.workTime["sunday"]?.get("end")}"

        view.action_details_phone.text = t.phone
        if(t.site != null) {
            view.action_details_site_layout.visibility = View.VISIBLE
            view.action_details_site.text = t.site
            view.action_details_site.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(t.site))
                startActivity(intent)
            }
        } else {
            view.action_details_site_layout.visibility = View.GONE
        }

        view.map_action_details.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        var isSocialNetworks = false
        view.action_details_socials.removeAllViews()
        t.socialNetworks.keys.forEach {
            if(t.socialNetworks[it] != null && t.socialNetworks[it] != "") {
                isSocialNetworks = true
                val icon = ImageView(context)
                val lp = LinearLayout.LayoutParams(80, 80)
                lp.setMargins(40, 4, 0, 0)
                icon.layoutParams = lp
                when(it) {
                    "instagram" -> icon.setImageResource(R.drawable.icon_instagram)
                    "twitter" -> icon.setImageResource(R.drawable.icon_twitter)
                    "facebook" -> icon.setImageResource(R.drawable.icon_facebook)
                    "vk" -> icon.setImageResource(R.drawable.icon_vk)
                }
                val url = if(t.socialNetworks[it]?.startsWith("http") == true) {
                    t.socialNetworks[it]
                } else {
                    "https://" + t.socialNetworks[it]
                }
                icon.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
                view.action_details_socials.addView(icon)
            }
        }
        view.action_details_socials_layout.visibility =
            if(isSocialNetworks) View.VISIBLE
            else View.GONE

        view.action_details_loader.visibility = View.GONE
        view.action_details_content.visibility = View.VISIBLE
    }

    companion object {

        var actionToOpen: ActionDemo? = null

        private var lastActionDetail: ActionDetail? = null
    }
}
