package com.zzz.www.smartdevice.bean

import com.google.gson.annotations.SerializedName
import com.zzz.www.smartdevice.api.HttpConfig

/**
 * @author zzz
 * @date create at 2018/1/3.
 */
data class TokenRequest(
        @SerializedName("corporateId") var corporateId : String = HttpConfig.cid,
        @SerializedName("corporatePasswd") var corporatePasswd : String = HttpConfig.password)