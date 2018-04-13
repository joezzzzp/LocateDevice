package com.zzz.www.smartdevice.main.show

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.main.show.DeviceLocationInfoAdapter.DeviceLocationInfoViewHolder
import kotlinx.android.synthetic.main.device_locaion_info_item.view.*

/**
 * @author zzz
 * @date create at 2018/1/4.
 */
class DeviceLocationInfoAdapter(context: Context, private var shownItems: ArrayList<Pair<String, String>>) :
        Adapter<DeviceLocationInfoViewHolder>() {
    private var inflater : LayoutInflater = LayoutInflater.from(context)



    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DeviceLocationInfoViewHolder {
        return DeviceLocationInfoViewHolder(
                inflater.inflate(R.layout.device_locaion_info_item, parent, false))
    }

    override fun onBindViewHolder(holder: DeviceLocationInfoViewHolder, position: Int) {
        if (shownItems.isNotEmpty() && position < shownItems.size) {
            val item : Pair<String, String> = shownItems[position]
            holder.label.text = item.first
            holder.content.text = item.second
        }
    }

    override fun getItemCount(): Int {
        return shownItems.size
    }

    class DeviceLocationInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val label : TextView = itemView.tvLabel
        val content : TextView = itemView.tvContent
    }
}