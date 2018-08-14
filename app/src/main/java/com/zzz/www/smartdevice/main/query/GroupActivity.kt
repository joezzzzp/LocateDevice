package com.zzz.www.smartdevice.main.query

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.bean.*
import com.zzz.www.smartdevice.db.DataRepo
import com.zzz.www.smartdevice.main.DeviceApplication
import com.zzz.www.smartdevice.main.show.DeviceDetailsActivity
import com.zzz.www.smartdevice.main.show.StringListAdapter
import com.zzz.www.smartdevice.utils.FileUtil
import com.zzz.www.smartdevice.utils.Util
import com.zzz.www.smartdevice.widget.ProgressDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class GroupActivity : AppCompatActivity(), GroupContract.View {

  companion object {
    private const val OPEN_FILE_REQUEST_CODE = 1
    private const val GET_READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2
  }

  private lateinit var loading: ProgressDialog
  private lateinit var typeInDialog: Dialog
  private lateinit var deleteDialog: Dialog
  private lateinit var deviceListDialog: AlertDialog

  private lateinit var groupAdapter: GroupAdapter

  private lateinit var presenter: GroupPresenter

  private var groupDevices: HashMap<Group, ArrayList<Device>> = linkedMapOf()
  private var showingGroup: Group? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    initView()
    presenter = GroupPresenter(this)
  }

  override fun onResume() {
    super.onResume()
    initData()
    if (DeviceApplication.refreshAll) {
    }
    DeviceApplication.refreshAll = false
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      OPEN_FILE_REQUEST_CODE -> {
        if (resultCode == Activity.RESULT_OK) {
          val path = FileUtil.getFilePathByUri(this, data?.data)
          path?.run {
            if (path.endsWith(".txt", ignoreCase = true)) {
              val file = File(path)
              if (file.exists()) {
                presenter.importFile(path)
                return
              }
            }
          }
        }
        Toast.makeText(this, R.string.invalid_file_type, Toast.LENGTH_SHORT).show()
      }
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == GET_READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
      && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      gotoChooseFile()
    }
  }

  private fun initView() {
    loading = ProgressDialog(this)
    groupList.run {
      layoutManager = LinearLayoutManager(this@GroupActivity)
      groupAdapter = GroupAdapter(this@GroupActivity)
      adapter = groupAdapter
    }
    snEditText.setOnEditorActionListener(TextView.OnEditorActionListener { textView, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
          .hideSoftInputFromWindow(snEditText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        val deviceId = textView.text.toString().trim()
        if (deviceId.isNotEmpty()) {
          presenter.queryDeviceByRestApi(deviceId)
        } else {
          Toast.makeText(this@GroupActivity, R.string.sn_empty_hint, Toast.LENGTH_SHORT).show()
        }
        return@OnEditorActionListener true
      }
      false
    })
    tvImport.setOnClickListener { getPermission() }
    tvQueryAll.setOnClickListener { presenter.queryAllDeviceByRestApi(groupDevices) }
  }

  private fun initData() {
    presenter.fillGroupData(groupDevices)
  }

  @SuppressLint("InflateParams")
  fun showTypeInDialog() {
    typeInDialog = Dialog(this).apply {
      setContentView(layoutInflater.inflate(R.layout.dialog_type_in, null, false)
        .apply {
          findViewById<TextView>(R.id.btnCancel).setOnClickListener {
            typeInDialog.dismiss()
          }
          findViewById<TextView>(R.id.btnConfirm).setOnClickListener {
            presenter.addGroup(findViewById<EditText>(R.id.etGroupName).text.toString().trim())
          }
        })
    }
    typeInDialog.show()
  }

  fun showDeleteDialog(group: Group) {
    deleteDialog = AlertDialog.Builder(this)
      .setTitle(R.string.delete_title)
      .setMessage(getString(R.string.delete_content, group.name))
      .setPositiveButton(R.string.confirm) { _, _ -> presenter.deleteGroup(group) }
      .setNegativeButton(R.string.cancel) { _, _ -> deleteDialog.dismiss() }
      .create()
    deleteDialog.show()
  }

  private fun showDeviceListDialog(group: Group) {
    showingGroup = group
    var showDialog = false
    val list = layoutInflater.inflate(R.layout.dialog_list_string, FrameLayout(this), false).apply {
      findViewById<RecyclerView>(R.id.stringList).apply {
        layoutManager = LinearLayoutManager(this@GroupActivity)
        adapter = StringListAdapter(this@GroupActivity).apply {
          groupDevices[group]?.run {
            if (isNotEmpty()) {
              val strings: ArrayList<CharSequence> = arrayListOf()
              forEach {
                if (it.status == DeviceStatus.ERROR || it.status == DeviceStatus.STATUS_AUTO_CHANGED) {
                  val name = if (it.name.isNotEmpty()) it.name else it.sn
                  val errorString = getString(R.string.abnormal)
                  val concatString = name + errorString
                  strings.add(Util.createSpannable(this@GroupActivity, concatString,
                    name.length, concatString.length, R.style.style_error))
                } else {
                  strings.add(if (it.name.isNotEmpty()) it.name else it.sn)
                }
              }
              addItems(strings)
              showDialog = true
            }
          }
        }
      }
    }
    if (!showDialog) {
      Toast.makeText(this, R.string.group_contains_nothing, Toast.LENGTH_SHORT).show()
      return
    }
    deviceListDialog = AlertDialog.Builder(this@GroupActivity).setView(list)
      .setOnDismissListener{ showingGroup = null }.show()
  }

  private fun updateGroupList() {
    (groupList.adapter as GroupAdapter).groups = ArrayList(groupDevices.keys)
  }

  fun itemClickAction(position: Int) {
    showingGroup?.run {
      var deviceInfo = groupDevices[this]?.get(position)?.sumInfo
      val name = groupDevices[this]?.get(position)?.run {
        if (!TextUtils.isEmpty(name)) {
          name
        } else if (!TextUtils.isEmpty(sn)) {
          sn
        } else {
          ""
        }
      } ?: ""
      if (deviceInfo == null) {
        groupDevices[this]?.get(position)?.run {
          deviceInfo = DeviceInfo(sn = this.sn, hasData = false)
        }
      }
      DeviceDetailsActivity.start(this@GroupActivity, deviceInfo, name, true)
    }
    deviceListDialog.dismiss()
  }

  fun showDeviceList(group: Group) {
    showDeviceListDialog(group)
  }

  private fun getPermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
      != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
        GET_READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
      return
    }
    gotoChooseFile()
  }

  private fun gotoChooseFile() {
    startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
      type = "text/plain"
      addCategory(Intent.CATEGORY_OPENABLE)
    }, OPEN_FILE_REQUEST_CODE)
  }

  override fun fillGroupDataResult(success: Boolean) {
    if (success) {
      updateGroupList()
    }
  }

  override fun addGroupResult(success: Boolean, message: String, group: Group?) {
    typeInDialog.dismiss()
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    group?.run { groupAdapter.addGroup(this) }
  }

  override fun deleteGroupResult(success: Boolean, message: String, group: Group) {
    deleteDialog.dismiss()
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    groupAdapter.deleteGroup(group)
  }

  override fun queryDeviceResult(success: Boolean, deviceInfo: DeviceInfo) {
    if (success) {
      var name = DataRepo.getInstance(this).findDevice(Device(sn = deviceInfo.sn)).name
      if (TextUtils.isEmpty(name)) {
        name = deviceInfo.sn
      }
      DeviceDetailsActivity.start(this, deviceInfo, name, true)
    } else {
      Toast.makeText(this, getString(R.string.can_not_get_device_info), Toast.LENGTH_SHORT).show()
    }
  }

  override fun getTokenResult(success: Boolean) {
    if (!success) {
      Toast.makeText(this, getString(R.string.hint_no_token), Toast.LENGTH_SHORT).show()
    }
  }

  override fun queryAllDevicesResult(success: Boolean) {
    if (success) {
      presenter.fillGroupData(groupDevices)
    } else {
      Toast.makeText(this, getString(R.string.query_failed), Toast.LENGTH_SHORT).show()
    }
  }

  override fun importFileResult(success: Boolean) {
    if (success) {
      presenter.fillGroupData(groupDevices)
    } else {
      Toast.makeText(this, R.string.parse_file_failed, Toast.LENGTH_SHORT).show()
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
