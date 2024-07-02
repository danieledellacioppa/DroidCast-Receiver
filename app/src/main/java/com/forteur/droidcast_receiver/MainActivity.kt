package com.forteur.droidcast_receiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.wifi.WifiManager
import android.os.Bundle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.forteur.droidcast_receiver.ui.theme.DroidCastReceiverTheme

class MainActivity : ComponentActivity() {
    companion object {
        val bitmapState = mutableStateOf<Bitmap?>(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start the receiver service
        val intent = Intent(this, ScreenReceiverService::class.java)
        startService(intent)

        val ipAddress = getIPAddress()

        setContent {
            DroidCastReceiverTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (bitmapState.value != null) {
                            Image(
                                bitmap = bitmapState.value!!.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = "Receiver IP: $ipAddress",
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    color = Color.Yellow,
                                    fontSize = 20.sp,
                                    fontFamily = MinecraftFontFamily,
                                    shadow = Shadow(
                                        color = Color.Black,
                                        offset = Offset(2f, 2f),
                                        blurRadius = 3f
                                    )
                                )
                            )
                        }
                        if (bitmapState.value != null) {
                            Image(
                                bitmap = bitmapState.value!!.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = "Waiting for image data...",
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    color = Color.Yellow,
                                    fontSize = 20.sp,
                                    fontFamily = MinecraftFontFamily,
                                    shadow = Shadow(
                                        color = Color.Black,
                                        offset = Offset(2f, 2f),
                                        blurRadius = 3f
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
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DroidCastReceiverTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Receiver IP: 0.0.0.0",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = Color.Yellow,
                        fontSize = 20.sp,
                        fontFamily = MinecraftFontFamily,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 3f
                        )
                    )
                )
                Text(
                    text = "Waiting for image data...",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = Color.Yellow,
                        fontSize = 20.sp,
                        fontFamily = MinecraftFontFamily,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 3f
                        )
                    )
                )
            }
        }
    }
}

val MinecraftFontFamily = FontFamily(
    Font(R.font.minecraft)
)
