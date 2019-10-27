package mobi.winds.seven.holiday.objects

import org.json.JSONObject

class SubCategory (
    var id: Long,
    var categoryId: Long,
    var title: String
) {

    companion object {
        fun fromJSON(json: JSONObject): SubCategory {
            return SubCategory(
                json.getLong("id"),
                json.getLong("categoryId"),
                json.getString("title")
            )
        }
    }
}