package com.forteur.droidcast_receiver

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.File

class ApkServer(private val context: Context, port: Int,  private val apkFile: File) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
        return if (apkFile.exists()) {
            Log.d("ApkServer", "Serving APK file")
            val fis = apkFile.inputStream()
            val response = newChunkedResponse(
                Response.Status.OK,
                "application/vnd.android.package-archive",
                fis
            )
            response.addHeader("Content-Disposition", "attachment; filename=\"droidcast_projector3.apk\"")
            response
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

