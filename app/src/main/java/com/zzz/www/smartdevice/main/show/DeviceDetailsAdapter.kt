package com.zzz.www.smartdevice.main.show

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.main.show.DeviceDetailsAdapter.DeviceLocationInfoViewHolder
import com.zzz.www.smartdevice.utils.Util
import kotlinx.android.synthetic.main.item_device_locaion_info.view.*

/**
 * @author zzz
 * @date create at 2018/1/4.
 */
class DeviceDetailsAdapter(var context: Context, private var shownItems: ArrayList<Pair<String, String>>,
                           private var showHistory: Boolean = true) :
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
      holder.content.text = Util.createSpannable(context, s, 0, s.length, R.style.style_can_click)
    }
    if (showHistory && position == itemCount - 2) {
      holder.label.text = shownItems[position].first
      val groupName = shownItems[position].second
      val s = context.getString(R.string.click_to_modify_group)
      val content = "$groupName$s"
      holder.content.text = Util.createSpannable(context, content, groupName.length, content.length,
        R.style.style_can_click)
    }
    if (shownItems.isNotEmpty() &&
          ((showHistory && position < shownItems.size - 1) || (!showHistory && position < shownItems.size))) {
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

  class DeviceLocationInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val label: TextView = itemView.tvLabel
    val content: TextView = itemView.tvContent
  }
}