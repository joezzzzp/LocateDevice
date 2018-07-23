package com.zzz.www.smartdevice.main.show

import com.zzz.www.smartdevice.base.BasePresenter
import com.zzz.www.smartdevice.base.BaseView
import com.zzz.www.smartdevice.bean.DeviceInfo

/**
 * @author zzz
 * @date create at 2018/5/22.
 */
interface DeviceDetailsContract {
  abstract class Presenter(protected var view: View) : BasePresenter() {
    abstract fun getHistory(sn: String, skip: Long, limit: Int)
    abstract fun updateNewDate(sn: String)
  }

  interface View : BaseView {
    fun getHistoryResult(isSuccess: Boolean, deviceInfo: List<DeviceInfo>)
    fun updateNewDateResult(isSuccess: Boolean, deviceInfo: DeviceInfo?)
  }
}