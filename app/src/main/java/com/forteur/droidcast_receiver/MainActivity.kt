package com.forteur.droidcast_receiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.forteur.droidcast_receiver.ui.theme.DroidCastReceiverTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    companion object {
        val orientationState: MutableState<Int> = mutableIntStateOf(0)
        val bitmapState = mutableStateOf<Bitmap?>(null)
    }

    private var apkServer: ApkServer? = null
    private val serverPort = 8080

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the receiver service
        val intent = Intent(this, ScreenReceiverService::class.java)
        startService(intent)

        val ipAddress = getIPAddress()

        // Copy the APK from /res/raw to the cache directory
        val apkFile = File(cacheDir, "droidcast_projector2.apk")
        if (!apkFile.exists()) {
            Log.d("MainActivity", "APK file does not exist")
            Log.d("MainActivity", "Copying APK file to cache directory")
            resources.openRawResource(R.raw.droidcast_projector2).use { input ->
                FileOutputStream(apkFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        Log.d("MainActivity", "APK file path: ${apkFile.absolutePath}")

//        resources.openRawResource(R.raw.droidcast_projector).use { input ->
//            FileOutputStream(apkFile).use { output ->
//                input.copyTo(output)
//            }
//        }

        // Start the HTTP server
        apkServer = ApkServer(this, serverPort, apkFile)
        apkServer?.start()

        val downloadLink = "http://$ipAddress:$serverPort"


        setContent {
            DroidCastReceiverTheme {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF3A3A3A), Color(0xFF1A1A1A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (bitmapState.value != null) {
                            val aspectRatio = bitmapState.value!!.width.toFloat() / bitmapState.value!!.height

                            when (orientationState.value) {
                                0 -> {
                                    Image(
                                        bitmap = bitmapState.value!!.asImageBitmap(),
                                        contentDescription = "Screen Cast",
                                        modifier = Modifier
//                                    .fillMaxWidth()
                                            .aspectRatio(aspectRatio),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                1 -> {
                                    Image(
                                        bitmap = bitmapState.value!!.asImageBitmap(),
                                        contentDescription = "Screen Cast",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(aspectRatio),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }


                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.screencast_idea),
                                contentDescription = "Screencast Idea",
                                modifier = Modifier.size(128.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Receiver IP: $ipAddress",
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    color = Color.Yellow,
                                    fontSize = 12.sp,
                                    fontFamily = MinecraftFontFamily,
                                    shadow = Shadow(
                                        color = Color.Black,
                                        offset = Offset(4f, 4f),
                                        blurRadius = 8f
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "...Waiting for image data...",
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    color = Color.Yellow,
                                    fontSize = 12.sp,
                                    fontFamily = MinecraftFontFamily,
                                    shadow = Shadow(
                                        color = Color.Black,
                                        offset = Offset(4f, 4f),
                                        blurRadius = 8f
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "Ensure that you have DroidCast-Projector installed on the casting device!",
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily.Default,
                                    shadow = Shadow(
                                        color = Color.Black,
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                ),
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            ApkQrCode(downloadLink)
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "Scan to download DroidCast-Projector",
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    shadow = Shadow(
                                        color = Color.Black,
                                        offset = Offset(2f, 2f),
                                        blurRadius = 4f
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getIPAddress(): String {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        return String.format(
            "%d.%d.%d.%d",
            (ipAddress and 0xff),
            (ipAddress shr 8 and 0xff),
            (ipAddress shr 16 and 0xff),
            (ipAddress shr 24 and 0xff)
        )
    }

    private fun copyApkToPublicDir() {
        val rawApk = resources.openRawResource(R.raw.droidcast_projector2)
        val outputFile = File(getRawFilePath(), "droidcast_projector2.apk")
        rawApk.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
    }


    private fun getApkUri(): Uri {
        val file = File(getRawFilePath(), "droidcast_projector2.apk")
        return FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            file
        )
    }

    private fun getRawFilePath(): File {
        return File(applicationContext.getExternalFilesDir(null), "raw").apply {
            if (!exists()) mkdirs()
        }
    }
}

@Composable
fun ApkQrCode(downloadLink: String) {
    val qrBitmap = generateQrCodeBitmap(downloadLink)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        qrBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.size(200.dp)
            )
        }
        Text(
            text = "Scan to Download DroidCast-Projector",
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

private fun generateQrCodeBitmap(text: String): Bitmap? {
    return try {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.Black.toArgb() else Color.White.toArgb())
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DroidCastReceiverTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF3A3A3A), Color(0xFF1A1A1A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.screencast_idea),
                    contentDescription = "Screencast Idea",
                    modifier = Modifier.size(128.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Receiver IP: 0.0.0.0",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = Color.Yellow,
                        fontSize = 12.sp,
                        fontFamily = MinecraftFontFamily,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(4f, 4f),
                            blurRadius = 8f
                        )
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Waiting for image data...",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = Color.Yellow,
                        fontSize = 12.sp,
                        fontFamily = MinecraftFontFamily,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(4f, 4f),
                            blurRadius = 8f
                        )
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Ensure that you have DroidCast-Projector installed on the casting device.",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Default,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    ),
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}

val MinecraftFontFamily = FontFamily(
    Font(R.font.minecraft)
)


