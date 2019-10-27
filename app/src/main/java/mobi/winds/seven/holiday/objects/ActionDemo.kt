package mobi.winds.seven.holiday.objects

import org.json.JSONObject

class ActionDemo (
    val id: Long,
    val title: String,
    val profit: Float,
    val oldCost: Float?,
    val newCost: Float,
    val category: Long,
    val subCategory: Long,
    val image: String,
    val latitude: Double?,
    val longitude: Double?,
    val interesting: Boolean,
    val categoryTop: Boolean,
    val actionBefore: Long
) {

    companion object {

        fun fromJSON(json: JSONObject): ActionDemo {
            return ActionDemo(
                json.getLong("id"),
                json.getString("title"),
                json.getDouble("profit").toFloat(),
                if(!json.isNull("oldCost")) json.getDouble("oldCost").toFloat() else null,
                json.getDouble("newCost").toFloat(),
                json.getLong("category"),
                json.getLong("subCategory"),
                json.getString("image"),
                json.getDouble("latitude"),
                json.getDouble("longitude"),
                json.getBoolean("interesting"),
                json.getBoolean("categoryTop"),
                json.getLong("actionBefore")
            )
        }
    }
}