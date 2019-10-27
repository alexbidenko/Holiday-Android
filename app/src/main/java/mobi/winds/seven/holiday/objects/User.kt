package mobi.winds.seven.holiday.objects

import org.json.JSONObject

class User (
    var name: String,
    var login: String,
    var email: String,
    var phone: String,
    var birthday: Long,
    var password: String
) {

    companion object {
        fun fromJSON(json: JSONObject): User {
            return User(
                json.getString("name"),
                json.getString("login"),
                json.getString("email"),
                json.getString("phone"),
                json.getLong("birthday"),
                json.getString("password")
            )
        }

        fun toJSON(user: User): JSONObject {
            return JSONObject()
                .put("name", user.name)
                .put("login", user.login)
                .put("email", user.email)
                .put("phone", user.phone)
                .put("birthday", user.birthday)
                .put("password", user.password)
        }
    }
}