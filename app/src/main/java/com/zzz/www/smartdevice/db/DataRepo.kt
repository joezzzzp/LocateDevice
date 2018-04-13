package com.zzz.www.smartdevice.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zzz.www.smartdevice.bean.*
import com.zzz.www.smartdevice.main.DeviceApplication

/**
 * @author zzz
 * @date create at 2018/4/23.
 */
class DataRepo {
  companion object {
    private val instance: DataRepo by lazy { DataRepo() }
    private var dbHelper: DBHelper? = null
    private val gson = Gson()

    fun getInstance(context: Context): DataRepo {
      if (dbHelper == null) {
        dbHelper = DBHelper(context.applicationContext)
      }
      return instance
    }
  }
  private var me = User()

  private fun checkMe(): Boolean {
    me = DeviceApplication.instance.me
    return me.id >= 0
  }

  private fun findUser(user: User): User {
    val ret = User()
    val db = dbHelper?.readableDatabase
    val cursor = db?.query(DBHelper.USER_TABLE_NAME, null,
      "${DBHelper.FIELD_USER_ID} = ? or ${DBHelper.FIELD_USER_NAME} = ?",
      arrayOf(user.id.toString(), user.name), null, null, null)
    if (cursor != null && cursor.count > 0) {
      cursor.run {
        moveToFirst()
        ret.apply {
          id = getLong(getColumnIndexOrThrow(DBHelper.FIELD_USER_ID))
          this.name = getString(getColumnIndexOrThrow(DBHelper.FIELD_USER_NAME))
          password = getString(getColumnIndexOrThrow(DBHelper.FIELD_USER_PASSWORD))
          flag = getString(getColumnIndexOrThrow(DBHelper.FIELD_USER_FLAG))
        }
      }
    }
    cursor?.close()
    return ret
  }

  private fun addUser(user: User): User {
    var ret = User()
    val db = dbHelper?.writableDatabase
    val contentValues = ContentValues().apply {
      put(DBHelper.FIELD_USER_NAME, user.name)
      put(DBHelper.FIELD_USER_PASSWORD, user.password)
      put(DBHelper.FIELD_USER_FLAG, AVAILABLE)
    }
    db?.run {
      ret = user.copy().apply {
        id = insertWithOnConflict(DBHelper.USER_TABLE_NAME, null, contentValues,
          CONFLICT_REPLACE)
      }
    }
    return ret
  }

  private fun updateUser(user: User): User {
    var ret = User()
    val db = dbHelper?.writableDatabase
    val contentValues = ContentValues().apply {
      put(DBHelper.FIELD_USER_NAME, user.name)
      put(DBHelper.FIELD_USER_PASSWORD, user.password)
      put(DBHelper.FIELD_USER_FLAG, user.flag)
    }
    db?.run {
      ret = user.copy()
      if (updateWithOnConflict(DBHelper.USER_TABLE_NAME, contentValues,
          "${DBHelper.FIELD_USER_ID} = ?", arrayOf(user.id.toString()), CONFLICT_REPLACE) < 1) {
        ret.id = -1L
      }
    }
    return ret
  }

  fun updateOrAddUser(user: User): User {
    val loginUser = findUser(user)
    return if (loginUser.valid()) {
      loginUser.run {
        name = user.name
        password = user.password
      }
      updateUser(loginUser)
    } else {
      addUser(user)
    }
  }

  fun getAllGroup(): ArrayList<Group> {
    val groups = arrayListOf<Group>()
    if (checkMe()) {
      val db = dbHelper?.readableDatabase
      val cursor = db?.query(DBHelper.GROUP_TABLE_NAME, null,
        "${DBHelper.FIELD_GROUP_BELONG_TO} = ? and ${DBHelper.FIELD_GROUP_FLAG} = ?",
        arrayOf(me.id.toString(), AVAILABLE), null, null, null)
      if (cursor != null && cursor.count > 0) {
        cursor.run {
          moveToFirst()
          while (!isAfterLast) {
            groups.add(Group().apply {
              id = getLong(getColumnIndexOrThrow(DBHelper.FIELD_GROUP_ID))
              name = getString(getColumnIndexOrThrow(DBHelper.FIELD_GROUP_NAME))
              user = getLong(getColumnIndexOrThrow(DBHelper.FIELD_GROUP_BELONG_TO))
              flag = getString(getColumnIndexOrThrow(DBHelper.FIELD_GROUP_FLAG))
            })
            moveToNext()
          }
        }
      }
      cursor?.close()
    }
    return groups
  }

  fun findGroup(group: Group): Group {
    val ret = Group()
    if (checkMe()) {
      val db = dbHelper?.readableDatabase
      val cursor = db?.query(DBHelper.GROUP_TABLE_NAME, null,
        "${DBHelper.FIELD_GROUP_ID} = ? and ${DBHelper.FIELD_GROUP_BELONG_TO} = ?" +
          " and ${DBHelper.FIELD_GROUP_FLAG} = ?",
        arrayOf(group.id.toString(), me.id.toString(), AVAILABLE), null, null, null)
      if (cursor != null && cursor.count > 0) {
        cursor.run {
          moveToFirst()
          ret.apply {
            id = getLong(getColumnIndexOrThrow(DBHelper.FIELD_GROUP_ID))
            name = getString(getColumnIndexOrThrow(DBHelper.FIELD_GROUP_NAME))
            user = getLong(getColumnIndexOrThrow(DBHelper.FIELD_GROUP_BELONG_TO))
            flag = getString(getColumnIndexOrThrow(DBHelper.FIELD_GROUP_FLAG))
          }
        }
      }
      cursor?.close()
    }
    return ret
  }

  fun addGroup(newGroup: Group): Group {
    var ret = Group()
    if (checkMe()) {
      val db = dbHelper?.writableDatabase
      val contentValues = ContentValues().apply {
        put(DBHelper.FIELD_GROUP_NAME, newGroup.name)
        put(DBHelper.FIELD_GROUP_BELONG_TO, me.id)
        put(DBHelper.FIELD_GROUP_FLAG, AVAILABLE)
      }
      db?.run {
        ret.apply {
          name = newGroup.name
          user = me.id
          flag = AVAILABLE
        }
        ret.id = insertWithOnConflict(DBHelper.GROUP_TABLE_NAME, null,
          contentValues, CONFLICT_REPLACE)
      }
    }
    return ret
  }

  fun updateGroup(updatedGroup: Group): Group {
    val ret = Group()
    if (checkMe()) {
      val db = dbHelper?.writableDatabase
      val contentValues = ContentValues().apply {
        put(DBHelper.FIELD_GROUP_NAME, updatedGroup.name)
        put(DBHelper.FIELD_GROUP_BELONG_TO, me.id)
        put(DBHelper.FIELD_GROUP_FLAG, updatedGroup.flag)
      }
      db?.run {
        ret.apply {
          name = updatedGroup.name
          user = me.id
          flag = AVAILABLE
        }
        if (update(DBHelper.GROUP_TABLE_NAME, contentValues,
            "${DBHelper.FIELD_GROUP_ID} = ? and ${DBHelper.FIELD_GROUP_BELONG_TO} = ?",
            arrayOf(updatedGroup.id.toString(), me.id.toString())) < 1) {
          ret.id = -1L
        }
      }
    }
    return ret
  }

  fun updateOrInserGroup(newGroup: Group): Group {
    val findedGroup = findGroup(newGroup)
    return if (findedGroup.valid()) {
      updateGroup(findedGroup)
    } else {
      addGroup(findedGroup)
    }
  }

  fun deleteGroup(groupId: Long): Boolean {
    var deleteSuccess = false
    if (checkMe()) {
      val db = dbHelper?.writableDatabase
      val contentValues = ContentValues().apply {
        put(DBHelper.FIELD_GROUP_FLAG, DELETED)
      }
      db?.beginTransaction()
      try {
        db?.run {
          val affectedGroupRows = updateWithOnConflict(DBHelper.GROUP_TABLE_NAME, contentValues,
            "${DBHelper.FIELD_GROUP_ID} = ? and ${DBHelper.FIELD_GROUP_BELONG_TO} = ?",
            arrayOf(groupId.toString(), me.id.toString()), CONFLICT_REPLACE)
          if (affectedGroupRows > 0) {
            val devicesContentValues = ContentValues().apply {
              put(DBHelper.FIELD_DEVICE_GROUP, -1L)
            }
            updateWithOnConflict(DBHelper.DEVICE_TABLE_NAME, devicesContentValues,
              "${DBHelper.FIELD_DEVICE_GROUP} = ? and ${DBHelper.FIELD_DEVICE_USER} = ?",
              arrayOf(groupId.toString(), me.id.toString()), CONFLICT_REPLACE)
            setTransactionSuccessful()
            deleteSuccess = true
          }
        }
      } catch (exception: Exception) {
        deleteSuccess = false
      } finally {
        db?.endTransaction()
      }
    }
    return deleteSuccess
  }

  fun findGroupDevices(groupId: Long): ArrayList<Device> {
    val ret = arrayListOf<Device>()
    if (checkMe()) {
      val db = dbHelper?.readableDatabase
      val cursor = db?.run {
        query(DBHelper.DEVICE_TABLE_NAME, null, "${DBHelper.FIELD_DEVICE_GROUP} = ? and " +
          "${DBHelper.FIELD_DEVICE_USER} = ? and ${DBHelper.FIELD_DEVICE_FLAG} = ?",
          arrayOf(groupId.toString(), me.id.toString(), AVAILABLE), null, null, null)
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
              user = getLong(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_USER))
            })
            moveToNext()
          }
        }
      }
      cursor?.close()
    }
    return ret
  }

  fun findDevice(device: Device): Device {
    val ret = Device()
    if (checkMe()) {
      val db = dbHelper?.readableDatabase
      val cursor = db?.query(DBHelper.DEVICE_TABLE_NAME, null,
        "(${DBHelper.FIELD_DEVICE_ID} = ? or ${DBHelper.FIELD_DEVICE_SN} = ?)" +
          " and ${DBHelper.FIELD_DEVICE_USER} = ?",
        arrayOf(device.id.toString(), device.sn, me.id.toString()), null, null, null)
      if (cursor != null && cursor.count > 0) {
        cursor.run {
          moveToFirst()
          ret.apply {
            id = getLong(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_ID))
            sn = getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_SN))
            name = getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_NAME))
            group = getLong(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_GROUP))
            user = getLong(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_USER))
            flag = getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_FLAG))
          }
        }
      }
      cursor?.close()
    }
    return ret
  }

  fun updateDevice(device: Device): Device {
    var ret = Device()
    if (checkMe()) {
      val db = dbHelper?.writableDatabase
      val contentValues = ContentValues().apply {
        put(DBHelper.FIELD_DEVICE_NAME, device.name)
        put(DBHelper.FIELD_DEVICE_GROUP, device.group)
        put(DBHelper.FIELD_DEVICE_USER, device.user)
        put(DBHelper.FIELD_DEVICE_FLAG, device.flag)
      }
      db?.run {
        ret = device.copy()
        if (update(DBHelper.DEVICE_TABLE_NAME, contentValues,
            "(${DBHelper.FIELD_DEVICE_ID} = ? or ${DBHelper.FIELD_DEVICE_SN} = ?) and " +
              "${DBHelper.FIELD_DEVICE_USER} = ?",
            arrayOf(device.id.toString(), device.sn, me.id.toString())) < 1) {
          ret.id = -1L
        }
      }
    }
    return ret
  }

  fun addDevice(device: Device): Device {
    var ret = Device()
    if (checkMe()) {
      ret = device.copy()
      val db = dbHelper?.writableDatabase
      val contentValues = ContentValues().apply {
        put(DBHelper.FIELD_DEVICE_SN, device.sn)
        put(DBHelper.FIELD_DEVICE_NAME, device.name)
        put(DBHelper.FIELD_DEVICE_GROUP, device.group)
        put(DBHelper.FIELD_DEVICE_USER, me.id)
        put(DBHelper.FIELD_DEVICE_FLAG, device.flag)
      }
      db?.run {
        ret.id = insertWithOnConflict(DBHelper.DEVICE_TABLE_NAME, null,
          contentValues, CONFLICT_REPLACE)
      }
    }
    return ret
  }

  fun findAllDevices(): ArrayList<Device> {
    return arrayListOf()
  }

  fun addDeviceInfo(deviceInfo: DeviceInfo): DeviceInfo {
    val ret = deviceInfo.copy()
    val db = dbHelper?.writableDatabase
    val contentValues = ContentValues().apply {
      put(DBHelper.FIELD_DEVICE_INFO_SN, deviceInfo.sn)
      put(DBHelper.FIELD_DEVICE_INFO_DATA, gson.toJson(deviceInfo.data))
      put(DBHelper.FIELD_DEVICE_INFO_TIME, deviceInfo.time)
      put(DBHelper.FIELD_DEVICE_INFO_FLAG, deviceInfo.flag)
    }
    db?.run {
      ret.id = insertWithOnConflict(DBHelper.DEVICE_INFO_TABLE_NAME, null,
        contentValues, CONFLICT_REPLACE)
    }
    return ret
  }

  fun getDeviceInfosByID(deviceId: Long, pageSize: Int, pageNumber: Int): ArrayList<DeviceInfo> {
    val ret = arrayListOf<DeviceInfo>()
    if (checkMe()) {
      val db = dbHelper?.readableDatabase
      val cursor = db?.query(DBHelper.DEVICE_INFO_TABLE_NAME, null,
        "${DBHelper.FIELD_DEVICE_INFO_ID} = ? and ${DBHelper.FIELD_DEVICE_INFO_FLAG} = ?",
        arrayOf(deviceId.toString(), AVAILABLE), null, null,
        "${DBHelper.FIELD_DEVICE_INFO_TIME} ASC",
        "${pageNumber * (pageSize - 1)}, $pageNumber")
      if (cursor != null && cursor.count > 0) {
        cursor.run {
          moveToFirst()
          while (!isAfterLast) {
            ret.add(DeviceInfo().apply {
              id = getLong(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_INFO_ID))
              sn = getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_INFO_SN))
              time = getLong(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_INFO_TIME))
              data = gson.fromJson(getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_INFO_DATA)),
               object: TypeToken<ArrayList<InfoItem>>(){}.type)
            })
            moveToNext()
          }
        }

      }
      cursor?.close()
    }
    return ret
  }

  fun getDeviceInfosBySN(sn: String, pageNumber: Int, pageSize: Int): ArrayList<DeviceInfo> {
    val ret = arrayListOf<DeviceInfo>()
    if (checkMe()) {
      val db = dbHelper?.readableDatabase
      val cursor = db?.query(DBHelper.DEVICE_INFO_TABLE_NAME, null,
        "${DBHelper.FIELD_DEVICE_INFO_SN} = ? and ${DBHelper.FIELD_DEVICE_INFO_FLAG} = ?",
        arrayOf(sn, AVAILABLE), null, null,
        "${DBHelper.FIELD_DEVICE_INFO_TIME} DESC",
        "${pageSize * (pageNumber - 1)}, $pageSize")
      if (cursor != null && cursor.count > 0) {
        cursor.run {
          moveToFirst()
          while (!isAfterLast) {
            ret.add(DeviceInfo().apply {
              id = getLong(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_INFO_ID))
              this.sn = getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_INFO_SN))
              time = getLong(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_INFO_TIME))
              data = gson.fromJson(getString(getColumnIndexOrThrow(DBHelper.FIELD_DEVICE_INFO_DATA)),
                object: TypeToken<ArrayList<InfoItem>>(){}.type)
            })
            moveToNext()
          }
        }
      }
      cursor?.close()
    }
    return ret
  }
}