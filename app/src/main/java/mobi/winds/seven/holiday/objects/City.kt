package mobi.winds.seven.holiday.objects

import org.json.JSONObject

class City (
    val city: String,
    val latitude: Double,
    val longitude: Double
) {

    companion object {

        fun fromJSON(json: JSONObject): City {
            return City(
                json.getString("city"),
                json.getDouble("latitude"),
                json.getDouble("longitude")
            )
        }
    }
}