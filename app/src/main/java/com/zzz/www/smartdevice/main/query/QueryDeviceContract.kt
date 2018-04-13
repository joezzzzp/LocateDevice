package com.zzz.www.smartdevice.main.query

import com.zzz.www.smartdevice.base.BasePresenter
import com.zzz.www.smartdevice.base.BaseView
import com.zzz.www.smartdevice.bean.DeviceLocationInfo

/**
 * @author zzz
 * @date create at 2018/1/2.
 */
interface QueryDeviceContract {
    abstract class Presenter(protected var view: View) : BasePresenter() {
        abstract fun getToken()
        abstract fun queryDeviceInfo(serialNumber : String)
    }

    interface View : BaseView {
        fun getTokenSuccess()
        fun getTokenFailed()
        fun queryDeviceSuccess(deviceLocationInfo: DeviceLocationInfo)
        fun queryDeviceFailed(errorMessage : String)
    }
}