package com.hmproductions.facewatch.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.google.gson.Gson
import com.hmproductions.facewatch.data.GenericResponse
import java.text.SimpleDateFormat
import java.util.*

fun Int.isSuccessful(): Boolean {
    return this in 200..299
}

fun getActualPath(context: Context, uri: Uri): String? {
    var result: String? = null
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            val columnIndex: Int = cursor.getColumnIndexOrThrow(projection[0])
            result = cursor.getString(columnIndex)
        }
        cursor.close()
    }
    if (result == null) {
        result = "Not found"
    }
    return result
}

fun extractErrorMessage(jsonString: String?): String {
    if (jsonString == null || jsonString.isBlank() || jsonString.isEmpty()) return "Internal server error"
    return Gson().fromJson(jsonString, GenericResponse::class.java).statusMessage
}

fun getDateInISOFormat(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.UK)
    return sdf.format(Date(System.currentTimeMillis()))
}

fun removeLeadingZero(s: String): String {
    return if (s[0] == '0') s.substring(1) else return s
}

fun customFormatDate(s: String): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'", Locale.US)
    val date = formatter.parse(s)
    return SimpleDateFormat("dd/MM/yy", Locale.US).format(date)
}