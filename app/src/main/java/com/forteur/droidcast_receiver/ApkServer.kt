package com.forteur.droidcast_receiver

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import java.io.File

class ApkServer(private val context: Context, port: Int) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
        val file = File(context.getExternalFilesDir(null), "raw/droidcast_projector.apk")
        return if (file.exists()) {
            newFixedLengthResponse(
                Response.Status.OK,
                "application/vnd.android.package-archive",
                file.inputStream(),
                file.length()
            )
        } else {
            newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "text/plain",
                "File not found"
            )
        }
    }
}

