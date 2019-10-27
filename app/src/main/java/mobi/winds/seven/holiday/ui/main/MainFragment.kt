package mobi.winds.seven.holiday.ui.main

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.main_category.view.*
import kotlinx.android.synthetic.main.main_fragment.view.*
import kotlinx.android.synthetic.main.main_sub_category.view.*
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.R
import mobi.winds.seven.holiday.datas.Server
import mobi.winds.seven.holiday.objects.ActionDemo
import mobi.winds.seven.holiday.objects.Category
import mobi.winds.seven.holiday.objects.SubCategory
import mobi.winds.seven.holiday.tasks.GetActionsTask
import mobi.winds.seven.holiday.tasks.GetCategoriesTask
import mobi.winds.seven.holiday.ui.profile.ProfileFragment
import mobi.winds.seven.holiday.ui.search.SearchFragment
import mobi.winds.seven.holiday.ui.settings.SettingsFragment
import mobi.winds.seven.holiday.ui.map.MapFragment

class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private var subCategories = ArrayList<SubCategory>()
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sPref = context!!.getSharedPreferences(MainActivity.APP_PREFERENCES, Context.MODE_PRIVATE)

        if(userLatitude.toFloat() != sPref.getFloat(MainActivity.KEY_USER_LATITUDE, 0f) ||
            userLongitude.toFloat() != sPref.getFloat(MainActivity.KEY_USER_LONGITUDE, 0f)) {

            GetActionsTask.page = 0
            GetActionsTask.isAllActions = false
            GetActionsTask.actionsObserver.cleanupBuffer()

            userLatitude = sPref.getFloat(MainActivity.KEY_USER_LATITUDE, 0f).toDouble()
            userLongitude = sPref.getFloat(MainActivity.KEY_USER_LONGITUDE, 0f).toDouble()

            view.main_actions.adapter?.notifyDataSetChanged()
        }

        if(GetActionsTask.page == 0) {
            GetActionsTask.getActions(
                userLatitude,
                userLongitude,
                MainActivity.radiusGetActions
            )
        }

        view.main_actions.layoutManager = LinearLayoutManager(context)
        view.main_actions.adapter = MainActionsAdapter()
        actions.clear()

        view.main_actions.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if((recyclerView.layoutManager as LinearLayoutManager).itemCount <=
                    (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() + 4 &&
                        !GetActionsTask.isAllActions) {
                    MainActionsAdapter.isLoad = true
                    view.main_actions.adapter?.notifyDataSetChanged()
                    GetActionsTask.getActions(
                        sPref.getFloat(MainActivity.KEY_USER_LATITUDE, 0f).toDouble(),
                        sPref.getFloat(MainActivity.KEY_USER_LONGITUDE, 0f).toDouble(),
                        MainActivity.radiusGetActions
                    )
                }
            }
        })

        view.main_sub_categories_category_image.setOnClickListener {
            view.main_sub_categories.visibility = View.GONE
            view.main_categories.visibility = View.VISIBLE

            MainActionsAdapter.categoryFilter = -1L
            MainActionsAdapter.subCategoryFilter = -1L

            view.main_actions.adapter?.notifyDataSetChanged()
        }

        if(GetCategoriesTask.categories.isEmpty()) {
            GetCategoriesTask.categoriesObserver.cleanupBuffer()
            GetCategoriesTask.getCategories()
            GetCategoriesTask.categoriesObserver.subscribe(object : Observer<ArrayList<Category>> {
                override fun onComplete() {}

                override fun onSubscribe(d: Disposable) {}

                override fun onNext(list: ArrayList<Category>) {
                    activity?.runOnUiThread {
                        createCategories(list, view)
                    }
                }

                override fun onError(e: Throwable) {}
            })

            GetCategoriesTask.subCategoriesObserver.cleanupBuffer()
            GetCategoriesTask.getSubCategories()
            GetCategoriesTask.subCategoriesObserver.subscribe(object : Observer<ArrayList<SubCategory>> {
                override fun onComplete() {}

                override fun onSubscribe(d: Disposable) {}

                override fun onNext(list: ArrayList<SubCategory>) {
                    activity?.runOnUiThread {
                        createSubCategories(list, view)
                    }
                }

                override fun onError(e: Throwable) {}
            })
        } else {
            createCategories(GetCategoriesTask.categories, view)
            createSubCategories(GetCategoriesTask.subCategories, view)
        }

        val userData = sPref.getString(MainActivity.KEY_USER_DATA, null)
        if(userData == null) {
            view.main_menu_button.setOnClickListener {
                MainActionsAdapter.categoryFilter = -1L
                MainActionsAdapter.subCategoryFilter = -1L

                MainActivity.get.changeFragment(SettingsFragment())
            }
        } else {
            view.main_menu_button.setOnClickListener {
                MainActionsAdapter.categoryFilter = -1L
                MainActionsAdapter.subCategoryFilter = -1L

                MainActivity.get.changeFragment(ProfileFragment())
            }
        }

        GetActionsTask.actionsObserver.subscribe(object : Observer<ArrayList<ActionDemo>> {
            override fun onComplete() {}

            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: ArrayList<ActionDemo>) {
                actions.addAll(t.filter {tAction ->
                    actions.forEach {
                        if(it.id == tAction.id) return@filter false
                    }
                    return@filter true
                })
                activity?.runOnUiThread {
                    view.main_swipe_refresh.isRefreshing = false
                    view.main_actions.adapter?.notifyDataSetChanged()
                }
            }

            override fun onError(e: Throwable) {}
        })

        view.main_search.setOnClickListener {
            MainActionsAdapter.categoryFilter = -1L
            MainActionsAdapter.subCategoryFilter = -1L

            MainActivity.get.changeFragment(SearchFragment())
        }

        view.main_floating_button.setOnClickListener {
            MapFragment.actionToOpen = null
            MainActivity.get.changeFragment(MainActivity.mapFragment)
        }

        view.main_swipe_refresh.setColorSchemeResources(
            R.color.colorAccent,
            android.R.color.holo_green_light,
            android.R.color.holo_blue_dark,
            android.R.color.holo_red_light
        )
        view.main_swipe_refresh.setOnRefreshListener {
            GetActionsTask.page = 0
            GetActionsTask.isAllActions = false
            GetActionsTask.isCanDoRequest = true
            GetActionsTask.actionsObserver.cleanupBuffer()
            actions.clear()
            GetActionsTask.getActions(
                userLatitude,
                userLongitude,
                MainActivity.radiusGetActions
            )

            Handler().postDelayed(
                {
                    view.main_swipe_refresh.isRefreshing = false
                },
                10 * 1000
            )
        }
    }

    override fun onPause() {
        super.onPause()

        MainActionsAdapter.categoryFilter = -1L
        MainActionsAdapter.subCategoryFilter = -1L
    }

    override fun onResume() {
        super.onResume()
        val actionsCash = ArrayList<ActionDemo>()
        actionsCash.addAll(actions.filter {
            return@filter checkInRadius(it)
        })

        actions.clear()
        actions.addAll(actionsCash)

        view!!.main_actions.adapter?.notifyDataSetChanged()
    }

    private fun createCategories(list: List<Category>, view: View) {
        view.main_categories.removeAllViews()
        val newList = ArrayList<Category>()
        newList.addAll(list.filter {
            for(i in 0 until list.size) {
                if(list[i].id == it.id && list[i] != it) return@filter false
                else if(list[i] == it) return@filter true
            }
            return@filter true
        })
        newList.forEach { c ->
            val category =
                activity!!.layoutInflater.inflate(R.layout.main_category, view.main_categories, false)
            category.main_category_title.text = c.title
            Glide.with(category.main_category_image)
                .load(Server.url + "/images/get/" + c.image)
                .circleCrop()
                .into(category.main_category_image)

            category.setOnClickListener {
                if(view.main_sub_categories.childCount > 1) {
                    Glide.with(view.main_sub_categories_category_image)
                        .load(Server.url + "/images/get/" + c.image)
                        .circleCrop()
                        .into(view.main_sub_categories_category_image)

                    for (i in 0 until subCategories.size) {
                        if(view.main_sub_categories.childCount > i + 1) {
                            view.main_sub_categories.getChildAt(i + 1).main_sub_category_card.setCardBackgroundColor(
                                Color.WHITE
                            )
                            view.main_sub_categories.getChildAt(i + 1).visibility =
                                if (subCategories[i].categoryId == c.id) {
                                    View.VISIBLE
                                } else {
                                    View.GONE
                                }
                        }
                    }

                    view.main_categories.visibility = View.GONE
                    view.main_sub_categories.visibility = View.VISIBLE

                    MainActionsAdapter.categoryFilter = c.id
                    MainActionsAdapter.subCategoryFilter = -1L

                    view.main_actions.adapter?.notifyDataSetChanged()
                }
            }

            view.main_categories.addView(category)
        }
    }

    private fun createSubCategories(list: List<SubCategory>, view: View) {
        subCategories.clear()
        view.main_sub_categories.removeViews(1, view.main_sub_categories.childCount - 1)
        subCategories.addAll(list.filter {
            for(i in 0 until list.size) {
                if(list[i].id == it.id && list[i] != it) return@filter false
                else if(list[i] == it) return@filter true
            }
            return@filter true
        })
        subCategories.forEach {
            val subCategory = activity!!.layoutInflater.inflate(
                R.layout.main_sub_category,
                view.main_sub_categories,
                false
            )
            subCategory.main_sub_category_title.text = it.title

            subCategory.setOnClickListener { _ ->
                for (i in 0 until subCategories.size) {
                    if(view.main_sub_categories.childCount > i + 1) {
                        view.main_sub_categories.getChildAt(i + 1).main_sub_category_card.setCardBackgroundColor(Color.WHITE)
                    }
                }
                if(MainActionsAdapter.subCategoryFilter != it.id) {
                    subCategory.main_sub_category_card.setCardBackgroundColor(Color.parseColor("#c069d5"))

                    MainActionsAdapter.subCategoryFilter = it.id
                } else {
                    MainActionsAdapter.subCategoryFilter = -1L
                }

                view.main_actions.adapter?.notifyDataSetChanged()
            }

            view.main_sub_categories.addView(subCategory)
        }
    }

    private fun checkInRadius(actionDemo: ActionDemo): Boolean {
        return actionDemo.latitude!! > userLatitude - MainActivity.radiusGetActions &&
                actionDemo.latitude < userLatitude + MainActivity.radiusGetActions &&
                actionDemo.longitude!! > userLongitude - MainActivity.radiusGetActions &&
                actionDemo.longitude < userLongitude + MainActivity.radiusGetActions
    }

    companion object {

        val actions = ArrayList<ActionDemo>()
    }
}
