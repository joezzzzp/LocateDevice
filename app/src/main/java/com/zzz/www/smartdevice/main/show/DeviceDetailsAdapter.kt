package com.zzz.www.smartdevice.main.show

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.main.show.DeviceDetailsAdapter.DeviceLocationInfoViewHolder
import kotlinx.android.synthetic.main.item_device_locaion_info.view.*

/**
 * @author zzz
 * @date create at 2018/1/4.
 */
class DeviceDetailsAdapter(var context: Context, private var shownItems: ArrayList<Pair<String, String>>,
                           var showHistory: Boolean = true) :
  Adapter<DeviceLocationInfoViewHolder>() {
  private var inflater: LayoutInflater = LayoutInflater.from(context)


  override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DeviceLocationInfoViewHolder {
    return DeviceLocationInfoViewHolder(
      inflater.inflate(R.layout.item_device_locaion_info, parent, false))
  }

  override fun onBindViewHolder(holder: DeviceLocationInfoViewHolder, position: Int) {
    if (showHistory && position == itemCount - 1) {
      holder.label.text = context.getString(R.string.history_label)
      val s = context.getString(R.string.click_to_show_history)
      holder.content.text = createSpannable(s, 0, s.length, R.style.style_can_click)
    }
    if (showHistory && position == itemCount - 2) {
      holder.label.text = shownItems[position].first
      val groupName = shownItems[position].second
      val s = context.getString(R.string.click_to_modify_group)
      val content = "$groupName$s"
      holder.content.text = createSpannable(content, groupName.length, content.length,
        R.style.style_can_click)
    }
    if (shownItems.isNotEmpty() && position < shownItems.size - 1) {
      val item: Pair<String, String> = shownItems[position]
      holder.label.text = item.first
      holder.content.text = item.second
    }
    holder.itemView.setOnClickListener {
      if (showHistory && position == itemCount - 1) {
        (context as DeviceDetailsActivity).showHistoryDialog()
      } else if (showHistory && position == itemCount - 2) {
        (context as DeviceDetailsActivity).showGroupDialog()
      }
    }
  }

  override fun getItemCount(): Int {
    return if (showHistory) shownItems.size + 1 else shownItems.size
  }

  private fun createSpannable(content: String, start: Int, end: Int, style: Int): SpannableString {
    val span = SpannableString(content)
    span.setSpan(TextAppearanceSpan(context, style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return span
  }

  class DeviceLocationInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val label: TextView = itemView.tvLabel
    val content: TextView = itemView.tvContent
  }
}