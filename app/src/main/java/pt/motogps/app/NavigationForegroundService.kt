package pt.motogps.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class NavigationForegroundService : Service() {
    companion object {
        const val ACTION_STOP = "pt.motogps.app.STOP_NAVIGATION"
        private const val CHANNEL_ID = "motogps_navigation"
        private const val NOTIFICATION_ID = 1001
        private const val MAX_ACCEPTABLE_ACCURACY_M = 100f
    }

    private lateinit var locationClient: FusedLocationProviderClient
    private var lastAcceptedElapsedNanos = 0L
    private var lastLocation: Location? = null
    private var smoothedSpeedKmh = 0f

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            handleLocation(location)
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopLocationUpdates()
                ServiceCompat.stopForeground(
                    this,
                    ServiceCompat.STOP_FOREGROUND_REMOVE
                )
                stopSelf()
                return START_NOT_STICKY
            }

        }

        startAsForegroundService()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startAsForegroundService() {
        val notification = buildNotification()

        if (Build.VERSION.SDK_INT >= 29) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, NavigationForegroundService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("MotoGPS")
            .setContentText("Navegação GPS ativa")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setContentIntent(openIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Parar",
                stopIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Navegação MotoGPS",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Localização contínua durante a navegação."
            setShowBadge(false)
        }

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1_000L
        )
            .setMinUpdateIntervalMillis(500L)
            .setMinUpdateDistanceMeters(2f)
            .setWaitForAccurateLocation(true)
            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            .setMaxUpdateAgeMillis(3_000L)
            .build()

        locationClient.removeLocationUpdates(callback)
        locationClient.requestLocationUpdates(
            request,
            callback,
            mainLooper
        )
    }

    private fun handleLocation(location: Location) {
        if (!location.latitude.isFinite() || !location.longitude.isFinite()) return

        if (Build.VERSION.SDK_INT >= 31 && location.isMock) {
            broadcastStatus("mock_location")
            return
        }

        val elapsed = location.elapsedRealtimeNanos
        if (elapsed > 0L && elapsed <= lastAcceptedElapsedNanos) return

        val accuracy = location.accuracy
        if (accuracy > 0f && accuracy > MAX_ACCEPTABLE_ACCURACY_M) {
            broadcastStatus("low_accuracy")
            return
        }

        if (elapsed > 0L) lastAcceptedElapsedNanos = elapsed

        val rawSpeedKmh = if (location.hasSpeed()) {
            (location.speed * 3.6f).coerceAtLeast(0f)
        } else {
            0f
        }

        smoothedSpeedKmh = if (lastLocation == null) {
            rawSpeedKmh
        } else {
            (smoothedSpeedKmh * 0.65f) + (rawSpeedKmh * 0.35f)
        }

        lastLocation = Location(location)
        broadcastLocation(lastLocation)
    }

    private fun stopLocationUpdates() {
        locationClient.removeLocationUpdates(callback)
    }

    private fun broadcastStatus(status: String) {
        sendBroadcast(
            Intent(MainActivity.ACTION_LOCATION_UPDATED)
                .setPackage(packageName)
                .putExtra(MainActivity.EXTRA_STATUS, status)
        )
    }

    private fun broadcastLocation(location: Location?) {
        if (location == null) return

        sendBroadcast(
            Intent(MainActivity.ACTION_LOCATION_UPDATED)
                .setPackage(packageName)
                .putExtra(MainActivity.EXTRA_LAT, location.latitude)
                .putExtra(MainActivity.EXTRA_LON, location.longitude)
                .putExtra(MainActivity.EXTRA_SPEED_KMH, smoothedSpeedKmh)
                .putExtra(MainActivity.EXTRA_ACCURACY, location.accuracy)
                .putExtra(
                    MainActivity.EXTRA_BEARING,
                    if (location.hasBearing()) location.bearing else Float.NaN
                )
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
    }
}
