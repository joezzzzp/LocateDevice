package com.zzz.www.smartdevice.main.query

import android.content.Context
import com.zzz.www.smartdevice.api.HttpConfig
import com.zzz.www.smartdevice.api.ZYFRepo
import com.zzz.www.smartdevice.bean.*
import com.zzz.www.smartdevice.db.DataRepo
import com.zzz.www.smartdevice.utils.Constants
import com.zzz.www.smartdevice.utils.Util
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


/**
 * @author zzz
 * @date create at 2018/1/3.
 */
class GroupPresenter(view: GroupContract.View) : GroupContract.Presenter(view) {

  private val period: Long = 30L * 24L * 60L * 60L * 1000L

  private var context: Context = view as Context
  override fun fillGroupData(groupDevices: MutableMap<Group, ArrayList<Device>>) {
    view.showLoading()
    groupDevices.clear()
    Observable.unsafeCreate (Observable.OnSubscribe<MutableMap<Group, ArrayList<Device>>> {
      val repo = DataRepo.getInstance(context)
      val groups = repo.getAllGroup()
      groups.forEach { group ->
        groupDevices[group] = repo.findGroupDevices(group.id).apply {
          forEach { device ->
            Util.getCurrentDeviceInfo(device, repo.findHistories(device, 0, System.currentTimeMillis()))
            device.currentDeviceInfo?.run {
              val switch4 = Util.findValue(this, Constants.SWITCH_4).toInt()
              if (switch4 == 0) {
                group.status = STATUS_ABNORMAL
                device.status = STATUS_ABNORMAL
              }
            }
          }
        }
      }
      it.onNext(groupDevices)
      it.onCompleted()
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
      .subscribe(object : Subscriber<MutableMap<Group, ArrayList<Device>>>() {
      override fun onNext(t: MutableMap<Group, ArrayList<Device>>?) {
        view.fillGroupDataResult(groupDevices.isNotEmpty())
      }

      override fun onCompleted() {
        view.hideLoading()
      }

      override fun onError(e: Throwable?) {
        view.fillGroupDataResult(false)
        view.hideLoading()
      }

    })

  }

  override fun addGroup(groupName: String) {
    val ret = DataRepo.getInstance(context).addGroup(Group().apply { name = groupName })
    view.addGroupResult(ret.valid(), if (ret.valid()) "" else "", ret)
  }

  override fun deleteGroup(group: Group) {
    val ret = DataRepo.getInstance(context).deleteGroup(group.id)
    view.deleteGroupResult(ret, if (ret) "" else "", group)
  }
  override fun getToken() {
    view.showLoading()
    ZYFRepo.get().getToken(TokenRequest()).subscribe(object : Subscriber<TokenResponse>() {
      override fun onNext(t: TokenResponse?) {
        t?.run {
          if (respCode == HttpConfig.successCode) {
            HttpConfig.token = this.token
            HttpConfig.expireTime = this.expireTime
            view.getTokenResult(true)
            return
          }
          view.getTokenResult(false)
        }
      }

      override fun onCompleted() {
        view.hideLoading()
      }

      override fun onError(e: Throwable?) {
        view.getTokenResult(false)
        view.hideLoading()
      }

    })
  }

  override fun queryDeviceByRestApi(sn: String) {
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

  override fun queryAllDeviceByRestApi(groupDevices: Map<Group, ArrayList<Device>>) {
    view.showLoading()
    val queue : ArrayList<Observable<QueryResult>> = arrayListOf()
    groupDevices.entries.iterator().run {
      while (hasNext()) {
        val item = next()
        item.value.iterator().run {
          while (hasNext()) {
            val device = next()
            queue.add(query(item.key, device))
          }
        }
      }
    }
    Observable.concat(queue).observeOn(AndroidSchedulers.mainThread()).doOnTerminate {
      view.hideLoading()
      view.queryAllDevicesResult(true)
    }.subscribe()
  }

  override fun importFile(path: String) {
    view.showLoading()
    Observable.unsafeCreate(Observable.OnSubscribe<Any> {
      Util.readTextAndImport(context, path)
      it.onNext(true)
      it.onCompleted()
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(object : Subscriber<Any>() {
        override fun onNext(t: Any?) {
          view.importFileResult(true)
        }

        override fun onCompleted() {
          view.hideLoading()
        }

        override fun onError(e: Throwable?) {
          e?.printStackTrace()
          view.importFileResult(false)
          view.hideLoading()
        }

      })

  }

  private fun saveDevice(sn: String) {
    val device = DataRepo.getInstance(context).findDevice(Device().apply {
      this.sn = sn
    })
    if (!device.valid()) {
      DataRepo.getInstance(context).addDevice(Device().apply {
        this.sn = sn
        this.name = ""
        this.group = -1L
      })
    }
  }

  private fun query(group: Group, device: Device) : Observable<QueryResult> {
    val queryTime = System.currentTimeMillis()
    return ZYFRepo.get().getDateData(HttpConfig.token, HttpConfig.corporateId, DateDataRequest().apply {
      this.deviceSn = device.sn
      this.beginDate = device.lastUpdateTime
      this.endDate = queryTime
    }).observeOn(Schedulers.io())
      .map { dateDataResponse -> QueryResult(context, group, device, dateDataResponse, queryTime).updateStatus() }
  }

  private class QueryResult(private val context: Context, private val group: Group,
                    private val device: Device, val data: DateDataResponse?,
                    private val queryTime: Long = 0) {
    fun updateStatus(): QueryResult {
      if (data?.respCode == HttpConfig.successCode) {
        val repo = DataRepo.getInstance(context)
        val d = repo.findDevice(device)
        if (!d.valid()) {
          return this
        }
        val deviceInfos = arrayListOf<DeviceInfo>()
        data.t.forEach {
          deviceInfos.add(DeviceInfo().apply {
            deviceId = d.id
            sn = d.sn
            time = it.collectDate
            data = Util.dataItem2InfoItem(it.items)
          })
        }
        if (deviceInfos.isNotEmpty()) {
          repo.addHistories(deviceInfos)
        }
        device.lastUpdateTime = queryTime
        repo.updateDevice(device)
        val allHistories = repo.findHistories(device, 0, System.currentTimeMillis())
        if (allHistories.isNotEmpty()) {
          Util.getCurrentDeviceInfo(device, allHistories)
          device.currentDeviceInfo?.run {
            val switch4 = Util.findValue(this, Constants.SWITCH_4).toInt()
            if (switch4 == 0) {
              group.status = STATUS_ABNORMAL
              device.status = STATUS_ABNORMAL
            }
          }
        }
      }
      return this
    }

  }

}
