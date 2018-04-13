package com.zzz.www.smartdevice.bean

import com.google.gson.annotations.SerializedName

/**
 * @author zzz
 * @date create at 2018/1/3.
 */
data class Token(@SerializedName("token") var token : String = "") {
    @SerializedName("respCode")
    var respCode : String = ""

    @SerializedName("respMessage")
    var respMessage : String = ""

    @SerializedName("expireTime")
    var expireTime : Long = 0
}