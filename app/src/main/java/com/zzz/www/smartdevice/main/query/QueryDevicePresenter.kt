package com.zzz.www.smartdevice.main.query

import android.text.TextUtils
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.api.HttpConfig
import com.zzz.www.smartdevice.api.ServiceRepo
import com.zzz.www.smartdevice.bean.*
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import com.zzz.www.smartdevice.bean.DeviceLocationInfo
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author zzz
 * @date create at 2018/1/3.
 */
class QueryDevicePresenter(view: QueryDeviceContract.View) : QueryDeviceContract.Presenter(view) {
    private val expireTimeInterval = 30 * 1000

    override fun getToken() {
        val tokenRequest = TokenRequest()
        tokenRequest.corporateId = HttpConfig.cid
        tokenRequest.corporatePasswd = HttpConfig.password
        ServiceRepo.getInstance().getToken(tokenRequest)?.subscribeOn(Schedulers.io())?.
                observeOn(AndroidSchedulers.mainThread())?.
                subscribe(object : Subscriber<Token>() {
                    override fun onNext(t: Token?) {
                        if (HttpConfig.successCode == t?.respCode) {
                            HttpConfig.token = t.token
                            HttpConfig.expireTime = t.expireTime
                            view.getTokenSuccess()
                        } else {
                            view.getTokenFailed()
                        }
                    }

                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable?) {
                        view.getTokenFailed()
                    }

                })
    }

    override fun queryDeviceInfo(serialNumber: String) {
        val deviceInfoRequest = DeviceInfoRequest(serialNumber)
        ServiceRepo.getInstance().getDeviceInfoBySN(HttpConfig.token, HttpConfig.cid,
                deviceInfoRequest)?.subscribeOn(Schedulers.io())?.
                observeOn(AndroidSchedulers.mainThread())?.
                subscribe(object : Subscriber<BaseResponseBody<DeviceInfo>>() {
                    override fun onCompleted() {
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        view.queryDeviceFailed("")
                    }

                    override fun onNext(t: BaseResponseBody<DeviceInfo>?) {
                        if (t?.respCode == HttpConfig.successCode) {
                            t.data?.let {
                                view.queryDeviceSuccess(getLocationInfo(it))
                            }
                        } else {
                            var errorMessage = ""
                            if(t?.respCode == HttpConfig.deviceSnErrorCode) {
                                when(view) {
                                    is MainActivity -> errorMessage = (view as MainActivity)
                                            .getString(R.string.device_sn_does_not_exist)
                                }
                                view.queryDeviceFailed(errorMessage)
                            }
                        }
                    }
                })
    }

    private fun getLocationInfo(deviceInfo: DeviceInfo) : DeviceLocationInfo {
        val locationInfo = DeviceLocationInfo()
        locationInfo.serialNumber = deviceInfo.deviceNum
        val formats = "yyyy-MM-dd HH:mm:ss"
        val date = SimpleDateFormat(formats, Locale.CHINA)
                .format(Date(deviceInfo.collectDate))
        locationInfo.collectDate = date
        val items = deviceInfo.items
        if (items != null && items.isNotEmpty()) {
            for (item in items) {
                val fieldId = item.fieldId
                val hexValues = item.hexValues
                when (fieldId) {
                    "1792" -> locationInfo.latitudeLBS = hexValues
                    "087B" -> locationInfo.longitudeLBS = hexValues
                    "1480" -> locationInfo.latitudeGPS = hexValues
                    "00EA" -> locationInfo.longitudeGPS = hexValues
                }
            }
        }
        return locationInfo
    }

    fun shouldRefreshToken() : Boolean {
        return TextUtils.isEmpty(HttpConfig.token) ||
                System.currentTimeMillis() - expireTimeInterval >= HttpConfig.expireTime
    }
}