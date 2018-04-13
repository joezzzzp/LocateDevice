package com.zzz.www.smartdevice.main.show

import android.content.Context
import com.zzz.www.smartdevice.api.HttpConfig
import com.zzz.www.smartdevice.api.ZYFRepo
import com.zzz.www.smartdevice.bean.DateData
import com.zzz.www.smartdevice.bean.DateDataRequest
import com.zzz.www.smartdevice.bean.DateDataResponse
import com.zzz.www.smartdevice.bean.DeviceInfo
import com.zzz.www.smartdevice.utils.Util
import rx.Subscriber

/**
 * @author zzz
 * @date create at 2018/5/22.
 */
class DeviceDetailsPresenter(view: DeviceDetailsContract.View) : DeviceDetailsContract.Presenter(view) {

  private var context: Context = view as Context

  override fun getHistory(sn: String, beginDate: Long, endDate: Long) {
    view.showLoading()
    ZYFRepo.get().getDateData(HttpConfig.token, HttpConfig.corporateId, DateDataRequest().apply {
      this.deviceSn = sn
      this.endDate = endDate
      this.beginDate = beginDate
    }).subscribe(object : Subscriber<DateDataResponse>() {
      override fun onNext(t: DateDataResponse?) {
        t?.run {
          if (respCode == HttpConfig.successCode) {
            val data = t.t
            if (data.isNotEmpty()) {
              val deviceInfos = arrayListOf<DeviceInfo>()
              data.iterator().run {
                while (hasNext()) {
                  val item = next()
                  deviceInfos.add(DeviceInfo().apply {
                    this.sn = item.deviceNum
                    this.time = item.collectDate
                    this.data = Util.dataItem2InfoItem(item.items)
                  })
                }
                view.getHistoryResult(true, deviceInfos)
                return
              }
            }
          }
        }
        view.getHistoryResult(false, arrayListOf())
      }

      override fun onCompleted() {
        view.hideLoading()
      }

      override fun onError(e: Throwable?) {
        view.hideLoading()
        view.getHistoryResult(false, arrayListOf())
      }

    })
  }
}