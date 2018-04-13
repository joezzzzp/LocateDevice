package com.zzz.www.smartdevice.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zzz.www.smartdevice.bean.User

/**
 * @author zzz
 * @date create at 2018/1/4.
 */
class PrefsUtil private constructor() {
  companion object {
    private const val PREFERENCE_FILE_NAME: String = "smart_device.xml"
    private const val HISTORY_KEY: String = "history"
    private const val AUTO_LOGIN_KEY: String = "auto_log_in"
    private const val ME_USER_KEY: String = "me_user"

    private val defaultSet: HashSet<String> = HashSet()
    private lateinit var preference: SharedPreferences
    private val gson = Gson()
    @Volatile
    private var instance: PrefsUtil? = null

    fun getInstance(context: Context): PrefsUtil =
      instance ?: synchronized(this) {
        instance ?: PrefsUtil().also {
          this.preference = context.getSharedPreferences(PREFERENCE_FILE_NAME,
            Context.MODE_PRIVATE)
          instance = it
        }
      }
  }

  fun putStringSet(set: HashSet<String>) =
    preference.edit().putStringSet(HISTORY_KEY, set).apply()

  fun getStringSet(): HashSet<String> =
    preference.getStringSet(HISTORY_KEY, defaultSet) as HashSet<String>

  fun setObject(key: String, any: Any) =
    preference.edit().putString(key, gson.toJson(any)).apply()

  fun <T> getObject(key: String, clazz: Class<T>): T =
    gson.fromJson(preference.getString(key, ""), clazz)

  fun setAutoLogIn(auto : Boolean = false) =
    preference.edit().putBoolean(AUTO_LOGIN_KEY, auto).apply()

  fun saveMe(me: User) {
    setObject(ME_USER_KEY, me)
  }

  fun getMe(): User? {
    return getObject(ME_USER_KEY, User::class.java)
  }
}