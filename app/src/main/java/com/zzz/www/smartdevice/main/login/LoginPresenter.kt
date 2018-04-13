package com.zzz.www.smartdevice.main.login

import android.content.Context
import android.widget.Toast
import com.tckj.zyfsdk.ZYFSdk
import com.tckj.zyfsdk.entity.BaseEntity
import com.tckj.zyfsdk.entity.CodeEntity
import com.tckj.zyfsdk.entity.LoginEntity
import com.tckj.zyfsdk.http.zhttp.listener.ZYFGetAuthCodeListener
import com.tckj.zyfsdk.http.zhttp.listener.ZYFLoginListener
import com.tckj.zyfsdk.http.zhttp.listener.ZYFRegisterListener
import com.tckj.zyfsdk.http.zhttp.listener.ZYFResetPasswordListener
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.api.HttpConfig
import com.zzz.www.smartdevice.api.ZYFRepo
import com.zzz.www.smartdevice.bean.TokenRequest
import com.zzz.www.smartdevice.bean.TokenResponse
import com.zzz.www.smartdevice.main.DeviceApplication
import rx.Subscriber
import java.lang.Exception
import java.util.regex.Pattern

/**
 * @author zzz
 * @date create at 2018/4/18.
 */
class LoginPresenter(view: LoginContract.View) : LoginContract.Presenter(view) {
  private val phoneRegex: String = "^1[0-9]{10}\$"
  private var context: Context = view as Context
  private var lastVerifyCode: String = ""
  private var lastRegisterPhoneNumber: String = ""

  override fun getVerifyCode(phoneNumber: String, isRegister: Boolean) {
    view.showLoading()
    if (!checkPhoneNumber(phoneNumber)) return
    lastRegisterPhoneNumber = phoneNumber
    ZYFSdk.getInstance().getAuthCode(if (isRegister) phoneNumber else "u$phoneNumber",
      object : ZYFGetAuthCodeListener {
      override fun onComplete(entity: CodeEntity?) {
        view.hideLoading()
        entity?.run {
          if (isRtState) {
            lastVerifyCode = rtData
            view.verifyCodeResult(true, entity.rtMsg)
          } else {
            view.verifyCodeResult(false, entity.rtMsg)
          }
        }
      }

      override fun onError(exception: Exception?) {
        lastVerifyCode = ""
        view.run {
          hideLoading()
          verifyCodeResult(false, "")
        }
      }
    })
  }

  override fun register(phoneNumber: String, password: String, verifyCode: String) {
    view.showLoading()
    if (!checkPhoneNumber(phoneNumber)) return
    if (phoneNumber != lastRegisterPhoneNumber) {
      view.run {
        hideLoading()
        registerResult(false, "")
      }
      return
    }
    if (verifyCode != lastVerifyCode) {
      view.run {
        hideLoading()
        registerResult(false, "")
      }
      return
    }
    ZYFSdk.getInstance().register(phoneNumber, password, object : ZYFRegisterListener {
      override fun onComplete(entity: BaseEntity?) {
        entity?.run {
          if (isRtState) {
            view.hideLoading()
            login(phoneNumber, password)
          } else {
            view.run {
              hideLoading()
              registerResult(false, entity.rtMsg)
            }
          }
        }
      }

      override fun onError(exception: Exception?) {
        view.run {
          hideLoading()
          registerResult(false, "")
        }
      }
    })
  }

  override fun login(phoneNumber: String, password: String) {
    view.showLoading()
    if (!checkPhoneNumber(phoneNumber)) return
    ZYFSdk.getInstance().login(phoneNumber, password, object : ZYFLoginListener {
      override fun onComplete(entity: LoginEntity?) {
        entity?.run {
          if (isRtState) {
            ZYFRepo.get().getToken(TokenRequest()).subscribe(object : Subscriber<TokenResponse>() {
              override fun onNext(t: TokenResponse?) {
                view.hideLoading()
                t?.run {
                  if (respCode == HttpConfig.successCode) {
                    HttpConfig.token = t.token
                    view.loginResult(true, "")
                    DeviceApplication.instance.run {
                      me.name = phoneNumber
                      me.password = password
                      syncMe()
                      customerID = entity.rtData.customerUser.sid.toString()
                    }
                    return
                  }
                }
                view.loginResult(false, "")
              }

              override fun onCompleted() {
              }

              override fun onError(e: Throwable?) {
                view.loginResult(false, "")
              }

            })
          } else {
            view.run {
              hideLoading()
              view.loginResult(false, entity.rtMsg)
            }
          }
        }
      }

      override fun onError(exception: Exception?) {
        view.run {
          hideLoading()
          exception?.run {
            loginResult(false, if (message.isNullOrEmpty()) "" else message!!)
          }
        }
      }
    })
  }

  override fun resetPassword(phoneNumber: String, password: String, verifyCode: String) {
    view.showLoading()
    if (!checkPhoneNumber(phoneNumber)) return
    if (phoneNumber != lastRegisterPhoneNumber) {
      view.run {
        hideLoading()
        resetPasswordResult(false, "")
      }
      return
    }
    if (verifyCode != lastVerifyCode) {
      view.run {
        hideLoading()
        resetPasswordResult(false, "")
      }
      return
    }
    ZYFSdk.getInstance().resetPassword(phoneNumber, password, object : ZYFResetPasswordListener {
      override fun onComplete(entity: CodeEntity?) {
        entity?.run {
          if (isRtState) {
            DeviceApplication.instance.logout()
            view.run {
              hideLoading()
              resetPasswordResult(true, entity.rtMsg)
            }
          } else {
            view.run {
              hideLoading()
              resetPasswordResult(false, entity.rtMsg)
            }
          }
        }
      }

      override fun onError(exception: Exception?) {
        view.run {
          hideLoading()
          exception?.run {
            resetPasswordResult(false, if (message.isNullOrEmpty()) "" else message!!)
          }
        }
      }
    })
  }

  private fun checkPhoneNumber(phone: String): Boolean {
    if (!Pattern.matches(phoneRegex, phone)) {
      view.hideLoading()
      Toast.makeText(context, context.getString(R.string.invalid_phone), Toast.LENGTH_SHORT).show()
      return false
    }
    return true
  }
}