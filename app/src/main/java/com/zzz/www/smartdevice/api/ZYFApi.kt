package com.zzz.www.smartdevice.api

import com.zzz.www.smartdevice.bean.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import rx.Observable

/**
 * @author zzz
 * @date create at 2018/5/10.
 */
interface ZYFApi {
  @POST("auth/getToken")
  fun getToken(@Body request: TokenRequest): Observable<TokenResponse>

  @POST("service/getByDate/{cid}")
  fun getDateDataBySn(@Header("token") token: String, @Path("cid") cooperateID: String,
                      @Body request: DateDataRequest):
    Observable<DateDataResponse>
}