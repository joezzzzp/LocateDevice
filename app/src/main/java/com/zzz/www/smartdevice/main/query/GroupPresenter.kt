package com.zzz.www.smartdevice.main.query

import android.content.Context
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.api.DeviceRepo
import com.zzz.www.smartdevice.bean.*
import com.zzz.www.smartdevice.db.DataRepo
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

  private var context: Context = view as Context

  override fun fillGroupData(groupDevices: MutableMap<Group, ArrayList<Device>>) {
    view.showLoading()
    groupDevices.clear()
    Observable.unsafeCreate(Observable.OnSubscribe<MutableMap<Group, ArrayList<Device>>> {
      val repo = DataRepo.getInstance(context)
      val groups = repo.getAllGroup()
      groups.forEach { group ->
        val devices = repo.findGroupDevices(group.id)
        for (device in devices) {
          if (device.status == DeviceStatus.ERROR || device.status == DeviceStatus.STATUS_AUTO_CHANGED) {
            group.status = DeviceStatus.ERROR
            break
          }
        }
        groupDevices[group] = devices
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
    view.addGroupResult(ret.valid(),
      context.getString(if (ret.valid()) R.string.add_group_success else R.string.add_group_failed), ret)
  }

  override fun deleteGroup(group: Group) {
    val ret = DataRepo.getInstance(context).deleteGroup(group.id)
    view.deleteGroupResult(ret,
      context.getString(if (ret) R.string.delete_group_success else R.string.delete_group_failed), group)
  }

  override fun queryDeviceByRestApi(sn: String) {
    view.showLoading()
    DeviceRepo.get().getDeviceSumInfo(sn).subscribe(object : Subscriber<DeviceInfoResponse>() {
      override fun onNext(response: DeviceInfoResponse?) {
        val startDate = response?.startTime ?: 0L
        val status = response?.status
        response?.sumInfo?.run {
          this.status = status
          this.startDate = startDate
          val dataRepo = DataRepo.getInstance(context)
          val device = Device(sn = sn)
          val foundDevice = dataRepo.findDevice(device).apply {
            this@apply.sn = sn
            this@apply.sumInfo = this@run
          }
          DataRepo.getInstance(context).updateOrInsertDevice(foundDevice)
          view.queryDeviceResult(true, this)
          return
        }
        if (response?.sumInfo == null) {
          val dataRepo = DataRepo.getInstance(context)
          val device = Device(sn = sn)
          val foundDevice = dataRepo.findDevice(device).apply {
            this@apply.sn = sn
            this@apply.sumInfo = DeviceInfo(sn = sn, startDate = response?.startTime ?: 0L, hasData = false)
          }
          DataRepo.getInstance(context).updateOrInsertDevice(foundDevice)
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
    groupDevices.entries.forEach { groupDevice ->
      groupDevice.value.forEach { device ->
        queue.add(query(groupDevice.key, device))
      }
    }
    Observable.concat(queue).observeOn(AndroidSchedulers.mainThread()).doOnTerminate {
      view.hideLoading()
      view.queryAllDevicesResult(true)
    }.subscribe(object : Subscriber<QueryResult>() {
      override fun onNext(t: QueryResult?) {

      }

      override fun onCompleted() {

      }

      override fun onError(e: Throwable?) {
        view.queryAllDevicesResult(false)
      }

    })
  }

  override fun importFile(path: String) {
    view.showLoading()
    Observable.unsafeCreate(Observable.OnSubscribe<Any> {
      try {
        Util.readTextAndImport(context, path)
      } catch (e: Exception) {
        it.onError(e)
      }
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

  private fun query(group: Group, device: Device) : Observable<QueryResult> =
    DeviceRepo.get().getDeviceSumInfo(device.sn).observeOn(Schedulers.io())
      .map { deviceInfoResponse -> QueryResult(context, group, device, deviceInfoResponse).updateStatus() }

  private class QueryResult(private val context: Context, private val group: Group,
                    private val device: Device, val data: DeviceInfoResponse?) {
    fun updateStatus(): QueryResult {
      data?.run {
        val repo = DataRepo.getInstance(context)
        val d = repo.findDevice(device)
        if (!d.valid()) {
          return this@QueryResult
        }
        repo.updateDevice(device.apply {
          this.sumInfo = this@run.sumInfo
          if (this.sumInfo != null) {
            this.sumInfo?.status = this@run.status
            this.sumInfo?.startDate = this@run.startTime ?: 0L
          } else {
            this.sumInfo = DeviceInfo(sn = device.sn, startDate = this@run.startTime ?: 0L, hasData = false)
          }
          this.status = this@run.status
        })
      }
      return this
    }

  }

}
