package com.zzz.www.smartdevice.bean

import com.google.gson.annotations.SerializedName

/**
 * @author zzz
 * @date create at 2018/1/4.
 */
data class DeviceInfoRequest(@SerializedName("deviceSn") var deviceSn: String) {
    @SerializedName("fieldId") var fieldId: String? = null
}