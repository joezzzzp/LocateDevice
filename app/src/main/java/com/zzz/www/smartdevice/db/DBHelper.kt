package com.zzz.www.smartdevice.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.zzz.www.smartdevice.bean.AVAILABLE
import com.zzz.www.smartdevice.bean.DELETED

/**
 * @author zzz
 * @date create at 2018/4/25.
 */
class DBHelper(context: Context):
  SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
  companion object {
    private const val DATABASE_VERSION = 1
    private const val DATABASE_NAME = "devices_database"

    //user table
    const val USER_TABLE_NAME = "user"
    const val FIELD_USER_ID = "id"
    const val FIELD_USER_NAME = "name"
    const val FIELD_USER_PASSWORD = "password"
    const val FIELD_USER_FLAG = "flag"

    //group table
    const val GROUP_TABLE_NAME = "group_info"
    const val FIELD_GROUP_ID = "id"
    const val FIELD_GROUP_NAME = "name"
    const val FIELD_GROUP_BELONG_TO = "user_id"
    const val FIELD_GROUP_FLAG = "flag"

    //device table
    const val DEVICE_TABLE_NAME = "device"
    const val FIELD_DEVICE_ID = "id"
    const val FIELD_DEVICE_SN = "sn"
    const val FIELD_DEVICE_NAME = "name"
    const val FIELD_DEVICE_GROUP = "group_id"
    const val FIELD_DEVICE_USER = "user_id"
    const val FIELD_DEVICE_FLAG = "flag"

    //device_info table
    const val DEVICE_INFO_TABLE_NAME = "device_info"
    const val FIELD_DEVICE_INFO_ID = "id"
    const val FIELD_DEVICE_INFO_SN = "sn"
    const val FIELD_DEVICE_INFO_DATA = "data"
    const val FIELD_DEVICE_INFO_TIME = "time"
    const val FIELD_DEVICE_INFO_FLAG = "flag"

    //create table sql
    private const val CREATE_USER_TABLE =
      "CREATE TABLE IF NOT EXISTS $USER_TABLE_NAME (" +
        "$FIELD_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "$FIELD_USER_NAME TEXT NOT NULL UNIQUE, " +
        "$FIELD_USER_PASSWORD TEXT NOT NULL, " +
        "$FIELD_USER_FLAG TEXT NOT NULL DEFAULT '$AVAILABLE')"

    private const val CREATE_GROUP_TABLE =
      "CREATE TABLE IF NOT EXISTS $GROUP_TABLE_NAME (" +
        "$FIELD_GROUP_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "$FIELD_GROUP_NAME TEXT NOT NULL, " +
        "$FIELD_GROUP_BELONG_TO INTEGER DEFAULT -1, " +
        "$FIELD_GROUP_FLAG TEXT NOT NULL DEFAULT '$AVAILABLE')"

    private const val CREATE_DEVICE_TABLE =
      "CREATE TABLE IF NOT EXISTS $DEVICE_TABLE_NAME (" +
        "$FIELD_DEVICE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "$FIELD_DEVICE_SN TEXT NOT NULL, " +
        "$FIELD_DEVICE_NAME TEXT NOT NULL, " +
        "$FIELD_DEVICE_GROUP INTEGER NOT NULL DEFAULT -1, " +
        "$FIELD_DEVICE_USER INTEGER NOT NULL DEFAULT -1, " +
        "$FIELD_DEVICE_FLAG TEXT NOT NULL DEFAULT '$AVAILABLE')"

    private const val CREATE_DEVICE_INFO_TABLE =
      "CREATE TABLE IF NOT EXISTS $DEVICE_INFO_TABLE_NAME (" +
        "$FIELD_DEVICE_INFO_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "$FIELD_DEVICE_INFO_SN TEXT NOT NULL, " +
        "$FIELD_DEVICE_INFO_DATA TEXT NOT NULL, " +
        "$FIELD_DEVICE_INFO_TIME INTEGER NOT NULL, " +
        "$FIELD_DEVICE_INFO_FLAG TEXT NOT NULL DEFAULT '$AVAILABLE')"
  }

  override fun onCreate(db: SQLiteDatabase?) {
    db?.run {
      execSQL(CREATE_USER_TABLE)
      execSQL(CREATE_GROUP_TABLE)
      execSQL(CREATE_DEVICE_TABLE)
      execSQL(CREATE_DEVICE_INFO_TABLE)
    }
  }

  override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

  }
}