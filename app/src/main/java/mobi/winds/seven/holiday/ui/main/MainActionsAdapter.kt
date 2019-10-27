package mobi.winds.seven.holiday.ui.main

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.main_action_demo.view.*
import mobi.winds.seven.holiday.R
import mobi.winds.seven.holiday.datas.Server
import mobi.winds.seven.holiday.objects.ActionDemo
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import mobi.winds.seven.holiday.MainActivity
import mobi.winds.seven.holiday.ui.map.MapFragment

class MainActionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.main_actions_header, parent, false)
                HeaderHolder(view)
            }
            TYPE_FOOTER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.main_actions_footer, parent, false)
                FooterHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.main_action_demo, parent, false)
                ActionViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(position == MainFragment.actions.size + 2) {
            (holder as FooterHolder).bind()
        } else if(position > 0) {
            (holder as ActionViewHolder).bind(MainFragment.actions[position - 1])
        }
    }

    override fun getItemCount(): Int {
        return MainFragment.actions.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_HEADER
            MainFragment.actions.size + 2 -> TYPE_FOOTER
            else -> TYPE_BODY
        }
    }

    inner class ActionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(actionDemo: ActionDemo) {
            if (categoryFilter > 0L && categoryFilter != actionDemo.category ||
                subCategoryFilter > 0L && subCategoryFilter != actionDemo.subCategory
            ) {
                itemView.main_action_layout.layoutParams.height = 0
            } else {
                itemView.main_action_layout.layoutParams =
                    RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                itemView.main_action_demo_title.text = actionDemo.title
                itemView.main_action_demo_old_cost.text = if (actionDemo.oldCost != null) {
                    itemView.main_action_demo_old_cost.visibility = View.VISIBLE
                    itemView.main_action_demo_old_cost.paintFlags =
                        itemView.main_action_demo_old_cost.paintFlags or STRIKE_THRU_TEXT_FLAG
                    "${actionDemo.oldCost.toInt()} \u20BD"
                } else {
                    itemView.main_action_demo_old_cost.visibility = View.GONE
                    ""
                }
                itemView.main_action_demo_new_cost.text = "${actionDemo.newCost.toInt()} \u20BD"
                itemView.main_action_demo_profit.text = "- ${actionDemo.profit.toInt()}%"
                Glide.with(itemView.main_action_demo_image)
                    .load(Server.url + "/images/get/" + actionDemo.image)
                    .placeholder(R.drawable.image_preview)
                    .into(itemView.main_action_demo_image)

                itemView.setOnClickListener {
                    MapFragment.actionToOpen = actionDemo

                    categoryFilter = -1L
                    subCategoryFilter = -1L

                    MainActivity.get.changeFragment(MainActivity.mapFragment)
                }
            }
        }
    }

    inner class HeaderHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class FooterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind() {
            if(isLoad) {
                itemView.main_action_layout.layoutParams =
                    RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            } else {
                itemView.main_action_layout.layoutParams =
                    RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
            }
        }
    }

    companion object {

        var isLoad = false

        var categoryFilter = -1L
        var subCategoryFilter = -1L

        private const val TYPE_HEADER = 1
        private const val TYPE_BODY = 2
        private const val TYPE_FOOTER = 3
    }
}