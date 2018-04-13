package com.zzz.www.smartdevice.main.show

import android.content.Context
import com.zzz.www.smartdevice.bean.Device
import com.zzz.www.smartdevice.bean.DeviceInfo
import com.zzz.www.smartdevice.db.DataRepo
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author zzz
 * @date create at 2018/5/22.
 */
class DeviceDetailsPresenter(view: DeviceDetailsContract.View) : DeviceDetailsContract.Presenter(view) {

  private var context: Context = view as Context

  override fun getHistory(sn: String, beginDate: Long, endDate: Long) {
    view.showLoading()
    Observable.unsafeCreate(Observable.OnSubscribe<ArrayList<DeviceInfo>> { t ->
      val histories = DataRepo.getInstance(context).findHistories(Device(sn = sn), beginDate, endDate)
      t?.onNext(histories)
      t?.onCompleted()
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
      .subscribe(object : Subscriber<ArrayList<DeviceInfo>>() {
        override fun onNext(t: ArrayList<DeviceInfo>?) {
          t?.run {
            view.getHistoryResult(true, t)
            return
          }
          view.getHistoryResult(false, arrayListOf())
        }

        override fun onCompleted() {
          view.hideLoading()
        }

        override fun onError(e: Throwable?) {
          view.getHistoryResult(false, arrayListOf())
          view.hideLoading()
        }

      })
  }
}