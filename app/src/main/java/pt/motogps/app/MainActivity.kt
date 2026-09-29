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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
    private var onUiStatusChange: ((String) -> Unit)? = null
    private var onRouteReady: (() -> Unit)? = null
    private var onNavigationChange: ((Boolean) -> Unit)? = null

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            requestInitialLocation()
            onUiStatusChange?.invoke("Permissão concedida. A obter localização GPS…")
            if (pendingStartNavigation) requestNotificationAndStart()
        } else {
            pendingStartNavigation = false
            onUiStatusChange?.invoke("A localização é necessária para calcular uma rota.")
            web?.evaluateJavascript("showLocationStatus('permissao_negada');", null)
        }
    }

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (pendingStartNavigation) {
            pendingStartNavigation = false
            startNavigationService()
            if (!granted) {
                onUiStatusChange?.invoke(
                    "Navegação ativa. Sem notificações na gaveta; o serviço continua visível no Gestor de tarefas do Android."
                )
            } else {
                onUiStatusChange?.invoke("Navegação GPS ativa.")
            }
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

    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            this,
            locationReceiver,
            IntentFilter(ACTION_LOCATION_UPDATED),
            ContextCompat.RECEIVER_NOT_EXPORTED
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
        var status by remember { mutableStateOf("A obter localização…") }
        var showLocationDisclosure by remember { mutableStateOf(!hasLocationPermission()) }
        var showNavigationDisclosure by remember { mutableStateOf(false) }
        var navigationActive by remember { mutableStateOf(false) }

        voiceEnabled = voice
        onUiStatusChange = { status = it }
        onRouteReady = { showNavigationDisclosure = true }
        onNavigationChange = { navigationActive = it }

        if (showLocationDisclosure) {
            AlertDialog(
                onDismissRequest = { showLocationDisclosure = false },
                title = { Text("Permitir localização") },
                text = {
                    Text(
                        "O MotoGPS usa a sua localização precisa ou aproximada para mostrar a posição e calcular rotas. " +
                            "Ao calcular uma rota, o destino e os pontos de origem e destino são enviados aos serviços de mapas Nominatim e OSRM. " +
                            "A localização contínua só começa quando iniciar a navegação."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showLocationDisclosure = false
                        locationPermission.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }) { Text("Continuar") }
                },
                dismissButton = {
                    TextButton(onClick = { showLocationDisclosure = false }) {
                        Text("Agora não")
                    }
                }
            )
        }

        if (showNavigationDisclosure) {
            AlertDialog(
                onDismissRequest = { showNavigationDisclosure = false },
                title = { Text("Iniciar navegação") },
                text = {
                    Text(
                        "A navegação usa a localização em segundo plano enquanto conduz. " +
                            "A notificação persistente permite regressar à app e parar o serviço. " +
                            "Se não autorizar notificações, o Android mostra o serviço no Gestor de tarefas. " +
                            "A app não guarda o histórico da sua localização."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showNavigationDisclosure = false
                        requestNotificationAndStart()
                    }) { Text("Pedir autorização") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showNavigationDisclosure = false
                        requestNotificationAndStart(requestNotifications = false)
                    }) { Text("Sem notificações") }
                }
            )
        }

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
                        if (dest.isBlank()) {
                            status = "Escreva um destino para calcular a rota."
                            return@Button
                        }
                        if (!hasLocationPermission()) {
                            showLocationDisclosure = true
                            status = "Permita a localização para calcular uma rota."
                            return@Button
                        }
                        status = "A calcular a rota…"
                        web?.evaluateJavascript(
                            "searchDestination(" + org.json.JSONObject.quote(dest.trim()) + ");",
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
                    .padding(horizontal = 8.dp)
                    .horizontalScroll(rememberScrollState()),
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

            Text(
                text = status,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall
            )
            if (navigationActive) {
                TextButton(
                    onClick = {
                        stopNavigationService()
                        status = "Navegação GPS parada."
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) { Text("Parar navegação") }
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
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val uri = request?.url ?: return true
                                // Keep the JavaScript bridge restricted to the bundled app document.
                                return uri.scheme != "https" ||
                                    uri.host != "appassets.androidplatform.net"
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                if (url == "https://appassets.androidplatform.net/assets/map.html") {
                                    requestInitialLocation()
                                }
                            }

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

                        addJavascriptInterface(
                            object {
                                @JavascriptInterface
                                fun getApiBaseUrl(): String = BuildConfig.MOTOGPS_API_BASE_URL

                                @JavascriptInterface
                                fun updateStatus(message: String) {
                                    runOnUiThread { onUiStatusChange?.invoke(message) }
                                }

                                @JavascriptInterface
                                fun routeReady() {
                                    runOnUiThread {
                                        onUiStatusChange?.invoke("Rota calculada. Reveja as instruções antes de iniciar.")
                                        onRouteReady?.invoke()
                                    }
                                }
                            },
                            "AndroidNavigation"
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

    private fun requestNotificationAndStart(requestNotifications: Boolean = true) {
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
            onUiStatusChange?.invoke("Ative os serviços de localização para iniciar a navegação.")
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

        if (requestNotifications && Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingStartNavigation = true
            onUiStatusChange?.invoke("A aguardar autorização de notificações…")
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        pendingStartNavigation = false
        startNavigationService()
        onUiStatusChange?.invoke("Navegação GPS ativa.")
    }

    private fun startNavigationService() {
        val intent = Intent(this, NavigationForegroundService::class.java)
        ContextCompat.startForegroundService(this, intent)
        onNavigationChange?.invoke(true)
    }

    private fun stopNavigationService() {
        startService(
            Intent(this, NavigationForegroundService::class.java)
                .setAction(NavigationForegroundService.ACTION_STOP)
        )
        onNavigationChange?.invoke(false)
    }

    @SuppressLint("MissingPermission")
    private fun requestInitialLocation() {
        if (!hasLocationPermission()) return
        if (!locationServicesEnabled()) {
            onUiStatusChange?.invoke("Ative os serviços de localização para mostrar a sua posição.")
            return
        }

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
                        } else {
                            onUiStatusChange?.invoke("À espera de uma posição GPS. Saia para uma zona com céu aberto e tente novamente.")
                        }
                    }
                }
            }
            .addOnFailureListener {
                onUiStatusChange?.invoke("Não foi possível obter a localização. Verifique as permissões e tente novamente.")
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
        onUiStatusChange = null
        onRouteReady = null
        onNavigationChange = null
        super.onDestroy()
    }
}
