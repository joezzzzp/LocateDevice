package com.zzz.www.smartdevice.utils

import android.content.Context
import com.tckj.zyfsdk.entity.DeviceDetailsEntity
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.bean.DateDataItem
import com.zzz.www.smartdevice.bean.DeviceInfo
import com.zzz.www.smartdevice.bean.InfoItem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author zzz
 * @date create at 2018/5/2.
 */
class Util {
  companion object {
    fun recordsBean2InfoItem(records: ArrayList<DeviceDetailsEntity.DeviceDetails.RecordsBean>): ArrayList<InfoItem> {
      val infos = arrayListOf<InfoItem>()
      records.iterator().run {
        while (hasNext()) {
          val nextItem = next()
          infos.add(InfoItem(nextItem))
        }
      }
      return infos
    }

    fun dataItem2InfoItem(records: ArrayList<out Any>): ArrayList<InfoItem> {
      val infos = arrayListOf<InfoItem>()
      records.iterator().run {
        while (hasNext()) {
          val nextItem = next()
          infos.add(InfoItem.createByRawValue(nextItem))
        }
      }
      return infos
    }

    fun findValue(info: DeviceInfo?, fieldId: String): Double {
      var value = ""
      info?.data?.iterator()?.run {
        while (hasNext()) {
          val item = next()
          if (item.fieldId == fieldId) {
            value = item.fieldValue
          }
        }
      }
      return value.toDouble()
    }

    fun formatDate(context: Context, time: Long): String {
      val format = SimpleDateFormat(context.getString(R.string.time_format), Locale.CHINA)
      return format.format(Date(time))
    }
  }
}