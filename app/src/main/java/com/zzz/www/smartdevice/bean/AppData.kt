package com.zzz.www.smartdevice.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tckj.zyfsdk.entity.DeviceDetailsEntity.DeviceDetails.RecordsBean
import com.zzz.www.smartdevice.api.HttpConfig
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*

/**
 * @author zzz
 * @date create at 2018/4/23.
 */

const val STATUS_NONE = 0
const val STATUS_NORMAL = 1
const val STATUS_ABNORMAL = 2

@SuppressLint("ParcelCreator")
@Parcelize
data class Group(var id: Long = -1,
                 var name: String = "",
                 var status: Int = STATUS_NONE) : Parcelable {
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
                  var lastUpdateTime: Long = 0,
                  var currentDeviceInfo: DeviceInfo? = null,
                  var status: Int = STATUS_NONE) : Parcelable {

  fun valid(): Boolean {
    return id > -1
  }
}

@SuppressLint("ParcelCreator")
@Parcelize
data class DeviceInfo(var id: Long = -1,
                      var deviceId: Long = -1,
                      var sn: String = "",
                      var data: @RawValue ArrayList<InfoItem> = arrayListOf(),
                      var time: Long = -1) : Parcelable {
  fun valid(): Boolean {
    return id > -1
  }
}

@SuppressLint("ParcelCreator")
@Parcelize
data class InfoItem (var fieldName: String = "",
                    var fieldUnit: String = "",
                    var disPlayName: String = "",
                    var fieldType: String = "",
                    var fieldValue: String = "",
                    var fieldId: String = "",
                    var dwType: Int = 0) : Parcelable {

  companion object {
    fun createByRawValue(dataItem: Any): InfoItem {
      return when (dataItem) {
        is RecordsBean -> InfoItem(dataItem)
        is DateDataItem -> InfoItem(dataItem)
        else -> InfoItem()
      }
    }
  }

  constructor(recordsBean: RecordsBean): this() {
    fieldName = recordsBean.fieldName ?: ""
    fieldUnit = recordsBean.fieldUnit ?: ""
    disPlayName = recordsBean.disPlayName ?: ""
    fieldType = recordsBean.fieldType ?: ""
    fieldValue = recordsBean.fieldValue ?: ""
    fieldId = recordsBean.fieldId ?: ""
    dwType = recordsBean.dwType
  }

  constructor(dateDataItem: DateDataItem): this() {
    fieldUnit = dateDataItem.fieldUnit
    disPlayName = dateDataItem.displayName
    fieldType = dateDataItem.fieldType
    fieldValue = dateDataItem.hexValues
    fieldId = dateDataItem.fieldId
  }
}

@SuppressLint("ParcelCreator")
@Parcelize
data class TokenRequest(var corporateId: String = HttpConfig.corporateId,
                        @SerializedName("corporatePasswd")
                        var corporatePassword: String = HttpConfig.corporatePassword) : Parcelable

@SuppressLint("ParcelCreator")
@Parcelize
data class TokenResponse(var expireTime: Long = 0L,
                         var respCode: String = "",
                         var respMessage: String = "",
                         var token: String = "") : Parcelable {
  fun isSuccess() = respCode == HttpConfig.successCode
}

@SuppressLint("ParcelCreator")
@Parcelize
data class DateDataRequest(var deviceSn: String = "",
                       var beginDate: Long = 0L,
                       var endDate: Long = 0L) : Parcelable

@SuppressLint("ParcelCreator")
@Parcelize
data class DateDataItem(var decimalDigit: Int = 0,
                        var displayName: String = "",
                        var fieldId: String = "",
                        var fieldType: String = "",
                        var fieldUnit: String = "",
                        var hexValues: String = "",
                        var numValues: Float = -1f) : Parcelable {
  override fun toString(): String {
    return "$decimalDigit \n $displayName \n $fieldId \n $fieldType \n $fieldUnit \n $hexValues \n $numValues"
  }
}

@SuppressLint("ParcelCreator")
@Parcelize
data class DateData(var collectDate: Long = 0,
                    var deviceNum: String = "",
                    var items: @RawValue ArrayList<DateDataItem> = arrayListOf(),
                    var updating: Boolean = false) : Parcelable

@SuppressLint("ParcelCreator")
@Parcelize
data class DateDataResponse(var respCode: String = "",
                        var respMessage: String = "",
                        var t: @RawValue ArrayList<DateData> = arrayListOf()) : Parcelable

