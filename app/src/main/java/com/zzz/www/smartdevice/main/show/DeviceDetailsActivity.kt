package com.zzz.www.smartdevice.main.show

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import com.baidu.mapapi.model.LatLng
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.bean.Device
import com.zzz.www.smartdevice.bean.DeviceInfo
import com.zzz.www.smartdevice.bean.Group
import com.zzz.www.smartdevice.bean.InfoItem
import com.zzz.www.smartdevice.db.DataRepo
import com.zzz.www.smartdevice.main.location.MapActivity
import com.zzz.www.smartdevice.utils.Util
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
  private var endDate = System.currentTimeMillis()
  private var beginDate = endDate - pagePeriod
  private var canLoadMore = false
  private var isLoading = false
  private var dialogType = SHOWING_NO_DIALOG

  private lateinit var historyDialog: AlertDialog
  private lateinit var groupDialog: AlertDialog

  private lateinit var presenter: DeviceDetailsPresenter

  companion object {
    private const val dataKey: String = "DEVICE_INFO_DATA"
    private const val isHistoryKey: String = "DEVICE_INFO_HISTORY_DATA"
    private const val PAGE_SIZE = 50

    private const val SHOWING_NO_DIALOG = -1
    private const val SHOWING_HISTORY_DIALOG = 1
    private const val SHOWING_GROUP_DIALOG = 2

    private const val LBS_LONGITUDE = "1EEA"
    private const val LBS_LATITUDE = "07CC"
    private const val GPS_LONGITUDE = "1567"
    private const val GPS_LATITUDE = "1CC3"

    private const val pagePeriod : Long = 7L * 24L * 60L * 60L * 1000L

    fun start(context: Context, deviceInfo: DeviceInfo, showHistory: Boolean) {
      val intent = Intent(context, DeviceDetailsActivity::class.java)
      intent.putExtra(dataKey, deviceInfo)
      intent.putExtra(isHistoryKey, showHistory)
      context.startActivity(intent)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_device_location_info)
    presenter = DeviceDetailsPresenter(this)
    deviceInfo = intent.getParcelableExtra(dataKey) as DeviceInfo
    deviceInfoAdapter = DeviceDetailsAdapter(this,
      deviceRecords2Items(deviceInfo.data), intent.getBooleanExtra(isHistoryKey, false))
    rvDeviceInfo.run {
      layoutManager = LinearLayoutManager(this@DeviceDetailsActivity)
      adapter = deviceInfoAdapter
    }
    btnShowLocation.setOnClickListener{
      val lbsLatitude = Util.findValue(deviceInfo, LBS_LATITUDE)
      val lbsLongitude = Util.findValue(deviceInfo, LBS_LONGITUDE)
      val gpsLatitude = Util.findValue(deviceInfo, GPS_LATITUDE)
      val gpsLongitude = Util.findValue(deviceInfo, GPS_LONGITUDE)
      var latitude = lbsLatitude
      var longitude = lbsLongitude
      var useGps = false
      if (gpsLatitude != 0.0 || gpsLongitude != 0.0) {
        latitude = gpsLatitude
        longitude = gpsLongitude
        useGps = true
      }
      if (!useGps) {
        Toast.makeText(this, getString(R.string.hint_no_gps), Toast.LENGTH_SHORT).show()
      }
      MapActivity.start(this@DeviceDetailsActivity, LatLng(latitude, longitude))
    }
  }

  fun showHistoryDialog() {
    if (listView == null || stringRecyclerView == null || historyListAdapter == null) {
      listView = layoutInflater.inflate(R.layout.dialog_list_string, null, false)
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
                isLoading = true
                addHistory()
              }
            }
          }
        })
      }
    }
    historyDialog = AlertDialog.Builder(this).setView(listView).setCancelable(true)
      .setOnDismissListener { dialogType = SHOWING_NO_DIALOG }.show()
    dialogType = SHOWING_HISTORY_DIALOG
    isLoading = true
    addHistory()
  }

  fun addHistory() {
    if (!isLoading && canLoadMore) {

      presenter.getHistory(deviceInfo.sn, beginDate, endDate)
    }

  }

  fun showGroupDialog() {
    val listView = layoutInflater.inflate(R.layout.dialog_list_string, null, false).apply {
      findViewById<RecyclerView>(R.id.stringList).apply {
        layoutManager = LinearLayoutManager(this@DeviceDetailsActivity)
        groupListAdapter = StringListAdapter(this@DeviceDetailsActivity)
        adapter = groupListAdapter
        if (allGroups.isEmpty()) {
          allGroups = DataRepo.getInstance(this@DeviceDetailsActivity).getAllGroup()
        }
        if (allGroups.isNotEmpty()) {
          val strings = arrayListOf<String>()
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
        DeviceDetailsActivity.start(this, historyDeviceInfo[position], false)
        historyDialog.dismiss()
      }
      SHOWING_GROUP_DIALOG -> {
        val device = DataRepo.getInstance(this).findDevice(Device().apply {
          this.sn = deviceInfo.sn
        })
        if (device.valid()) {
          device.group = allGroups[position].id
          if (DataRepo.getInstance(this).updateDevice(device).valid()) {
            rvDeviceInfo.adapter = DeviceDetailsAdapter(this, deviceRecords2Items(deviceInfo.data), true)
          } else {
            Toast.makeText(this, R.string.update_group_failed, Toast.LENGTH_SHORT).show()
          }
        }
        groupDialog.dismiss()
      }
      else -> {}
    }
  }

  private fun deviceRecords2Items(
    deviceRecords: ArrayList<InfoItem>):
    ArrayList<Pair<String, String>> {
    val items = arrayListOf<Pair<String, String>>()
    deviceRecords.iterator().run {
      while (hasNext()) {
        val record = next()
        items.add(Pair(record.disPlayName, "${record.fieldValue}${record.fieldUnit}"))
      }
    }
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

  override fun getHistoryResult(isSuccess: Boolean, deviceInfo: ArrayList<DeviceInfo>) {
    isLoading = false
    if (deviceInfo.isNotEmpty()) {
      canLoadMore = true
      endDate = beginDate
      beginDate = endDate - pagePeriod
      historyDeviceInfo.addAll(deviceInfo)
      val strings = arrayListOf<String>()
      deviceInfo.iterator().run {
        while (hasNext()) {
          val item = next()
          strings.add(Util.formatDate(this@DeviceDetailsActivity, item.time))
        }
      }
      historyListAdapter?.addItems(strings)
    } else {
      canLoadMore = false
    }
    historyListAdapter?.showLoading(false)
  }

  override fun showLoading() {
  }

  override fun showLoading(text: String) {
  }

  override fun hideLoading() {
  }
}