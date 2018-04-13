package com.zzz.www.smartdevice.main.query

import android.content.Context
import com.tckj.zyfsdk.ZYFSdk
import com.tckj.zyfsdk.entity.BaseEntity
import com.tckj.zyfsdk.entity.DeviceDetailsEntity
import com.tckj.zyfsdk.http.zhttp.listener.ZYFBindDeviceListener
import com.tckj.zyfsdk.http.zhttp.listener.ZYFGetBindDeviceListener
import com.zzz.www.smartdevice.api.HttpConfig
import com.zzz.www.smartdevice.api.ZYFRepo
import com.zzz.www.smartdevice.bean.*
import com.zzz.www.smartdevice.db.DataRepo
import com.zzz.www.smartdevice.main.DeviceApplication
import com.zzz.www.smartdevice.utils.Util
import kotlinx.android.parcel.RawValue
import rx.Subscriber
import java.lang.Exception


/**
 * @author zzz
 * @date create at 2018/1/3.
 */
class GroupPresenter(view: GroupContract.View) : GroupContract.Presenter(view) {
  private val period: Long = 30L * 24L * 60L * 60L * 1000L
  private var context: Context = view as Context

  override fun getGroups() {
    val groups = DataRepo.getInstance(context).getAllGroup()
    view.getGroupResult(groups.isNotEmpty(), groups)
  }

  override fun addGroup(groupName: String) {
    val ret = DataRepo.getInstance(context).addGroup(Group().apply { name = groupName })
    view.addGroupResult(ret.valid(), if (ret.valid()) "" else "", ret)
  }
  override fun deleteGroup(group: Group) {
    val ret = DataRepo.getInstance(context).deleteGroup(group.id)
    view.deleteGroupResult(ret, if (ret) "" else "", group)
  }

  override fun logOut() {
    DeviceApplication.instance.logout()
    view.logoutResult(true, "")
  }

  override fun queryDevice(deviceId: String) {
    ZYFSdk.getInstance().bindDevice(context, deviceId, DeviceApplication.instance.customerID, object : ZYFBindDeviceListener {
      override fun onComplete(entity: BaseEntity?) {
        ZYFSdk.getInstance().getBindDeviceDetails(context, deviceId, object : ZYFGetBindDeviceListener {
          override fun onComplete(details: DeviceDetailsEntity?) {
            details?.run {
              if (isRtState) {
                saveDevice(deviceId)
                val temp = DeviceInfo().apply {
                  sn = deviceId
                  time = System.currentTimeMillis()
                  data = Util.dataItem2InfoItem(details.rtData.records as
                    ArrayList<DeviceDetailsEntity.DeviceDetails.RecordsBean>)
                }
                val deviceInfo = DataRepo.getInstance(context).addDeviceInfo(temp)
                view.queryDeviceResult(deviceInfo.valid(), deviceInfo)
              } else {
                view.queryDeviceResult(false, DeviceInfo())
              }
            }
          }

          override fun onError(exception: Exception?) {
            view.queryDeviceResult(false, DeviceInfo())
          }
        })
      }

      override fun onError(exception: Exception?) {
        view.queryDeviceResult(false, DeviceInfo())
      }
    })
  }

  fun queryDeviceByRestApi(sn: String) {
    view.showLoading()
    ZYFRepo.get().getDateData(HttpConfig.token, HttpConfig.corporateId, DateDataRequest().apply {
      this.deviceSn = sn
      this.endDate = System.currentTimeMillis()
      this.beginDate = this.endDate - period
    }).subscribe(object : Subscriber<DateDataResponse>() {
      override fun onNext(t: DateDataResponse?) {
        t?.run {
          if (respCode == HttpConfig.successCode) {
            saveDevice(sn)
            val data = t.t
            if (data.isNotEmpty()) {
              view.queryDeviceResult(true, DeviceInfo().apply {
                this.sn = sn
                this.time = data[0].collectDate
                this.data = Util.dataItem2InfoItem(data[0].items)
              })
              return
            }
          }
        }
        view.queryDeviceResult(false, DeviceInfo())
      }

      override fun onCompleted() {
        view.hideLoading()
      }

      override fun onError(e: Throwable?) {
        view.hideLoading()
        view.queryDeviceResult(false, DeviceInfo())
      }

    })
  }

  fun saveDevice(sn: String) {
    val device = DataRepo.getInstance(context).findDevice(Device().apply {
      this.sn = sn
    })
    if (!device.valid()) {
      DataRepo.getInstance(context).addDevice(Device().apply {
        this.sn = sn
        this.name = ""
        this.user = DeviceApplication.instance.me.id
        this.group = -1L
      })
    }
  }
}
