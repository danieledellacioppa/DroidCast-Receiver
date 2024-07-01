package com.forteur.droidcast_receiver
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
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
        try {
            val dataInputStream = DataInputStream(inputStream)
            val size = dataInputStream.readInt()
            Log.d(TAG, "Expected image data size: $size")

            val byteArrayOutputStream = ByteArrayOutputStream()
            val buffer = ByteArray(BUFFER_SIZE)
            var totalBytesRead = 0
            var bytesRead: Int

            while (totalBytesRead < size) {
                bytesRead = dataInputStream.read(buffer, 0, Math.min(buffer.size, size - totalBytesRead))
                totalBytesRead += bytesRead
                byteArrayOutputStream.write(buffer, 0, bytesRead)
                Log.d(TAG, "Read $bytesRead bytes, total read: $totalBytesRead")
            }

            val imageData = byteArrayOutputStream.toByteArray()
            Log.d(TAG, "Total image data read: ${imageData.size} bytes")

            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            if (bitmap != null) {
                MainActivity.bitmapState.value = bitmap
                Log.d(TAG, "Bitmap received and updated")
            } else {
                Log.e(TAG, "Failed to decode bitmap")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing input stream", e)
        }
    }

    companion object {
        const val PORT = 12345
        const val BUFFER_SIZE = 1024 * 1024
    }
}
