package com.zzz.www.smartdevice.bean

import com.google.gson.annotations.SerializedName

/**
 * @author zzz
 * @date create at 2018/1/3.
 */
data class BaseResponseBody<T> (@SerializedName("respCode") var respCode : String) {
    @SerializedName("respMessage")
    var respMessage : String = ""

    @SerializedName("t")
    var data : T? = null
}