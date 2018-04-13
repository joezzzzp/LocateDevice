package com.zzz.www.smartdevice.main.query

import com.zzz.www.smartdevice.base.BasePresenter
import com.zzz.www.smartdevice.base.BaseView
import com.zzz.www.smartdevice.bean.Device
import com.zzz.www.smartdevice.bean.DeviceInfo
import com.zzz.www.smartdevice.bean.Group

/**
 * @author zzz
 * @date create at 2018/1/2.
 */
interface GroupContract {
  abstract class Presenter(protected var view: View) : BasePresenter() {
    abstract fun getGroups()
    abstract fun addGroup(groupName: String)
    abstract fun deleteGroup(group: Group)
    abstract fun logOut()
    abstract fun queryDevice(deviceId: String)
  }

  interface View : BaseView {
    fun getGroupResult(success: Boolean, groups: ArrayList<Group>)
    fun addGroupResult(success: Boolean, message: String, group: Group?)
    fun deleteGroupResult(success: Boolean, message: String, group: Group)
    fun logoutResult(success: Boolean, message: String)
    fun queryDeviceResult(success: Boolean, deviceInfo: DeviceInfo)
  }
}