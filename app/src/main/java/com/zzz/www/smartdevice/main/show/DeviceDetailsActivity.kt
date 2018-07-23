package com.zzz.www.smartdevice.main.show

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.baidu.mapapi.model.LatLng
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.bean.Device
import com.zzz.www.smartdevice.bean.DeviceInfo
import com.zzz.www.smartdevice.bean.DeviceStatus
import com.zzz.www.smartdevice.bean.Group
import com.zzz.www.smartdevice.db.DataRepo
import com.zzz.www.smartdevice.main.DeviceApplication
import com.zzz.www.smartdevice.main.location.MapActivity
import com.zzz.www.smartdevice.utils.Util
import com.zzz.www.smartdevice.widget.ProgressDialog
import kotlinx.android.synthetic.main.activity_device_location_info.*

/**
 * @author zzz
 * @date create at 2018/1/4.
 */
class DeviceDetailsActivity : AppCompatActivity(), DeviceDetailsContract.View {

  private lateinit var deviceInfoAdapter: DeviceDetailsAdapter
  private lateinit var deviceInfo: DeviceInfo
  private var historyListAdapter: StringListAdapter? = null
  private var groupListAdapter: StringListAdapter? = null
  private var listView: View? = null
  private var stringRecyclerView: RecyclerView? = null
  private var historyDeviceInfo: ArrayList<DeviceInfo> = arrayListOf()
  private var allGroups: ArrayList<Group> = arrayListOf()
  private var skip: Long = 0
  private var canLoadMore = true
  private var isLoading = false
  private var dialogType = SHOWING_NO_DIALOG
  private var deviceName = ""
  private lateinit var loading: ProgressDialog

  private var historyDialog: AlertDialog? = null
  private lateinit var groupDialog: AlertDialog

  private lateinit var presenter: DeviceDetailsPresenter

  companion object {
    private const val dataKey: String = "DEVICE_INFO_DATA"
    private const val isHistoryKey: String = "DEVICE_INFO_HISTORY_DATA"
    private const val deviceNameKey: String = "DEVICE_NAME"
    private const val PAGE_SIZE = 10

    private const val SHOWING_NO_DIALOG = -1
    private const val SHOWING_HISTORY_DIALOG = 1
    private const val SHOWING_GROUP_DIALOG = 2

    fun start(context: Context, deviceInfo: DeviceInfo?, name: String, showHistory: Boolean) {
      val intent = Intent(context, DeviceDetailsActivity::class.java)
      intent.putExtra(dataKey, deviceInfo)
      intent.putExtra(isHistoryKey, showHistory)
      intent.putExtra(deviceNameKey, name)
      context.startActivity(intent)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_device_location_info)
    presenter = DeviceDetailsPresenter(this)
    loading = ProgressDialog(this)
    deviceName = intent.getStringExtra(deviceNameKey)
    initData(intent.getParcelableExtra(dataKey) as? DeviceInfo)
  }

  private fun initData(deviceInfo: DeviceInfo?) {
    deviceInfo?.run {
      this@DeviceDetailsActivity.deviceInfo = deviceInfo
      deviceInfoAdapter = DeviceDetailsAdapter(this@DeviceDetailsActivity,
        deviceInfo2Items(deviceInfo), intent.getBooleanExtra(isHistoryKey, false))
      rvDeviceInfo.run {
        layoutManager = LinearLayoutManager(this@DeviceDetailsActivity)
        adapter = deviceInfoAdapter
        visibility = View.VISIBLE
      }
      tvNoData.visibility = View.GONE
      supportActionBar?.run {
        displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        customView =
          LayoutInflater.from(this@DeviceDetailsActivity).inflate(R.layout.action_bar_view,
            FrameLayout(this@DeviceDetailsActivity), false).apply {
            val shownName = deviceName + when (deviceInfo.status) {
              DeviceStatus.ERROR -> getString(R.string.abnormal)
              DeviceStatus.STATUS_AUTO_CHANGED -> getString(R.string.status_changed)
              else -> ""
            }
            findViewById<TextView>(R.id.name_text_view).run {
              if (deviceInfo.status == DeviceStatus.ERROR || deviceInfo.status == DeviceStatus.STATUS_AUTO_CHANGED) {
                text = Util.createSpannable(this@DeviceDetailsActivity, shownName,
                  deviceName.length, shownName.length, R.style.style_error)
                val message = if (deviceInfo.status == DeviceStatus.ERROR) getString(R.string.clear_error)
                else getString(R.string.clear_status_changed)
                setOnClickListener {
                  AlertDialog.Builder(this@DeviceDetailsActivity).setMessage(message).
                    setPositiveButton(R.string.confirm) { dialog, _ ->
                      presenter.updateNewDate(deviceInfo.sn)
                      dialog?.dismiss()
                    }.setNegativeButton(R.string.cancel) { dialog, _ -> dialog?.dismiss() }.show()
                }
              } else {
                text = shownName
              }
            }
            findViewById<ImageButton>(R.id.map_image_button).setOnClickListener { gotoMapActivity() } }
      }
      return
    }
    supportActionBar?.run {
      displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
      customView = LayoutInflater.from(this@DeviceDetailsActivity).inflate(R.layout.action_bar_view,
        FrameLayout(this@DeviceDetailsActivity), false).apply {
        findViewById<TextView>(R.id.name_text_view).text = deviceName
      }
    }
    rvDeviceInfo.visibility = View.GONE
    tvNoData.visibility = View.VISIBLE
  }

  private fun gotoMapActivity() {
    deviceInfo.run {
      var latitude = lbsLatitude
      var longitude = lbsLongitude
      var useGps = false
      if (gpsLatitude != 0.0 || gpsLongitude != 0.0) {
        latitude = gpsLatitude
        longitude = gpsLongitude
        useGps = true
      }
      if (!useGps) {
        Toast.makeText(this@DeviceDetailsActivity, getString(R.string.hint_no_gps), Toast.LENGTH_SHORT).show()
      }
      MapActivity.start(this@DeviceDetailsActivity, LatLng(latitude, longitude))
    }
  }

  fun showHistoryDialog() {
    if (listView == null || stringRecyclerView == null || historyListAdapter == null) {
      listView = layoutInflater.inflate(R.layout.dialog_list_string, FrameLayout(this), false)
      stringRecyclerView = listView?.findViewById(R.id.stringList)
      historyListAdapter = StringListAdapter(this@DeviceDetailsActivity)
      stringRecyclerView?.apply {
        layoutManager = LinearLayoutManager(this@DeviceDetailsActivity)
        adapter = historyListAdapter
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
          override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            recyclerView?.run {
              if (!canScrollVertically(1)) {
                addHistory()
              }
            }
          }
        })
      }
    }
    if (historyDialog == null) {
      historyDialog = AlertDialog.Builder(this).setView(listView).setCancelable(true)
        .setOnDismissListener { dialogType = SHOWING_NO_DIALOG }.create()
    }
    historyDialog?.show()
    dialogType = SHOWING_HISTORY_DIALOG
    addHistory()
  }

  fun addHistory() {
    if (!isLoading && canLoadMore) {
      isLoading = true
      historyListAdapter?.showLoading(true)
      presenter.getHistory(deviceInfo.sn, skip, PAGE_SIZE)
    }
  }

  fun showGroupDialog() {
    val listView = layoutInflater.inflate(R.layout.dialog_list_string, FrameLayout(this), false).apply {
      findViewById<RecyclerView>(R.id.stringList).apply {
        layoutManager = LinearLayoutManager(this@DeviceDetailsActivity)
        groupListAdapter = StringListAdapter(this@DeviceDetailsActivity)
        adapter = groupListAdapter
        if (allGroups.isEmpty()) {
          allGroups = DataRepo.getInstance(this@DeviceDetailsActivity).getAllGroup()
        }
        if (allGroups.isNotEmpty()) {
          val strings = arrayListOf<CharSequence>()
          allGroups.iterator().run {
            while (hasNext()) {
              val item = next()
              strings.add(item.name)
            }
          }
          groupListAdapter?.addItems(strings)
        }
      }
    }
    groupDialog = AlertDialog.Builder(this).setView(listView).setOnDismissListener {
      groupListAdapter?.removeAll()
      dialogType = SHOWING_NO_DIALOG
    }.setCancelable(true).show()
    dialogType = SHOWING_GROUP_DIALOG
  }

  fun itemClickAction(position: Int) {
    when (dialogType) {
      SHOWING_HISTORY_DIALOG -> {
        DeviceDetailsActivity.start(this, historyDeviceInfo[position], deviceName, false)
        historyDialog?.dismiss()
      }
      SHOWING_GROUP_DIALOG -> {
        val device = DataRepo.getInstance(this).findDevice(Device().apply {
          this.sn = deviceInfo.sn
        })
        if (device.valid()) {
          if (device.group == allGroups[position].id) {
            groupDialog.dismiss()
            return
          }
          device.group = allGroups[position].id
          if (DataRepo.getInstance(this).updateDevice(device).valid()) {
            rvDeviceInfo.adapter = DeviceDetailsAdapter(this, deviceInfo2Items(deviceInfo), true)
            DeviceApplication.refreshAll = true
          } else {
            Toast.makeText(this, R.string.update_group_failed, Toast.LENGTH_SHORT).show()
          }
        }
        groupDialog.dismiss()
      }
      else -> {}
    }
  }

  private fun deviceInfo2Items(deviceInfo: DeviceInfo): ArrayList<Pair<String, String>> {
    val items = arrayListOf<Pair<String, String>>()
    items.addAll(deviceInfo.getStringPair())
    if (intent.getBooleanExtra(isHistoryKey, false)) {
      items.add(Pair(getString(R.string.group_name_belongs_to), getGroupName()))
    }
    return items
  }

  private fun getGroupName(): String {
    var name = ""
    val device = DataRepo.getInstance(this).findDevice(Device().apply {
      sn = deviceInfo.sn
    })
    if (device.valid()) {
      val group = DataRepo.getInstance(this).findGroup(Group(id = device.group))
      if (group.valid()) {
        name = group.name
      }
    }
    return name
  }

  override fun getHistoryResult(isSuccess: Boolean, deviceInfo: List<DeviceInfo>) {
    isLoading = false
    if (deviceInfo.isNotEmpty()) {
      canLoadMore = true
      skip += PAGE_SIZE
      historyDeviceInfo.addAll(deviceInfo)
      val strings = arrayListOf<CharSequence>()
      deviceInfo.iterator().run {
        while (hasNext()) {
          val item = next()
          strings.add(Util.formatDate(this@DeviceDetailsActivity, item.collectDate))
        }
      }
      historyListAdapter?.addItems(strings)
    } else {
      canLoadMore = false
    }
    historyListAdapter?.showLoading(false)
  }

  override fun updateNewDateResult(isSuccess: Boolean, deviceInfo: DeviceInfo?) {
    if (isSuccess) {
      initData(deviceInfo)
    } else {
      Toast.makeText(this, R.string.query_failed, Toast.LENGTH_SHORT).show()
    }
  }

  override fun showLoading() {
    loading.show()
  }

  override fun showLoading(text: String) {
    loading.show(text)
  }

  override fun hideLoading() {
    loading.dismiss()
  }
}