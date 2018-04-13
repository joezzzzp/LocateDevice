package com.zzz.www.smartdevice.main.query

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.EventLog
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.TextView
import android.widget.Toast
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.bean.DeviceLocationInfo
import com.zzz.www.smartdevice.main.show.DeviceLocationInfoActivity
import com.zzz.www.smartdevice.utils.DpUtil
import com.zzz.www.smartdevice.utils.PrefsUtil
import com.zzz.www.smartdevice.widget.ProgressDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), QueryDeviceContract.View , TextWatcher{

    private var isQuery : Boolean = false
    private lateinit var loading : ProgressDialog
    private lateinit var presenter : QueryDevicePresenter
    private lateinit var prefsUtil : PrefsUtil
    private lateinit var listPopupWindow : ListPopupWindow
    private lateinit var history : ArrayList<String>
    private lateinit var currentSet : HashSet<String>
    private var activeTextWatcher = false
    private var serialNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loading = ProgressDialog(this)
        presenter = QueryDevicePresenter(this)
        prefsUtil = PrefsUtil.getInstance(this)
        createListPopupWindow()
        snEditText.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(snEditText.windowToken,
                                InputMethodManager.HIDE_NOT_ALWAYS)
                querySN(snEditText.text.toString().trim())
                return@OnEditorActionListener true
            }
            false
        })
        isQuery = false
        showLoading()
        presenter.getToken()
    }

    override fun onResume() {
        super.onResume()
        currentSet = prefsUtil.getStringSet()
        activeTextWatcher = true
        snEditText.addTextChangedListener(this)
    }

    override fun onPause() {
        listPopupWindow.dismiss()
        snEditText.removeTextChangedListener(this)
        super.onPause()
    }

    private fun querySN(sn: String) {
        if (checkSN(sn)) {
            snEditText.setText("")
            serialNumber = sn
            showLoading(getString(R.string.progress_dialog_query_text))
            isQuery = true
            if (presenter.shouldRefreshToken()) {
                presenter.getToken()
            } else {
                presenter.queryDeviceInfo(serialNumber)
            }
        } else {
            Toast.makeText(this, R.string.sn_empty_hint, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkSN(sn: String): Boolean = !TextUtils.isEmpty(sn)

    private fun createListPopupWindow() {
        listPopupWindow = ListPopupWindow(this)
        listPopupWindow.width = DpUtil.dip2px(this, 200F)
        listPopupWindow.height =  DpUtil.dip2px(this, 150F)
        listPopupWindow.anchorView = snEditText
        listPopupWindow.isModal = false
        listPopupWindow.setOnItemClickListener { _, _, position, _ ->
            if (position < history.size - 1) {
                snEditText.setText(history[position])
                snEditText.setSelection(snEditText.text.length)
            } else if (position == history.size - 1) {
                snEditText.setText("")
                clearHistory()
            }
            listPopupWindow.dismiss()
        }
    }

    private fun showListPopupWindow() {
        if (history.size > 1 && !TextUtils.isEmpty(snEditText.text.toString().trim())) {
            val lastHeight : Int = listPopupWindow.height
            if (history.size < 3) {
                listPopupWindow.height = DpUtil.dip2px(this, 100F)
            } else {
                listPopupWindow.height = DpUtil.dip2px(this, 150F)
            }
            listPopupWindow.setAdapter(ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, history))
            if (!listPopupWindow.isShowing || lastHeight != listPopupWindow.height) {
                listPopupWindow.show()
            }
        } else {
            listPopupWindow.dismiss()
        }
    }

    private fun addRecord(record : String) {
        val set : HashSet<String> = HashSet(prefsUtil.getStringSet())
        set.add(record)
        prefsUtil.putStringSet(set)
    }

    private fun clearHistory() {
        val set : HashSet<String> = HashSet()
        currentSet = set
        prefsUtil.putStringSet(set)
    }

    private fun getHistory(text: String) {
        history = ArrayList()
        currentSet.filter { it.contains(text, true) }.forEach { history.add(it) }
        history.add(getString(R.string.clear_history))
    }

    override fun afterTextChanged(s: Editable?) {
        getHistory(s.toString())
        showListPopupWindow()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

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

    override fun queryDeviceSuccess(deviceLocationInfo: DeviceLocationInfo) {
        isQuery = false
        addRecord(serialNumber)
        serialNumber = ""
        hideLoading()
        DeviceLocationInfoActivity.start(this, deviceLocationInfo)
    }

    override fun queryDeviceFailed(errorMessage : String) {
        isQuery = false
        serialNumber = ""
        hideLoading()
        if (TextUtils.isEmpty(errorMessage)) {
            Toast.makeText(this, R.string.query_failed, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getTokenSuccess() {
        if (isQuery) {
            presenter.queryDeviceInfo(serialNumber)
        } else {
            isQuery = false
            serialNumber = ""
            hideLoading()
        }
    }

    override fun getTokenFailed() {
        hideLoading()
        var text : String = getString(R.string.init_failed)
        if (isQuery) {
            text = getString(R.string.query_failed)
        }
        isQuery = false
        serialNumber = ""
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
