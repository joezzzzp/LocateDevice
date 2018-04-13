package com.zzz.www.smartdevice.api

/**
 * @author zzz
 * @date create at 2018/1/3.
 */
class HttpConfig {

    companion object {
        val baseUrl : String = "https://api.hizyf.com/DM-open-service/"
        val account : String = "1445266146@QQ.com"
        val password : String = "123456"
        val cid : String = "5db57c93b1d246fa999a60e32130c81b"
        val successCode : String = "00000000"
        val deviceSnErrorCode : String = "10000002"

        var token : String = ""
        var expireTime : Long = 0
    }
}