package com.zzz.www.smartdevice.bean

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @author zzz
 * @date create at 2018/1/2.
 */
@SuppressLint("ParcelCreator")
@Parcelize data class DeviceLocationInfo (var serialNumber : String = "",
                                          var collectDate : String = "",
                                          var latitudeGPS : String = "",
                                          var longitudeGPS : String = "",
                                          var latitudeLBS : String = "",
                                          var longitudeLBS : String = "") : Parcelable