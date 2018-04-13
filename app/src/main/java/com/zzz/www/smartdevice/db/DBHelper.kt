package com.zzz.www.smartdevice.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * @author zzz
 * @date create at 2018/4/25.
 */
class DBHelper(context: Context):
  SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
  companion object {
    private const val DATABASE_VERSION = 1
    private const val DATABASE_NAME = "devices_database"

    //group table
    const val GROUP_TABLE_NAME = "group_info"
    const val FIELD_GROUP_ID = "id"
    const val FIELD_GROUP_NAME = "name"

    //device table
    const val DEVICE_TABLE_NAME = "device"
    const val FIELD_DEVICE_ID = "id"
    const val FIELD_DEVICE_SN = "sn"
    const val FIELD_DEVICE_NAME = "name"
    const val FIELD_DEVICE_GROUP = "group_id"
    const val FIELD_DEVICE_LAST_UPDATE_TIME = "last_update_time"

    //device history info table
    const val HISTORY_TABLE_NAME = "history"
    const val FIELD_HISTORY_ID = "id"
    const val FIELD_HISTORY_DEVICE_ID = "device_id"
    const val FIELD_HISTORY_SN = "device_sn"
    const val FIELD_HISTORY_TIME = "time"
    const val FIELD_HISTORY_DATA = "data"

    //create table sql
    private const val CREATE_GROUP_TABLE =
      "CREATE TABLE IF NOT EXISTS $GROUP_TABLE_NAME (" +
        "$FIELD_GROUP_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "$FIELD_GROUP_NAME TEXT NOT NULL)"

    private const val CREATE_DEVICE_TABLE =
      "CREATE TABLE IF NOT EXISTS $DEVICE_TABLE_NAME (" +
        "$FIELD_DEVICE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "$FIELD_DEVICE_SN TEXT NOT NULL, " +
        "$FIELD_DEVICE_NAME TEXT NOT NULL, " +
        "$FIELD_DEVICE_LAST_UPDATE_TIME INTEGER NOT NULL, " +
        "$FIELD_DEVICE_GROUP INTEGER NOT NULL DEFAULT -1)"

    private const val CREATE_HISTORY_TABLE =
      "CREATE TABLE IF NOT EXISTS $HISTORY_TABLE_NAME (" +
        "$FIELD_HISTORY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "$FIELD_HISTORY_DEVICE_ID INTEGER NOT NULL, " +
        "$FIELD_HISTORY_SN TEXT NOT NULL, " +
        "$FIELD_HISTORY_TIME INTEGER NOT NULL, " +
        "$FIELD_HISTORY_DATA TEXT NOT NULL)"
  }

  override fun onCreate(db: SQLiteDatabase?) {
    db?.run {
      execSQL(CREATE_GROUP_TABLE)
      execSQL(CREATE_DEVICE_TABLE)
      execSQL(CREATE_HISTORY_TABLE)
    }
  }

  override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

  }
}