package com.zzz.www.smartdevice.bean

import com.google.gson.annotations.SerializedName

/**
 * @author zzz
 * @date create at 2018/1/3.
 */
data class DeviceInfo(@SerializedName("deviceNum") var deviceNum : String) {

    @SerializedName("collectDate")
    var collectDate: Long = 0

    @SerializedName("deviceStatus")
    var deviceStatus: String? = null

    @SerializedName("deviceVersion")
    var deviceVersion: String? = null

    @SerializedName("deviceVersionSid")
    var deviceVersionSid: String? = null

    @SerializedName("onlineStatus")
    var onlineStatus: String? = null

    @SerializedName("productVersion")
    var productVersion: String? = null

    @SerializedName("productVersionSid")
    var productVersionSid: String? = null

    @SerializedName("updating")
    var updating: Boolean = false

    @SerializedName("items")
    var items: List<DevicePropertyItem>? = null
}