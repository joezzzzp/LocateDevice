package com.zzz.www.smartdevice.api

import com.zzz.www.smartdevice.bean.DateDataRequest
import com.zzz.www.smartdevice.bean.DateDataResponse
import com.zzz.www.smartdevice.bean.TokenRequest
import com.zzz.www.smartdevice.bean.TokenResponse
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author zzz
 * @date create at 2018/5/10.
 */
class ZYFRepo private constructor() {
  private val api = HttpClient.getClient().create(ZYFApi::class.java)

  companion object {
    private val instance: ZYFRepo by lazy (LazyThreadSafetyMode.SYNCHRONIZED ) { ZYFRepo() }

    fun get(): ZYFRepo = instance
  }

  private fun <T> schedulers(): Observable.Transformer<T, T> =
    Observable.Transformer { t -> t!!.subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread()) }

  fun getToken(tokenRequest: TokenRequest): Observable<TokenResponse> =
    api.getToken(tokenRequest).compose(schedulers())

  fun getDateData(token: String, cooperateId: String, dateDataRequest: DateDataRequest):
    Observable<DateDataResponse> =
    api.getDateDataBySn(token, cooperateId, dateDataRequest).compose(schedulers())
}