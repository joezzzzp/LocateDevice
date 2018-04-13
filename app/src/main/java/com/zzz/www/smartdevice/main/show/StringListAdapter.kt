package com.zzz.www.smartdevice.main.show

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.main.query.GroupActivity
import kotlinx.android.synthetic.main.item_loading_footer.view.*
import kotlinx.android.synthetic.main.item_string.view.*

/**
 * @author zzz
 * @date create at 2018/5/2.
 */
class StringListAdapter(var context: Context, var items: ArrayList<CharSequence> = arrayListOf()):
  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  companion object {
    private const val TYPE_STRING = 1
    private const val TYPE_FOOTER = 2
  }

  private var showLoading = false

  fun addItems(items: ArrayList<CharSequence>) {
    val insertPosition = this.items.size
    this.items.addAll(items)
    notifyItemRangeInserted(insertPosition, items.size)
  }

  fun removeAll() {
    items.clear()
    notifyDataSetChanged()
  }

  fun showLoading(showLoading: Boolean) {
    if (!this.showLoading && showLoading) {
      this.showLoading = showLoading
      notifyItemInserted(itemCount)
    }
    if (this.showLoading && !showLoading) {
      this.showLoading = showLoading
      notifyItemRemoved(itemCount)
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
    return when(viewType) {
      TYPE_STRING -> StringViewHolder(LayoutInflater.from(context).inflate(R.layout.item_string,
        parent, false))
      else -> LoadingViewHolder(LayoutInflater.from(context).inflate(R.layout.item_loading_footer,
        parent, false))
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
    holder?.run {
      when (holder) {
        is StringViewHolder -> (this as StringViewHolder).run {
          content.text = items[position]
          itemView.setOnClickListener {
            when (context) {
              is DeviceDetailsActivity -> (context as DeviceDetailsActivity).itemClickAction(position)
              is GroupActivity -> (context as GroupActivity).itemClickAction(position)
            }
          }
        }
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    val total = itemCount
    if (showLoading && position == total - 1) {
      return TYPE_FOOTER
    }
    return TYPE_STRING
  }

  override fun getItemCount(): Int = if (showLoading) items.size + 1 else items.size

  class StringViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val content: TextView = itemView.stringContent
  }

  class LoadingViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val progressBar: ProgressBar = itemView.progressBar
  }
}