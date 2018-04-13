package com.zzz.www.smartdevice.main.show

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.zzz.www.smartdevice.R
import com.zzz.www.smartdevice.bean.DeviceLocationInfo
import kotlinx.android.synthetic.main.activity_device_location_info.*

/**
 * @author zzz
 * @date create at 2018/1/4.
 */
class DeviceLocationInfoActivity : AppCompatActivity() {

    private lateinit var adapter : DeviceLocationInfoAdapter

    companion object {
        private val dataKey : String = "DEVICE_LOCATION_INFO_DATA"

        fun start(context: Context, deviceLocationInfo: DeviceLocationInfo) {
            val intent = Intent(context, DeviceLocationInfoActivity::class.java)
            intent.putExtra(dataKey, deviceLocationInfo)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_location_info)

        val deviceLocationInfo : DeviceLocationInfo = intent.getParcelableExtra(dataKey)
        adapter = DeviceLocationInfoAdapter(this,
                deviceLocationInfo2Items(deviceLocationInfo))
        rvDeviceInfo.layoutManager = LinearLayoutManager(this)
        rvDeviceInfo.adapter = adapter
    }

    private fun deviceLocationInfo2Items(deviceLocationInfo: DeviceLocationInfo) :
            ArrayList<Pair<String, String>> {
        val labels : Array<String> = resources.getStringArray(
                R.array.device_location_info_label_array)
        val items : ArrayList<Pair<String, String>> = ArrayList()
        items.add(Pair(labels[0], deviceLocationInfo.serialNumber))
        items.add(Pair(labels[1], deviceLocationInfo.collectDate))
        items.add(Pair(labels[2], deviceLocationInfo.latitudeLBS))
        items.add(Pair(labels[3], deviceLocationInfo.longitudeLBS))
        items.add(Pair(labels[4], deviceLocationInfo.latitudeGPS))
        items.add(Pair(labels[5], deviceLocationInfo.longitudeGPS))
        return items
    }
}