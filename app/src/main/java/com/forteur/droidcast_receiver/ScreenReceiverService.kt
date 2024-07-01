package com.forteur.droidcast_receiver
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import java.io.InputStream
import java.net.ServerSocket

class ScreenReceiverService : Service() {
    private val TAG = "ScreenReceiverService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            try {
                val serverSocket = ServerSocket(PORT)
                Log.d(TAG, "Server socket started on port $PORT")
                while (true) {
                    val clientSocket = serverSocket.accept()
                    Log.d(TAG, "Client connected: ${clientSocket.inetAddress}")
                    val inputStream = clientSocket.getInputStream()
                    displayStream(inputStream)
                    clientSocket.close()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting server socket", e)
            }
        }.start()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun displayStream(inputStream: InputStream) {
        val bytes = ByteArray(BUFFER_SIZE)
        var bytesRead: Int
        while (inputStream.read(bytes).also { bytesRead = it } != -1) {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytesRead)
            MainActivity.bitmapState.value = bitmap
            Log.d(TAG, "Bitmap received and updated")
        }
    }

    companion object {
        const val PORT = 12345
        const val BUFFER_SIZE = 1024 * 1024
    }
}

