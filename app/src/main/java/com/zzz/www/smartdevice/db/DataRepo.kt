package com.zzz.www.smartdevice.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import com.google.gson.Gson
import com.zzz.www.smartdevice.bean.Device
import com.zzz.www.smartdevice.bean.DeviceInfo
import com.zzz.www.smartdevice.bean.DeviceStatus
import com.zzz.www.smartdevice.bean.Group

/**
 * @author zzz
 * @date create at 2018/4/23.
 */
class DataRepo {
  companion object {
    private val instance: DataRepo by lazy { DataRepo() }
    private val gson: Gson = Gson()
    private var dbHelper: DBHelper? = null

    fun getInstance(context: Context): DataRepo {
      if (dbHelper == null) {
        dbHelper = DBHelper(context.applicationContext)
      }
      return instance
    }
  }

  fun getAllGroup(): ArrayList<Group> {
    val groups = arrayListOf<Group>()
    val db = dbHelper?.readableDatabase
    val cursor = db?.query(DBHelper.GROUP_TABLE_NAME, null, null, null,
      null, null, null)
    if (cursor != null && cursor.count > 0) {
      cursor.run {
        moveToFirst()
        while (!isAfterLast) {
          groups.add(Group().apply {
            id = getLong(getColumnIndexOrThrow(DBHelper.FIELD_GROUP_ID))
            name = getString(getColumnIndexOrThrow(DBHelper.FIELD_GROUP_NAME))
          })
          moveToNext()
        }
      }
    }
    cursor?.close()
    return groups
  }

  fun findGroup(group: Group): Group {
    val ret = Group()
    val db = dbHelper?.readableDatabase
    val cursor = db?.query(DBHelper.GROUP_TABLE_NAME, null,
      "${DBHelper.FIELD_GROUP_ID} = ? or ${DBHelper.FIELD_GROUP_NAME} = ?",
      arrayOf(group.id.toString(), group.name), null, null, null)
    if (cursor != null && cursor.count > 0) {
      cursor.run {
        moveToFirst()
        ret.apply {
          id = getLong(getColumnIndexOrThrow(DBHelper.FIELD_GROUP_ID))
          name = getString(getColumnIndexOrThrow(DBHelper.FIELD_GROUP_NAME))
        }
      }
    }
    cursor?.close()
    return ret
  }

  fun addGroup(newGroup: Group): Group {
    val ret = Group()
    val db = dbHelper?.writableDatabase
    val contentValues = ContentValues().apply {
      put(DBHelper.FIELD_GROUP_NAME, newGroup.name)
    }
    db?.run {
      ret.apply {
        name = newGroup.name
      }
      ret.id = insertWithOnConflict(DBHelper.GROUP_TABLE_NAME, null,
        contentValues, CONFLICT_REPLACE)
    }
    return ret
  }

  fun updateGroup(updatedGroup: Group): Group {
    val ret = Group()
    val db = dbHelper?.writableDatabase
    val contentValues = ContentValues().apply {
      put(DBHelper.FIELD_GROUP_NAME, updatedGroup.name)
    }
    db?.run {
      ret.apply {
        name = updatedGroup.name
      }
      if (update(DBHelper.GROUP_TABLE_NAME, contentValues, "${DBHelper.FIELD_GROUP_ID} = ?",
          arrayOf(updatedGroup.id.toString())) < 1) {
        ret.id = -1L
      }
    }
    return ret
  }

  fun updateOrInserGroup(newGroup: Group): Group {
    val foundGroup = findGroup(newGroup)
    foundGroup.name = newGroup.name
    return if (foundGroup.valid()) {
      updateGroup(foundGroup)
    } else {
      addGroup(foundGroup)
    }
  }

  fun deleteGroup(groupId: Long): Boolean {
    val db = dbHelper?.writableDatabase
    db?.run {
      return delete(DBHelper.GROUP_TABLE_NAME, "${DBHelper.FIELD_GROUP_ID} = ?",
        arrayOf(groupId.toString())) > 0
    }
    return false
  }

  fun findGroupDevices(groupId: Long): ArrayList<Device> {
    val ret = arrayListOf<Device>()
    val db = dbHelper?.readableDatabase
    val cursor = db?.run {
      query(DBHelper.DEVICE_TABLE_NAME, null, "${DBHelper.FIELD_DEVICE_GROUP} = ?",
        arrayOf(groupId.toString()), null, null, null)
    }
    if (cursor != null && cursor.count > 0) {
      cursor.run {
        moveToFirst()
        while (!isAfterLast) {
          ret.add(Device().apply {
            id = getLong(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_ID))
            sn = getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_SN))
            name = getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_NAME))
            group = getLong(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_GROUP))
            sumInfo = gson.fromJson(getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_LAST_SUM_INFO)),
              DeviceInfo::class.java)
            status = DeviceStatus.valueOf(getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_STATUS)))
          })
          moveToNext()
        }
      }
    }
    cursor?.close()
    return ret
  }

  fun findDevice(device: Device): Device {
    val ret = Device()
    val db = dbHelper?.readableDatabase
    device.sn = device.sn.toUpperCase()
    val cursor = db?.query(DBHelper.DEVICE_TABLE_NAME, null,
      "${DBHelper.FIELD_DEVICE_ID} = ? or ${DBHelper.FIELD_DEVICE_SN} = ?",
      arrayOf(device.id.toString(), device.sn), null, null, null)
    if (cursor != null && cursor.count > 0) {
      cursor.run {
        moveToFirst()
        ret.apply {
          id = getLong(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_ID))
          sn = getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_SN))
          name = getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_NAME))
          group = getLong(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_GROUP))
          sumInfo = gson.fromJson(getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_LAST_SUM_INFO)),
            DeviceInfo::class.java)
          status = DeviceStatus.valueOf(getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_STATUS)))
        }
      }
    }
    cursor?.close()
    return ret
  }

  fun updateDevice(device: Device): Device {
    var ret = Device()
    val db = dbHelper?.writableDatabase
    device.sn = device.sn.toUpperCase()
    val contentValues = ContentValues().apply {
      put(DBHelper.FIELD_DEVICE_NAME, device.name)
      put(DBHelper.FIELD_DEVICE_GROUP, device.group)
      put(DBHelper.FIELD_DEVICE_LAST_SUM_INFO, gson.toJson(device.sumInfo))
      put(DBHelper.FIELD_DEVICE_STATUS, device.status.name)
    }
    db?.run {
      ret = device.copy()
      if (update(DBHelper.DEVICE_TABLE_NAME, contentValues,
          "${DBHelper.FIELD_DEVICE_ID} = ? or ${DBHelper.FIELD_DEVICE_SN} = ?",
          arrayOf(device.id.toString(), device.sn)) < 1) {
        ret.id = -1L
      }
    }
    return ret
  }

  fun addDevice(device: Device): Device {
    val ret = device.copy()
    val db = dbHelper?.writableDatabase
    device.sn = device.sn.toUpperCase()
    val contentValues = ContentValues().apply {
      put(DBHelper.FIELD_DEVICE_SN, device.sn)
      put(DBHelper.FIELD_DEVICE_NAME, device.name)
      put(DBHelper.FIELD_DEVICE_GROUP, device.group)
      put(DBHelper.FIELD_DEVICE_LAST_SUM_INFO, gson.toJson(device.sumInfo))
      put(DBHelper.FIELD_DEVICE_STATUS, device.status.name)
    }
    db?.run {
      ret.id = insertWithOnConflict(DBHelper.DEVICE_TABLE_NAME, null,
        contentValues, CONFLICT_REPLACE)
    }
    return ret
  }

  fun updateOrInsertDevice(device: Device): Device {
    val foundDevice = findDevice(device)
    foundDevice.run {
      name = device.name
      group = device.group
      sn = device.sn
      sumInfo = device.sumInfo
      status = device.status
    }
    return if (foundDevice.valid()) {
      updateDevice(foundDevice)
    } else {
      addDevice(foundDevice)
    }
  }
}