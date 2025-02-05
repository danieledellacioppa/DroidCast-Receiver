package com.forteur.droidcast_receiver
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.InputStream
import java.net.ServerSocket

class ScreenReceiverService : Service() {
    private val TAG = "ScreenReceiverService"
    private val TIMEOUT: Long = 60000 // 1 minute
    private var timeoutHandler: Handler? = null
    private var timeoutRunnable: Runnable? = null

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
        resetTimeout()
        try {
            val dataInputStream = DataInputStream(inputStream)
            // Legge il flag dell'orientamento inviato dal projector
            val orientationFlag = dataInputStream.readInt()
            val size = dataInputStream.readInt()
            Log.d(TAG, "Expected image data size: $size")

            val imageData = ByteArray(size)
            var totalBytesRead = 0
            var bytesRead: Int

            while (totalBytesRead < size) {
                bytesRead = dataInputStream.read(imageData, totalBytesRead, size - totalBytesRead)
                if (bytesRead == -1) break
                totalBytesRead += bytesRead
                Log.d(TAG, "Read $bytesRead bytes, total read: $totalBytesRead")
            }

            if (totalBytesRead == size) {
                val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                if (bitmap != null) {
                    MainActivity.bitmapState.value = bitmap
                    MainActivity.orientationState.value = orientationFlag
                    Log.d(TAG, "Bitmap received and updated, orientation: $orientationFlag")
                    resetTimeout()
                } else {
                    Log.e(TAG, "Failed to decode bitmap")
                }
            } else {
                Log.e(TAG, "Incomplete image data received. Expected: $size, Read: $totalBytesRead")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing input stream", e)
        }
    }

    private fun resetTimeout() {
        timeoutRunnable?.let { timeoutHandler?.removeCallbacks(it) }
        timeoutHandler = Handler(Looper.getMainLooper())
        timeoutRunnable = Runnable {
            MainActivity.bitmapState.value = null
        }
        timeoutHandler?.postDelayed(timeoutRunnable!!, TIMEOUT)
    }

    companion object {
        const val PORT = 12345
        const val BUFFER_SIZE = 1024 * 1024
    }
}

