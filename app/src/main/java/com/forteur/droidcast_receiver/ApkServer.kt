package com.forteur.droidcast_receiver

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.File

class ApkServer(private val context: Context, port: Int,  private val apkFile: File) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
//        val file = File(context.getExternalFilesDir(null), "raw/droidcast_projector.apk")
        return if (apkFile.exists()) {
            Log.d("ApkServer", "Serving APK file")
            newFixedLengthResponse(
                Response.Status.OK,
                "application/vnd.android.package-archive",
                apkFile.inputStream(),
                apkFile.length()
            )
        } else {
            Log.e("ApkServer", "APK file not found")
            newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "text/plain",
                "File not found"
            )
        }
    }
}

