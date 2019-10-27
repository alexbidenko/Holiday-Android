package mobi.winds.seven.holiday.objects

import org.json.JSONObject

class Category (
    val id: Long,
    val title: String,
    val image: String
) {

    companion object {
        fun fromJSON(json: JSONObject): Category {
            return Category(
                json.getLong("id"),
                json.getString("title"),
                json.getString("image")
            )
        }
    }
}