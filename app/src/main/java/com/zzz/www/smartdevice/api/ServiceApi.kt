package com.zzz.www.smartdevice.api

import com.zzz.www.smartdevice.bean.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import rx.Observable

/**
 * @author zzz
 * @date create at 2018/1/2.
 */
interface ServiceApi {
    @POST("auth/getToken")
    fun getToken(@Body tokenRequest: TokenRequest) : Observable<Token>

    @POST("serviceSum/deviceDataNew/{cid}")
    fun getDeviceInfoBySN(@Header("token") token: String,
                          @Path("cid") cid: String,
                          @Body deviceInfoRequest: DeviceInfoRequest) :
            Observable<BaseResponseBody<DeviceInfo>>
}