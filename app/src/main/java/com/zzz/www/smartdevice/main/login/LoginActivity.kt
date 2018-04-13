package com.zzz.www.smartdevice.main.login

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.api.HttpClient
import com.zzz.www.smartdevice.api.HttpConfig
import com.zzz.www.smartdevice.api.ZYFApi
import com.zzz.www.smartdevice.api.ZYFRepo
import com.zzz.www.smartdevice.bean.DateDataRequest
import com.zzz.www.smartdevice.bean.DateDataResponse
import com.zzz.www.smartdevice.bean.TokenResponse
import com.zzz.www.smartdevice.bean.TokenRequest
import com.zzz.www.smartdevice.main.DeviceApplication
import com.zzz.www.smartdevice.main.query.GroupActivity
import com.zzz.www.smartdevice.widget.ProgressDialog
import kotlinx.android.synthetic.main.activity_log_in.*
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author zzz
 * @date create at 2018/4/18.
 */
class LoginActivity : AppCompatActivity(), LoginContract.View {
  private lateinit var presenter: LoginPresenter
  private lateinit var currentState: State
  private lateinit var loading: ProgressDialog



  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_log_in)
    loading = ProgressDialog(this)
    presenter = LoginPresenter(this)
    initView(State.LOGIN)
    initAction()
    autoLogin()
  }

  private fun initView(state: State) {
    currentState = state
    clearTypeIn()
    when(state) {
      State.LOGIN -> {
        verifyCodeLayout.visibility = View.GONE
        btnRegisterOrLog.setText(R.string.btn_login)
        tvRegisterNewAccount.setText(R.string.register_account)
        tvForgetPassword.visibility = View.VISIBLE
      }
      State.REGISTER -> {
        verifyCodeLayout.visibility = View.VISIBLE
        btnRegisterOrLog.setText(R.string.btn_register)
        tvRegisterNewAccount.setText(R.string.return_login)
        tvForgetPassword.visibility = View.GONE
      }
      State.RESET_PASSWORD -> {
        verifyCodeLayout.visibility = View.VISIBLE
        btnRegisterOrLog.setText(R.string.btn_reset_password)
        tvRegisterNewAccount.setText(R.string.return_login)
        tvForgetPassword.visibility = View.GONE
      }
    }
  }

  private fun initAction() {
    btnVerifyCode.setOnClickListener { presenter.getVerifyCode(etAccount.text.toString().trim(),
      currentState == State.REGISTER)}
    btnRegisterOrLog.setOnClickListener {
      when (currentState) {
        State.LOGIN -> {
          if (checkText(etAccount, etPassword)) {
            presenter.login(etAccount.text.toString().trim(), etPassword.text.toString().trim())
          }
        }
        State.REGISTER -> {
          if (checkText(etAccount, etPassword, etVerifyCode)) {
            presenter.register(etAccount.text.toString().trim(), etPassword.text.toString().trim(),
              etVerifyCode.text.toString().trim())
          }
        }
        State.RESET_PASSWORD -> {
          if (checkText(etAccount, etPassword, etVerifyCode)) {
            presenter.resetPassword(etAccount.text.toString().trim(), etPassword.text.toString().trim(),
              etVerifyCode.text.toString().trim())
          }
        }
      }
    }
    tvForgetPassword.setOnClickListener { initView(State.RESET_PASSWORD) }
    tvRegisterNewAccount.setOnClickListener {
      when (currentState) {
        State.LOGIN -> initView(State.REGISTER)
        else -> initView(State.LOGIN)
      }
    }
  }

  private fun autoLogin() {
    val me = DeviceApplication.instance.me
    if (me.valid()) {
      presenter.login(me.name, me.password)
    }
  }

  private fun clearTypeIn() {
    etAccount.setText("")
    etPassword.setText("")
    etVerifyCode.setText("")
  }

  private fun checkText(vararg editTexts: EditText): Boolean {
    for (it in editTexts) {
      if (it.text.toString().trim().isEmpty()) {
        Toast.makeText(this, getString(R.string.empty_string), Toast.LENGTH_SHORT).show()
        return false
      }
    }
    return true
  }

  override fun verifyCodeResult(isSuccess: Boolean, message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }

  override fun registerResult(isSuccess: Boolean, message: String) {
    if (!isSuccess) {
      Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
      return
    }
  }

  override fun loginResult(isSuccess: Boolean, message: String) {
    if (!isSuccess) {
      Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
      return
    }
    startActivity(Intent(this, GroupActivity::class.java))
    finish()
  }

  override fun resetPasswordResult(isSuccess: Boolean, message: String) {
    if (!isSuccess) {
      Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
      return
    }
    Toast.makeText(this, R.string.reset_password_success, Toast.LENGTH_SHORT).show()
    initView(State.LOGIN)
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

enum class State {
  REGISTER,
  LOGIN,
  RESET_PASSWORD
}
