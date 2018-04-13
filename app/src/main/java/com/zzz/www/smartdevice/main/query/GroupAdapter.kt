package com.zzz.www.smartdevice.main.query

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.bean.Group
import kotlinx.android.synthetic.main.item_group.view.*

/**
 * @author zzz
 * @date create at 2018/4/23.
 */
class GroupAdapter(private val activity: GroupActivity): RecyclerView.Adapter<GroupAdapter.GroupHolder>() {
  var groups: ArrayList<Group> = arrayListOf()
  set(value) {
    field = value
    notifyDataSetChanged()
  }

  fun addGroup(group: Group) {
    groups.add(group)
    notifyItemInserted(itemCount - 2)
  }

  fun deleteGroup(group: Group) {
    val index = groups.indexOf(group)
    if (index > -1) {
      notifyItemRemoved(index)
      groups.removeAt(index)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): GroupHolder {
    return GroupHolder(LayoutInflater.from(activity).inflate(R.layout.item_group, parent, false))
  }

  override fun onBindViewHolder(holder: GroupHolder?, position: Int) {
    holder?.run {
      if (adapterPosition < itemCount - 1) {
        itemView.setBackgroundResource(R.drawable.card_round_corner_frame)
        groupName.run {
          text = groups[position].name
          setTextColor(Color.BLACK)
          setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        }
      } else {
        itemView.setBackgroundColor(Color.TRANSPARENT)
        groupName.run {
          text = activity.getString(R.string.create_new_group)
          setTextColor(Color.parseColor("#2196F3"))
          setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }
      }
      itemView.apply {
        setOnClickListener {
          if (adapterPosition < itemCount - 1) {
            activity.showDeviceList(groups[adapterPosition])
          } else {
            activity.showTypeInDialog()
          }
        }
        setOnLongClickListener {
          if (adapterPosition < itemCount - 1) {
            activity.showDeleteDialog(groups[position])
          }
          return@setOnLongClickListener true
        }
      }
    }
  }

  override fun getItemCount(): Int {
    return groups.size + 1
  }

  class GroupHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val groupName = itemView.groupName!!
  }
}