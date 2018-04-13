package com.zzz.www.smartdevice.main

import android.app.Application
import com.baidu.mapapi.SDKInitializer
import com.facebook.stetho.Stetho
import com.zzz.www.smartdevice.bean.User
import com.zzz.www.smartdevice.db.DataRepo
import com.zzz.www.smartdevice.utils.PrefsUtil

/**
 * @author zzz
 * @date create at 2018/4/25.
 */
class DeviceApplication : Application() {
  companion object {
    lateinit var instance: DeviceApplication
  }

  lateinit var me: User
  var customerID: String = ""

  override fun onCreate() {
    super.onCreate()
    SDKInitializer.initialize(this)
    instance = this
    me = PrefsUtil.getInstance(this).getMe() ?: User()
    initStetho()
  }

  private fun initStetho() {
    Stetho.initialize(Stetho.newInitializerBuilder(this)
      .enableDumpapp({ Stetho.DefaultDumperPluginsBuilder(instance).finish() })
      .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
      .build())
  }

  fun syncMe() {
    me = DataRepo.getInstance(this).updateOrAddUser(me)
    PrefsUtil.getInstance(this).saveMe(me)
  }

  fun logout() {
    me = User()
    PrefsUtil.getInstance(this).saveMe(me)
  }
}