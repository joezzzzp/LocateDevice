package com.zzz.www.smartdevice.main.query

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.tckj.zyfsdk.ZYFSdk
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.bean.Device
import com.zzz.www.smartdevice.bean.DeviceInfo
import com.zzz.www.smartdevice.bean.Group
import com.zzz.www.smartdevice.db.DataRepo
import com.zzz.www.smartdevice.main.login.LoginActivity
import com.zzz.www.smartdevice.main.show.DeviceDetailsActivity
import com.zzz.www.smartdevice.main.show.StringListAdapter
import com.zzz.www.smartdevice.widget.ProgressDialog
import kotlinx.android.synthetic.main.activity_main.*

class GroupActivity : AppCompatActivity(), GroupContract.View {
  private var isQuery: Boolean = false

  private lateinit var loading: ProgressDialog
  private lateinit var presenter: GroupPresenter

  private lateinit var typeInDialog: Dialog
  private lateinit var deleteDialog: Dialog
  private lateinit var groupAdapter: GroupAdapter

  private lateinit var deviceListDialog: AlertDialog
  private var devices: ArrayList<Device> = arrayListOf()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    isQuery = false
    initView()
    presenter = GroupPresenter(this).also { it.getGroups() }
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
    tvLogout.setOnClickListener{ presenter.logOut() }
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
    deleteDialog = AlertDialog.Builder(this).setTitle(R.string.delete_title)
      .setMessage(getString(R.string.delete_content, group.name))
      .setPositiveButton(R.string.confirm) { _, _ -> presenter.deleteGroup(group) }
      .setNegativeButton(R.string.cancel) { _, _ -> deleteDialog.dismiss() }
      .create()
    deleteDialog.show()
  }

  private fun showDeviceListDialog(groupId: Long) {
    val list = layoutInflater.inflate(R.layout.dialog_list_string, null, false).apply {
      findViewById<RecyclerView>(R.id.stringList).apply {
        layoutManager = LinearLayoutManager(this@GroupActivity)
        adapter = StringListAdapter(this@GroupActivity).apply {
          devices = DataRepo.getInstance(this@GroupActivity).findGroupDevices(groupId)
          if (devices.isNotEmpty()) {
            val strings: ArrayList<String> = arrayListOf()
            devices.iterator().run {
              while (hasNext()) {
                val item = next()
                strings.add(if (item.name.isNotEmpty()) item.name else item.sn)
              }
            }
            addItems(strings)
          }
        }
      }
    }
    if (devices.isNotEmpty()) {
      deviceListDialog = AlertDialog.Builder(this).setView(list).setOnDismissListener{
        devices.clear()
      }.show()
    } else {
      Toast.makeText(this, R.string.group_contains_nothing, Toast.LENGTH_SHORT).show()
    }
  }

  fun itemClickAction(position: Int) {
    presenter.queryDeviceByRestApi(devices[position].sn)
    deviceListDialog.dismiss()
  }

  fun showDeviceList(group: Group) {
    showDeviceListDialog(group.id)
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

  override fun getGroupResult(success: Boolean, groups: ArrayList<Group>) {
    if (success) {
      (groupList.adapter as GroupAdapter).groups = groups
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

  override fun logoutResult(success: Boolean, message: String) {
    if (success) {
      startActivity(Intent(this, LoginActivity::class.java))
      finish()
    }
  }

  override fun queryDeviceResult(success: Boolean, deviceInfo: DeviceInfo) {
    if (success) {
      DeviceDetailsActivity.start(this, deviceInfo, true)
    }
  }

}
