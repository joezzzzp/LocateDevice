package com.zzz.www.smartdevice.utils

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import com.tckj.zyfsdk.entity.DeviceDetailsEntity
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.bean.*
import com.zzz.www.smartdevice.db.DataRepo
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author zzz
 * @date create at 2018/5/2.
 */
class Util {
  companion object {

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

    fun findValue(info: DeviceInfo?, fieldId: String): String {
      var value = ""
      info?.data?.iterator()?.run {
        while (hasNext()) {
          val item = next()
          if (item.fieldId == fieldId) {
            value = item.fieldValue
            break
          }
        }
      }
      return value
    }

    fun findValue(infoItems: ArrayList<InfoItem>, fieldId: String): String {
      var value = ""
      infoItems.iterator().run {
        while (hasNext()) {
          val item = next()
          if (item.fieldId == fieldId) {
            value = item.fieldValue
            break
          }
        }
      }
      return value
    }

    fun findItem(infoItems: ArrayList<InfoItem>, fieldId: String): InfoItem? {
      infoItems.iterator().run {
        while (hasNext()) {
          val item = next()
          if (item.fieldId == fieldId) {
            return item
          }
        }
      }
      return null
    }

    fun formatDate(context: Context, time: Long): String {
      val format = SimpleDateFormat(context.getString(R.string.time_format), Locale.CHINA)
      return format.format(Date(time))
    }

    fun readTextAndImport(context: Context, path: String) {
      val repo = DataRepo.getInstance(context)
      val reader = BufferedReader(InputStreamReader(FileInputStream(path)))
      var line = reader.readLine()
      while (line != null) {
        if (TextUtils.isEmpty(line) || line.startsWith("#", ignoreCase = true)) {
          line = reader.readLine()
          continue
        }
        val ss = line.split(",", limit = 3)
        val properties = arrayListOf<String>()
        ss.iterator().run {
          while (hasNext()) {
            properties.add(next().trim())
          }
        }
        if (TextUtils.isEmpty(properties[0]) || TextUtils.isEmpty(properties[1])) {
          line = reader.readLine()
          continue
        }
        var group = repo.findGroup(Group(name = properties[0]))
        if (!group.valid()) {
          group = repo.addGroup(Group(name = properties[0]))
        }
        val device = Device(sn = properties[1], name = properties[2], group = group.id)
        repo.updateOrInsertDevice(device)
        line = reader.readLine()
      }
    }

    fun createSpannable(context: Context, content: String, start: Int, end: Int, style: Int): SpannableString {
      val span = SpannableString(content)
      span.setSpan(TextAppearanceSpan(context, style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      return span
    }

    fun getCurrentDeviceInfo(device: Device, allHistories: ArrayList<DeviceInfo>) {
      if (allHistories.isEmpty()) {
        return
      }
      device.currentDeviceInfo = allHistories[0]
      var switch1count = 0; var switch2count = 0; var switch3count = 0
      allHistories.iterator().run {
        while (hasNext()) {
          val item = next()
          switch1count += getSwitchValue(item.data, Constants.SWITCH_1)
          switch2count += getSwitchValue(item.data, Constants.SWITCH_2)
          switch3count += getSwitchValue(item.data, Constants.SWITCH_3)
        }
      }
      device.currentDeviceInfo?.run {
        Util.findItem(this.data, Constants.SWITCH_1)?.fieldValue = switch1count.toString()
        Util.findItem(this.data, Constants.SWITCH_2)?.fieldValue = switch2count.toString()
        Util.findItem(this.data, Constants.SWITCH_3)?.fieldValue = switch3count.toString()
      }
    }

    private fun getSwitchValue(infoItems: ArrayList<InfoItem>, fieldId: String): Int {
      var ret = 0
      if (Util.findValue(infoItems, fieldId) == "0") {
        ret = 1
      }
      return ret
    }
  }
}