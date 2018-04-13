package com.zzz.www.smartdevice.widget

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import com.zzz.www.smartdevice.R
import rx.Observable
import rx.Subscription
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author zzz
 * @date create at 2018/1/4.
 */
class ProgressDialog(context: Context) {
    private var dialog: MaterialDialog? = null

    // 显示计数, 避免显示多个
    private var showingCount: AtomicInteger = AtomicInteger(0)
    private var timeoutSubscription: Subscription? = null


    init {
        dialog = MaterialDialog.Builder(context)
                .content(context.getString(R.string.progress_dialog_loading_text))
                .progress(true, 0)
                .dismissListener({ showingCount.set(0) })
                .cancelable(false)
                .build()
    }

    fun show() {
        dialog!!.show()
        showingCount.getAndIncrement()
        timeout()
    }

    fun show(text: String) {
        dialog!!.setCancelable(false)
        dialog!!.setContent(text)
        dialog!!.show()
        showingCount.getAndIncrement()
        timeout()
    }

    private fun timeout() {
        if (timeoutSubscription != null && !timeoutSubscription!!.isUnsubscribed) {
            timeoutSubscription!!.unsubscribe()
        }
        timeoutSubscription = Observable.just("timeout")
                .delay(10, TimeUnit.SECONDS)
                .subscribe {
                    if (dialog != null && dialog!!.isShowing) {
                        dialog!!.setCancelable(true)
                    }
                }
    }

    fun dismiss() {
        val count = showingCount.decrementAndGet()
        // 显示计数为0时才真正关闭
        if (count <= 0) {
            dialog!!.dismiss()
            showingCount.set(0)
        }
    }

    /**
     * 强制关闭, 忽略showingCount
     */
    fun forceDismiss() {
        dialog!!.dismiss()
        showingCount.set(0)
    }

}