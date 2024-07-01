package com.forteur.droidcast_receiver

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

        setContent {
            DroidCastReceiverTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (bitmapState.value != null) {
                        Image(
                            bitmap = bitmapState.value!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "Waiting for image data...",
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DroidCastReceiverTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Waiting for image data...",
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }
    }
}
