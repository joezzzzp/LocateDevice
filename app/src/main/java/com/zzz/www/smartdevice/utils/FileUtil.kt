package com.zzz.www.smartdevice.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.DocumentsContract
import android.content.ContentUris
import android.os.Environment.getExternalStorageDirectory
import android.os.Build
import android.content.ContentResolver
import android.os.Environment


/**
 * @author zzz
 * @date create at 2018/7/23.
 */
class FileUtil {

  companion object {

    fun getFilePathByUri(context: Context, uri: Uri?): String? {
      if (uri == null) {
        return null
      }
      var path: String? = null
      // 以 file:// 开头的
      if (ContentResolver.SCHEME_FILE == uri.scheme) {
        path = uri.path
        return path
      }
      // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
      if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
        val cursor = context.contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA),
          null, null, null)
        if (cursor != null) {
          if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (columnIndex > -1) {
              path = cursor.getString(columnIndex)
            }
          }
          cursor.close()
        }
        return path
      }
      // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
      if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
          if (isExternalStorageDocument(uri)) {
            // ExternalStorageProvider
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
              path = Environment.getExternalStorageDirectory().path + "/" + split[1]
              return path
            }
          } else if (isDownloadsDocument(uri)) {
            // DownloadsProvider
            val id = DocumentsContract.getDocumentId(uri)
            val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
              java.lang.Long.valueOf(id))
            path = getDataColumn(context, contentUri, null, null)
            return path
          } else if (isMediaDocument(uri)) {
            // MediaProvider
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            when (type) {
              "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
              "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
              "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])
            path = getDataColumn(context, contentUri, selection, selectionArgs)
            return path
          }
        }
      }
      return null
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
      var cursor: Cursor? = null
      val column = "_data"
      val projection = arrayOf(column)
      try {
        cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
          val columnIndex = cursor.getColumnIndexOrThrow(column)
          return cursor.getString(columnIndex)
        }
      } finally {
        if (cursor != null)
          cursor.close()
      }
      return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean =
      "com.android.externalstorage.documents" == uri.authority

    private fun isDownloadsDocument(uri: Uri): Boolean =
      "com.android.providers.downloads.documents" == uri.authority

    private fun isMediaDocument(uri: Uri): Boolean =
      "com.android.providers.media.documents" == uri.authority

  }
}