package com.zzz.www.smartdevice.api

/**
 * @author zzz
 * @date create at 2018/1/3.
 */
class HttpConfig {

  companion object {
    const val baseUrl: String = "https://api.hizyf.com/DM-open-service/"
    const val account: String = "15300030920"
    const val corporateId: String = "305f55e28c334b958777f9c037a02cd9"
    const val corporatePassword: String = "1q2w3e4r"
    const val successCode: String = "00000000"
    const val deviceSnErrorCode: String = "10000002"

    var token: String = ""
    var expireTime: Long = 0

    var sn: String = "001221A00B39"
    var sn1: String = "0011613003c8"
  }
}