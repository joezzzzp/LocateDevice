package com.zzz.www.smartdevice.api

import com.tckj.zyfsdk.ZYFSdk
import com.tckj.zyfsdk.ZYFSdkListener
import com.tckj.zyfsdk.entity.BaseEntity
import com.tckj.zyfsdk.http.zhttp.listener.ZYFBindDeviceListener
import com.tckj.zyfsdk.http.zhttp.listener.ZYFRegisterListener
import rx.Observable
import rx.Observer
import rx.Subscriber
import java.lang.Exception

/**
 * @author zzz
 * @date create at 2018/4/18.
 */
class ZYFJob: Observable.OnSubscribe<BaseEntity>, ZYFSdkListener<BaseEntity> {

  private var subscriber: Subscriber<in BaseEntity>? = null

  override fun onComplete(entity: BaseEntity?) {
    subscriber?.onNext(entity)
    subscriber?.onCompleted()
  }

  override fun onError(exception: Exception?) {
    subscriber?.onError(exception)
  }

  override fun call(t: Subscriber<in BaseEntity>?) {
    this.subscriber = t

  }

}