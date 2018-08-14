package com.zzz.www.smartdevice.api

import com.zzz.www.smartdevice.bean.*
import retrofit2.http.*
import rx.Observable

/**
 * @author zzz
 * @date create at 2018/5/10.
 */
interface DeviceApi {

  @GET("info")
  fun getDeviceSumInfo(@Query("sn") sn: String): Observable<DeviceInfoResponse>

  @GET("history")
  fun getDeviceHistorySumInfo(@Query("sn") sn: String, @Query("skip") skip: Long,
                              @Query("limit") limit: Int):
    Observable<List<DeviceInfo>>

  @GET("updateDate")
  fun updateNewDate(@Query("sn") sn: String,
                    @Query("newTimeStamp") newTimeStamp: Long?): Observable<CommonResponse>

}