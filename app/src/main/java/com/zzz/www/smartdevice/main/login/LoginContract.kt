package com.zzz.www.smartdevice.main.login

import com.zzz.www.smartdevice.base.BasePresenter
import com.zzz.www.smartdevice.base.BaseView

/**
 * @author zzz
 * @date create at 2018/4/18.
 */
interface LoginContract {
  abstract class Presenter(protected var view: View): BasePresenter() {
    abstract fun getVerifyCode(phoneNumber: String, isRegister: Boolean)
    abstract fun register(phoneNumber: String, password: String, verifyCode: String)
    abstract fun login(phoneNumber: String, password: String)
    abstract fun resetPassword(phoneNumber: String, password: String, verifyCode: String)
  }

  interface View: BaseView {
    fun verifyCodeResult(isSuccess: Boolean, message: String)
    fun registerResult(isSuccess: Boolean, message: String)
    fun loginResult(isSuccess: Boolean, message: String)
    fun resetPasswordResult(isSuccess: Boolean, message: String)
  }
}