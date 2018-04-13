package com.zzz.www.smartdevice.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * @author zzz
 * @date create at 2018/1/4.
 */
class PrefsUtil private constructor(){
    companion object {
        private val PREFERENCE_FILE_NAME : String = "smart_device.xml"
        private val HISTORY_KEY : String = "history"
        private val defaultSet : HashSet<String> = HashSet()
        private lateinit var preference : SharedPreferences
        @Volatile private var instance : PrefsUtil? = null

        fun getInstance(context: Context) : PrefsUtil =
                instance ?: synchronized(this) {
                    instance ?: PrefsUtil().also {
                        this.preference = context.getSharedPreferences(PREFERENCE_FILE_NAME,
                                Context.MODE_PRIVATE)
                        instance = it
                    }
                }
    }

    fun putStringSet(set : HashSet<String>) =
            preference.edit().putStringSet(HISTORY_KEY, set).apply()


    fun getStringSet() : HashSet<String> =
            preference.getStringSet(HISTORY_KEY, defaultSet) as HashSet<String>
}