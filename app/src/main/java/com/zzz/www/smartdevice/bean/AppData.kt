package com.zzz.www.smartdevice.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import com.zzz.www.smartdevice.utils.Util
import kotlinx.android.parcel.Parcelize
import java.time.LocalDateTime
import java.util.*

/**
 * @author zzz
 * @date create at 2018/4/23.
 */

enum class DeviceStatus {
  NORMAL, ERROR, STATUS_AUTO_CHANGED
}

@SuppressLint("ParcelCreator")
@Parcelize
data class Group(var id: Long = -1,
                 var name: String = "",
                 @Transient var status: DeviceStatus = DeviceStatus.NORMAL) : Parcelable {
  fun valid(): Boolean {
    return id > -1
  }
}

@SuppressLint("ParcelCreator")
@Parcelize
data class Device(var id: Long = -1,
                  var sn: String = "",
                  var name: String = "",
                  var group: Long = -1,
                  var sumInfo: DeviceInfo? = null,
                  var status: DeviceStatus = DeviceStatus.NORMAL) : Parcelable {

  fun valid(): Boolean {
    return id > -1
  }
}

@SuppressLint("ParcelCreator")
@Parcelize
data class CommonResponse(var code: Int,
                          var message: String,
                          var data: DeviceInfoResponse?): Parcelable

@SuppressLint("ParcelCreator")
@Parcelize
data class DeviceInfoResponse(var id: String? = null,
                              var sn: String = "",
                              var name: String = "",
                              var sumInfo: DeviceInfo? = null,
                              var startTime: Long? = null,
                              var updatedAt: Long? = null,
                              var status: DeviceStatus = DeviceStatus.NORMAL): Parcelable

@SuppressLint("ParcelCreator")
@Parcelize
data class DeviceInfo(var id: String? = null,
                      var sn: String = "",
                      var collectDate: Long = 0L,
                      var switch1: Int = 0,
                      var switch2: Int = 0,
                      var switch3: Int = 0,
                      var switch4: Int = 0,
                      var ad1: Int = 0,
                      var ad2: Int = 0,
                      var ad3: Int = 0,
                      var ad4: Int = 0,
                      var voltage: Int = 0,
                      var gpsLongitude: Double = 0.0,
                      var gpsLatitude: Double = 0.0,
                      var lbsLongitude: Double = 0.0,
                      var lbsLatitude: Double = 0.0,
                      var launchTime: Long = 0,
                      var battery: Int = 0,
                      var signalIntensity: Int = 0,
                      var status: DeviceStatus? = DeviceStatus.NORMAL) : Parcelable {
  fun getStringPair(): ArrayList<Pair<String, String>> =
    arrayListOf<Pair<String, String>>().apply {
      add(Pair("序列号", sn))
      add(Pair("采集时间", Util.formatDate(null, collectDate)))
      add(Pair("A放电", "$switch1"))
      add(Pair("B放电", "$switch2"))
      add(Pair("C放电", "$switch3"))
      add(Pair("状态", if (switch4 == 0) "故障" else "正常"))
      add(Pair("模数转化器1", "${ad1}μA"))
      add(Pair("模数转化器2", "${ad2}μA"))
      add(Pair("模数转化器3", "${ad3}μA"))
      add(Pair("模数转化器4", "${ad4}μA"))
//      add(Pair("电压", "${voltage}Mv"))
      add(Pair("经度(GPS)", "$gpsLongitude"))
      add(Pair("纬度(GPS)", "$gpsLatitude"))
      add(Pair("经度(LBS)", "$lbsLongitude"))
      add(Pair("纬度(LBS)", "$lbsLatitude"))
//      add(Pair("运行时长", "${launchTime}s"))
//      add(Pair("电量", "$battery%"))
//      add(Pair("信号强度", "${signalIntensity}dB"))
    }
}
