package mobi.winds.seven.holiday.ui.map

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.*
import android.support.v4.app.Fragment
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.Image
import com.here.android.mpa.common.OnEngineInitListener
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.SupportMapFragment
import mobi.winds.seven.holiday.R
import com.here.android.mpa.mapping.MapMarker
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.here.android.mpa.common.ViewObject
import com.here.android.mpa.mapping.MapGesture
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.map_marker.view.*
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.datas.Server
import mobi.winds.seven.holiday.objects.ActionDemo
import mobi.winds.seven.holiday.tasks.GetActionsTask
import mobi.winds.seven.holiday.tasks.GetCategoriesTask

class MapFragmentView(val fragment: Fragment) {

    private var map: Map? = null
    var startLocation: GeoCoordinate? = null
    var startZoom: Double? = null

    val mapSelectorObserver = PublishSubject.create<ActionDemo>()

    fun initMapFragment() {
        val mapFragment = fragment.childFragmentManager.findFragmentById(R.id.map_map) as SupportMapFragment

        mapFragment.init { error ->
            if (error == OnEngineInitListener.Error.NONE) {
                map = mapFragment.map

                startLocation?.let { map!!.setCenter(it, Map.Animation.NONE) }
                startZoom?.let { map!!.zoomLevel = it }

                GetActionsTask.actionsObserver.subscribe(object : Observer<ArrayList<ActionDemo>> {
                    override fun onComplete() {}

                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(list: ArrayList<ActionDemo>) {
                        actions.addAll(list.filter {listAction ->
                            actions.forEach {
                                if(it.id == listAction.id) return@filter false
                            }
                            addMapMarker(fragment.context!!, listAction)
                            return@filter true
                        })
                    }

                    override fun onError(e: Throwable) {}
                })

                val sPref = fragment.context!!.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)
                checkedPoints.add(GeoCoordinate(
                    sPref.getFloat(MainActivity.KEY_USER_LATITUDE, 0f).toDouble(),
                    sPref.getFloat(MainActivity.KEY_USER_LONGITUDE, 0f).toDouble()
                ))

                subscribe()

                mapFragment.mapGesture.addOnGestureListener(object : MapGesture.OnGestureListener {
                        override fun onPanStart() {}

                        override fun onPanEnd() {
                            setMarkersVisible()

                            var isDoUpdate = true
                            checkedPoints.forEach {
                                if(map!!.center.latitude < it.latitude + checkedRadius &&
                                    map!!.center.latitude > it.latitude - checkedRadius &&
                                    map!!.center.longitude < it.longitude + checkedRadius &&
                                    map!!.center.longitude > it.longitude - checkedRadius) isDoUpdate = false
                            }

                            if(isDoUpdate) {
                                if(checkedPoints.size > 20) checkedPoints.removeAt(0)
                                checkedPoints.add(map!!.center)
                                GetMapActionsTask.getForMap(
                                    map!!.center.latitude,
                                    map!!.center.longitude,
                                    checkedRadius
                                )
                            }
                        }

                        override fun onMultiFingerManipulationStart() {}

                        override fun onMultiFingerManipulationEnd() {}

                        override fun onMapObjectsSelected(list: List<ViewObject>): Boolean {
                            map!!.setCenter(
                                GeoCoordinate(
                                    (list[0] as MapMarker).coordinate.latitude - 0.0025,
                                    (list[0] as MapMarker).coordinate.longitude + 0.001
                                ),
                                Map.Animation.NONE
                            )
                            map!!.setZoomLevel(17.0, Map.Animation.LINEAR)
                            for(i in 0 until listMarkers.size) {
                                if(listMarkers[i] == list[0] as MapMarker) {
                                    mapSelectorObserver.onNext(listActionsDemo[i])
                                }
                            }

                            return false
                        }

                        override fun onTapEvent(pointF: PointF): Boolean { return false }

                        override fun onDoubleTapEvent(pointF: PointF): Boolean { return false }

                        override fun onPinchLocked() {}

                        override fun onPinchZoomEvent(v: Float, pointF: PointF): Boolean { return false }

                        override fun onRotateLocked() {}

                        override fun onRotateEvent(v: Float): Boolean { return false }

                        override fun onTiltEvent(v: Float): Boolean { return false }

                        override fun onLongPressEvent(pointF: PointF): Boolean { return false }

                        override fun onLongPressRelease() {}

                        override fun onTwoFingerTapEvent(pointF: PointF): Boolean { return false }
                    }
                )

                setMarkersVisible()
            }
        }
    }

    fun setMarkersVisible() {
        if(map!!.zoomLevel < 15.0) {
            listMarkers.forEach {
                it.isVisible = false
            }
        } else {
            listMarkers.forEach {
                it.isVisible = true
            }
        }
    }

    private fun subscribe() {
        GetMapActionsTask.actionsMapObserver.subscribe(object : Observer<ArrayList<ActionDemo>> {
            override fun onComplete() {}

            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: ArrayList<ActionDemo>) {
                actions.addAll(t.filter {listAction ->
                    actions.forEach {
                        if(it.id == listAction.id) return@filter false
                    }
                    addMapMarker(fragment.context!!, listAction)
                    return@filter true
                })
            }

            override fun onError(e: Throwable) {}

        })
    }

    private fun addMapMarker(context: Context, action: ActionDemo) {

        GetCategoriesTask.categories.forEach {
            if(action.category == it.id) {
                Glide.with(fragment)
                    .asBitmap()
                    .load(Server.url + "/images/get/" + it.image)
                    .circleCrop()
                    .into(object : CustomTarget<Bitmap>() {

                        override fun onLoadCleared(placeholder: Drawable?) {}

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            val markerImage = Image()
                            markerImage.bitmap = createViewMarker(context, resource, action)

                            val marker = MapMarker(GeoCoordinate(action.latitude!!, action.longitude!!), markerImage)
                            marker.anchorPoint = PointF(105f, 230f)

                            marker.isVisible = map!!.zoomLevel > 15.0

                            listMarkers.add(marker)
                            listActionsDemo.add(action)

                            if(map != null) {
                                map!!.addMapObject(marker)
                            }
                        }
                    })
            }
        }
    }

    companion object {

        private const val checkedRadius = 0.8

        private val actions = ArrayList<ActionDemo>()

        private val listMarkers = ArrayList<MapMarker>()
        private val listActionsDemo = ArrayList<ActionDemo>()

        private val checkedPoints = ArrayList<GeoCoordinate>()

        fun createViewMarker(context: Context, bitmap: Bitmap, action: ActionDemo): Bitmap {
            val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.map_marker, null)
            view.map_marker_title.text = action.title
            view.map_marker_image.setImageBitmap(bitmap)
            view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            val result = Bitmap.createBitmap(
                view.measuredWidth,
                view.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val c = Canvas(result)
            view.draw(c)
            return result
        }
    }
}