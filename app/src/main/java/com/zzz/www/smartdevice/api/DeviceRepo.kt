package com.zzz.www.smartdevice.api

import com.zzz.www.smartdevice.bean.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author zzz
 * @date create at 2018/5/10.
 */
class DeviceRepo private constructor() {
  private val api = HttpClient.getClient().create(DeviceApi::class.java)

  companion object {
    private val instance: DeviceRepo by lazy (LazyThreadSafetyMode.SYNCHRONIZED ) { DeviceRepo() }

    fun get(): DeviceRepo = instance
  }

  private fun <T> schedulers(): Observable.Transformer<T, T> =
    Observable.Transformer { t -> t!!.subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread()) }

  fun getDeviceSumInfo(sn: String): Observable<DeviceInfoResponse> =
    api.getDeviceSumInfo(sn).compose(schedulers())

  fun getDeviceHistorySumInfo(sn: String, skip: Long, limit: Int): Observable<List<DeviceInfo>> =
    api.getDeviceHistorySumInfo(sn, skip, limit).compose(schedulers())

  fun updateNewDate(sn: String, newTimeStamp: Long?): Observable<CommonResponse> =
    api.updateNewDate(sn, newTimeStamp).compose(schedulers())
}