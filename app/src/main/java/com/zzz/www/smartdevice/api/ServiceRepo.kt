package com.zzz.www.smartdevice.api

import com.zzz.www.smartdevice.bean.*
import retrofit2.Retrofit
import rx.Observable

/**
 * @author zzz
 * @date create at 2018/1/3.
 */
class ServiceRepo private constructor() {

    companion object {
        private var client : Retrofit? = null
        private var repo : ServiceRepo? = null

        fun getInstance() : ServiceRepo =
                repo ?: synchronized(this) {
                    repo ?: ServiceRepo().also { getOrCreateRetrofitInstance() }
                }

        private fun getOrCreateRetrofitInstance() : Retrofit =
            client ?: synchronized(this) {
                client ?: HttpClient.getClient().also { client = it }
            }
    }

    fun getToken(tokenRequest: TokenRequest) : Observable<Token>? =
            client?.create(ServiceApi::class.java)?.getToken(tokenRequest)

    fun getDeviceInfoBySN(token: String, cid: String, deviceInfoRequest: DeviceInfoRequest) :
            Observable<BaseResponseBody<DeviceInfo>>? =
            client?.create(ServiceApi::class.java)?.getDeviceInfoBySN(token, cid, deviceInfoRequest)
}