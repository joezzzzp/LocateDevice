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
    abstract fun fillGroupData(groupDevices: MutableMap<Group, ArrayList<Device>>)
    abstract fun addGroup(groupName: String)
    abstract fun deleteGroup(group: Group)
    abstract fun queryDeviceByRestApi(sn: String)
    abstract fun queryAllDeviceByRestApi(groupDevices: Map<Group, ArrayList<Device>>)
    abstract fun importFile(path: String)
  }

  interface View : BaseView {
    fun fillGroupDataResult(success: Boolean)
    fun addGroupResult(success: Boolean, message: String, group: Group?)
    fun deleteGroupResult(success: Boolean, message: String, group: Group)
    fun queryDeviceResult(success: Boolean, deviceInfo: DeviceInfo)
    fun queryAllDevicesResult(success: Boolean)
    fun getTokenResult(success: Boolean)
    fun importFileResult(success: Boolean)
  }
}