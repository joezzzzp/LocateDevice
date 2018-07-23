package com.zzz.www.smartdevice.utils

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.bean.Device
import com.zzz.www.smartdevice.bean.Group
import com.zzz.www.smartdevice.db.DataRepo
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author zzz
 * @date create at 2018/5/2.
 */
class Util {
  companion object {

    fun formatDate(context: Context?, time: Long): String =
      SimpleDateFormat(
        if (context != null) context.getString(R.string.time_format) else "yyyy-MM-dd HH:mm:ss",
        Locale.CHINA).format(Date(time))

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
  }
}