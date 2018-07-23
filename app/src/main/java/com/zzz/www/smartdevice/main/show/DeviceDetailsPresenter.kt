package com.zzz.www.smartdevice.main.show

import android.content.Context
import com.zzz.www.smartdevice.api.DeviceRepo
import com.zzz.www.smartdevice.bean.CommonResponse
import com.zzz.www.smartdevice.bean.Device
import com.zzz.www.smartdevice.bean.DeviceInfo
import com.zzz.www.smartdevice.db.DataRepo
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author zzz
 * @date create at 2018/5/22.
 */
class DeviceDetailsPresenter(view: DeviceDetailsContract.View) : DeviceDetailsContract.Presenter(view) {
  override fun updateNewDate(sn: String) {
    view.showLoading()
    DeviceRepo.get().updateNewDate(sn).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).
      subscribe(object : Subscriber<CommonResponse>() {
        override fun onNext(t: CommonResponse?) {
          t?.run {
            if (code == 200) {
              data?.run {
                val status = this.status
                val repo = DataRepo.getInstance(view as Context)
                val currentDevice = repo.findDevice(Device(sn = sn))
                if (currentDevice.valid()) {
                  currentDevice.status = status
                  currentDevice.sumInfo = sumInfo
                  repo.updateDevice(currentDevice)
                  if (sumInfo == null) {
                    view.updateNewDateResult(true, null)
                  } else {
                    view.updateNewDateResult(true, sumInfo!!.apply { this.status = status })
                  }
                  return
                }
              }
            }
          }
          view.updateNewDateResult(false, null)
        }

        override fun onCompleted() {
          view.hideLoading()
        }

        override fun onError(e: Throwable?) {
          view.updateNewDateResult(false, null)
        }

      })
  }

  override fun getHistory(sn: String, skip: Long, limit: Int) {
    DeviceRepo.get().getDeviceHistorySumInfo(sn, skip, limit)
      .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
      .subscribe(object : Subscriber<List<DeviceInfo>>() {
        override fun onNext(t: List<DeviceInfo>?) {
          t?.run {
            view.getHistoryResult(true, t)
            return
          }
          view.getHistoryResult(false, arrayListOf())
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable?) {
          view.getHistoryResult(false, arrayListOf())
        }

      })
  }
}