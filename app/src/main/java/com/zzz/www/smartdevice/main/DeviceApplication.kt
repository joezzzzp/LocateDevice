package com.zzz.www.smartdevice.main

import android.app.Application
import com.baidu.mapapi.SDKInitializer
import com.facebook.stetho.Stetho

/**
 * @author zzz
 * @date create at 2018/4/25.
 */
class DeviceApplication : Application() {
  companion object {
    lateinit var instance: DeviceApplication
    var refreshAll = true
  }

  override fun onCreate() {
    super.onCreate()
    SDKInitializer.initialize(this)
    instance = this
    initStetho()
  }

  private fun initStetho() {
    Stetho.initialize(Stetho.newInitializerBuilder(this)
      .enableDumpapp({ Stetho.DefaultDumperPluginsBuilder(instance).finish() })
      .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
      .build())
  }
}