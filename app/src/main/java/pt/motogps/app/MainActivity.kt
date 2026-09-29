package pt.motogps.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
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
import androidx.core.content.ContextCompat
import androidx.webkit.WebViewAssetLoader
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class MainActivity : ComponentActivity() {
    companion object {
        const val ACTION_LOCATION_UPDATED = "pt.motogps.app.LOCATION_UPDATED"
        const val EXTRA_LAT = "lat"
        const val EXTRA_LON = "lon"
        const val EXTRA_SPEED_KMH = "speed_kmh"
        const val EXTRA_ACCURACY = "accuracy"
        const val EXTRA_BEARING = "bearing"
        const val EXTRA_STATUS = "status"
    }

    private val locationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var web: WebView? = null
    private lateinit var tts: TextToSpeech
    private var voiceEnabled = true
    private var pendingStartNavigation = false

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            requestInitialLocation()
            if (pendingStartNavigation) requestNotificationAndStart()
        } else {
            pendingStartNavigation = false
            web?.evaluateJavascript("showLocationStatus('permissao_negada');", null)
        }
    }

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (pendingStartNavigation) {
            pendingStartNavigation = false
            startNavigationService()
        }
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_LOCATION_UPDATED) return

            val status = intent.getStringExtra(EXTRA_STATUS)
            if (status != null) {
                web?.evaluateJavascript(
                    "showLocationStatus(" + org.json.JSONObject.quote(status) + ");",
                    null
                )
            }

            if (!intent.hasExtra(EXTRA_LAT) || !intent.hasExtra(EXTRA_LON)) return
            val lat = intent.getDoubleExtra(EXTRA_LAT, Double.NaN)
            val lon = intent.getDoubleExtra(EXTRA_LON, Double.NaN)
            if (!lat.isFinite() || !lon.isFinite()) return

            val speedKmh = intent.getFloatExtra(EXTRA_SPEED_KMH, 0f)
            web?.evaluateJavascript(
                "setUser($lat,$lon,$speedKmh);",
                null
            )
        }
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale("pt", "PT")
            }
        }

        setContent { App() }

        if (!hasLocationPermission()) {
            locationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            requestInitialLocation()
        }
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            this,
            locationReceiver,
            IntentFilter(ACTION_LOCATION_UPDATED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        sendBroadcast(
            Intent(NavigationForegroundService.ACTION_REQUEST_LAST)
                .setPackage(packageName)
        )
    }

    override fun onStop() {
        unregisterReceiver(locationReceiver)
        super.onStop()
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

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = dest,
                    onValueChange = { dest = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Destino") }
                )

                Button(
                    onClick = {
                        pendingStartNavigation = true
                        requestNotificationAndStart()

                        val safe = dest
                            .replace("\\", "\\\\")
                            .replace("'", "\\'")
                        web?.evaluateJavascript(
                            "searchDestination('" + safe + "');",
                            null
                        )
                    }
                ) {
                    Text("Ir")
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = mode == "Moto",
                    onClick = {
                        mode = "Moto"
                        web?.evaluateJavascript("setMode('moto');", null)
                    },
                    label = { Text("Moto") }
                )
                FilterChip(
                    selected = mode == "Curvas",
                    onClick = {
                        mode = "Curvas"
                        web?.evaluateJavascript("setMode('curves');", null)
                    },
                    label = { Text("Curvas") }
                )
                FilterChip(
                    selected = mode == "Sem portagens",
                    onClick = {
                        mode = "Sem portagens"
                        web?.evaluateJavascript("setMode('no_tolls');", null)
                    },
                    label = { Text("Sem portagens") }
                )
                Switch(
                    checked = voice,
                    onCheckedChange = { voice = it }
                )
            }

            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        web = this
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        settings.javaScriptCanOpenWindowsAutomatically = false
                        settings.setSupportMultipleWindows(false)

                        val assetLoader = WebViewAssetLoader.Builder()
                            .addPathHandler(
                                "/assets/",
                                WebViewAssetLoader.AssetsPathHandler(context)
                            )
                            .addPathHandler(
                                "/res/",
                                WebViewAssetLoader.ResourcesPathHandler(context)
                            )
                            .build()

                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                val url = request?.url ?: return null
                                return assetLoader.shouldInterceptRequest(url)
                            }

                            override fun onRenderProcessGone(
                                view: WebView?,
                                detail: android.webkit.RenderProcessGoneDetail?
                            ): Boolean {
                                return true
                            }
                        }

                        addJavascriptInterface(
                            object {
                                @JavascriptInterface
                                fun speak(text: String) {
                                    if (voiceEnabled) {
                                        tts.speak(
                                            text,
                                            TextToSpeech.QUEUE_FLUSH,
                                            null,
                                            "motogps"
                                        )
                                    }
                                }
                            },
                            "AndroidVoice"
                        )

                        loadUrl("https://appassets.androidplatform.net/assets/map.html")
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun locationServicesEnabled(): Boolean {
        val manager = getSystemService(LocationManager::class.java)
        return Build.VERSION.SDK_INT < 28 || manager.isLocationEnabled
    }

    private fun requestNotificationAndStart() {
        if (!hasLocationPermission()) {
            pendingStartNavigation = true
            locationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        if (!locationServicesEnabled()) {
            pendingStartNavigation = false
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingStartNavigation = true
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        pendingStartNavigation = false
        startNavigationService()
    }

    private fun startNavigationService() {
        val intent = Intent(this, NavigationForegroundService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    @SuppressLint("MissingPermission")
    private fun requestInitialLocation() {
        if (!hasLocationPermission() || !locationServicesEnabled()) return

        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setMaxUpdateAgeMillis(3_000L)
            .setDurationMillis(5_000L)
            .build()

        locationClient.getCurrentLocation(request, null)
            .addOnSuccessListener { current ->
                if (current != null) {
                    deliverLocation(current.latitude, current.longitude, current.speed * 3.6f)
                } else {
                    locationClient.lastLocation.addOnSuccessListener { last ->
                        if (last != null) {
                            deliverLocation(
                                last.latitude,
                                last.longitude,
                                last.speed * 3.6f
                            )
                        }
                    }
                }
            }
    }

    private fun deliverLocation(lat: Double, lon: Double, speedKmh: Float) {
        if (!lat.isFinite() || !lon.isFinite()) return
        web?.evaluateJavascript(
            "setUser($lat,$lon,$speedKmh);",
            null
        )
    }

    override fun onDestroy() {
        if (::tts.isInitialized) tts.shutdown()
        web?.destroy()
        super.onDestroy()
    }
}
