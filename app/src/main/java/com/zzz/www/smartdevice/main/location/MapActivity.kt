package com.zzz.www.smartdevice.main.location

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatus
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.model.LatLng
import com.zzz.www.smartdevice.R
import kotlinx.android.synthetic.main.activity_location_on_map.*

/**
 * @author zzz
 * @date create at 2018/5/16.
 */
class MapActivity : AppCompatActivity(){

  private lateinit var location: LatLng

  companion object {
    private const val LOCATION_KEY = "LOCATION_KEY"

    fun start(context: Context, location: LatLng) {
      val intent = Intent(context, MapActivity::class.java)
      intent.putExtra(LOCATION_KEY, location)
      context.startActivity(intent)
    }
  }

  private fun showPosition(position: LatLng?) {
    position?.run {
      val marker = BitmapDescriptorFactory.fromResource(R.drawable.icon_mark)
      val option = MarkerOptions().position(position).icon(marker)
      mapView.map.run {
        addOverlay(option)
        animateMapStatus(MapStatusUpdateFactory.newMapStatus(
            MapStatus.Builder().target(position).zoom(18f).build()))
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_location_on_map)
    location = (intent.getParcelableExtra(LOCATION_KEY) as LatLng)
    showPosition(location)
  }

  override fun onResume() {
    super.onResume()
    mapView.onResume()
  }

  override fun onPause() {
    super.onPause()
    mapView.onPause()
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.onDestroy()
  }
}