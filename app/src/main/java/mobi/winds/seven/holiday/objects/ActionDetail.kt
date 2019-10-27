package mobi.winds.seven.holiday.objects

import org.json.JSONObject

class ActionDetail (
    var id: Long,
    var actionId: Long,
    var address: String,
    var phone: String,
    var workTime: Map<String, Map<String, String>>,
    var site: String?,
    var information: String?,
    var socialNetworks: Map<String, String?>,
    var createTime: Long
) {

    companion object {

        fun fromJSON(json: JSONObject): ActionDetail {
            val workTime = mutableMapOf<String, Map<String, String>>()
            json.getJSONObject("workTime").keys().forEach {keyDay ->
                val day = mutableMapOf<String, String>()
                json.getJSONObject("workTime").getJSONObject(keyDay).keys().forEach {
                    day[it] = json.getJSONObject("workTime").getJSONObject(keyDay).getString(it)
                }
                workTime[keyDay] = day
            }

            val socialNetworks = mutableMapOf<String, String?>()
            json.getJSONObject("socialNetworks").keys().forEach {
                socialNetworks[it] = if(json.getJSONObject("socialNetworks").isNull(it)) {
                    null
                } else {
                    json.getJSONObject("socialNetworks").getString(it)
                }
            }

            return ActionDetail(
                json.getLong("id"),
                json.getLong("actionId"),
                json.getString("address"),
                json.getString("phone"),
                workTime,
                if(json.isNull("site")) null else json.getString("site"),
                if(json.isNull("information")) null else json.getString("information"),
                socialNetworks,
                json.getLong("createTime")
            )
        }
    }
}