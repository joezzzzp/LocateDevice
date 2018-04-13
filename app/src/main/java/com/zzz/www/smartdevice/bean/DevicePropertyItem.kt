package com.zzz.www.smartdevice.bean

import com.google.gson.annotations.SerializedName

/**
 * @author zzz
 * @date create at 2018/1/3.
 */
data class DevicePropertyItem(@SerializedName("decimalDigit") var decimalDigit : Int) {

    @SerializedName("displayName")
    var displayName: String = ""

    @SerializedName("fieldId")
    var fieldId: String = ""

    @SerializedName("fieldType")
    var fieldType: String = ""

    @SerializedName("fieldUnit")
    var fieldUnit: String = ""

    @SerializedName("hexValues")
    var hexValues: String = ""

    @SerializedName("numValues")
    var numValues: String = ""
}