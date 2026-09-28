package pt.motogps.app

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.*
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val location by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private var web: WebView? = null
    private lateinit var tts: TextToSpeech
    private var voiceEnabled = true
    private val permission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { startLocationUpdates() }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val current = result.lastLocation ?: return
            web?.evaluateJavascript("setUser(" + current.latitude + "," + current.longitude + "," + current.speed + ");", null)
        }
    }
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        tts = TextToSpeech(this) { status -> if (status == TextToSpeech.SUCCESS) tts.language = Locale("pt", "PT") }
        permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        setContent { App() }
    }
    @SuppressLint("SetJavaScriptEnabled")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun App() {
        var dest by remember { mutableStateOf("") }
        var mode by remember { mutableStateOf("Moto") }
        var voice by remember { mutableStateOf(true) }
        voiceEnabled = voice
        Column(Modifier.fillMaxSize()) {
            TopAppBar(title = { Text("MotoGPS") })
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = dest, onValueChange = { dest = it }, Modifier.weight(1f), singleLine = true, label = { Text("Destino") })
                Button(onClick = {
                    val safe = dest.replace("\\", "\\\\").replace("'", "\\'")
                    web?.evaluateJavascript("searchDestination('" + safe + "');", null)
                }) { Text("Ir") }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(mode == "Moto", { mode = "Moto"; web?.evaluateJavascript("setMode('moto');", null) }, label = { Text("Moto") })
                FilterChip(mode == "Curvas", { mode = "Curvas"; web?.evaluateJavascript("setMode('curves');", null) }, label = { Text("Curvas") })
                FilterChip(mode == "Sem portagens", { mode = "Sem portagens"; web?.evaluateJavascript("setMode('no_tolls');", null) }, label = { Text("Sem portagens") })
                Switch(checked = voice, onCheckedChange = { voice = it })
            }
            AndroidView(factory = { context ->
                WebView(context).apply {
                    web = this
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    addJavascriptInterface(object {
                        @JavascriptInterface fun speak(text: String) {
                            if (voiceEnabled) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "motogps")
                        }
                    }, "AndroidVoice")
                    loadUrl("file:///android_asset/map.html")
                }
            }, modifier = Modifier.weight(1f))
        }
    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1500L).setMinUpdateIntervalMillis(750L).setMinUpdateDistanceMeters(2f).build()
        location.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }
    override fun onDestroy() {
        location.removeLocationUpdates(locationCallback)
        if (::tts.isInitialized) tts.shutdown()
        web?.destroy()
        super.onDestroy()
    }
}