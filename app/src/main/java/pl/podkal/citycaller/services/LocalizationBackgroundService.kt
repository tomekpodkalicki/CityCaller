package pl.podkal.citycaller.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pl.podkal.citycaller.R
import pl.podkal.citycaller.data.models.IncidentModel
import pl.podkal.citycaller.data.models.LocationModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LocalizationBackgroundService : Service() {
    private val LOC_SERVICE_ID = 1
    private val LOC_SERVICE_DANGER_ID = 2
    private val LOC_CHANNEL_ID = "LOC_CHANNEL_ID"
    private val UPDATE_INTERVAL_LOCATION: Long = 10_000
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocation: FusedLocationProviderClient
    private val db = FirebaseFirestore.getInstance()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            val locationModel = LocationModel(
                locationResult.lastLocation?.latitude,
                locationResult.lastLocation?.longitude
            )

            CoroutineScope(Dispatchers.IO).launch {
                if (isNearIncident(locationModel)) createWarningNotification()
            }
        }
    }

    private fun createWarningNotification() {
        val builder = NotificationCompat.Builder(this, LOC_CHANNEL_ID)
            .setContentTitle("Warning! Near an incident!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        val channel = NotificationChannel(
            LOC_CHANNEL_ID,
            "LOC SERVICE",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        manager.notify(LOC_SERVICE_DANGER_ID, builder)
    }

    private suspend fun isNearIncident(locationModel: LocationModel): Boolean {
        val incidents = db.collection("incidents")
            .get()
            .await()
            .toObjects(IncidentModel::class.java)

        for (incident in incidents) {
            if (calculateDistance(locationModel, incident.location) <= 1) {
                return true
            }
        }
        return false
    }

    private fun calculateDistance(userLocation: LocationModel, location: LocationModel?): Double {
        userLocation.lat ?: return -1.0
        userLocation.lng ?: return -1.0
        val venueLat = location?.lat ?: return -1.0
        val venueLng = location.lng ?: return -1.0
        val dLat = Math.toRadians(userLocation.lat!! - venueLat)
        val dLng = Math.toRadians(userLocation.lng!! - venueLng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(venueLat)) * cos(Math.toRadians(userLocation.lat!!)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * Math.atan2(sqrt(a), sqrt(1 - a))
        val distance = 6371 * c // Earth radius in kilometers
        return distance
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            if (it == "STOP_SERVICE") {
                stopSelf() // Stop the service if the "STOP_SERVICE" action is received
            }
        }

        startLocationUpdate()
        prepareForegroundNotification()  // Initialize the foreground service
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_LOCATION
        ).build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocation.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun prepareForegroundNotification() {
        val stopIntent = Intent(this, LocalizationBackgroundService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, LOC_CHANNEL_ID)
            .setContentTitle("City Caller Localization")
            .setContentText("Localization is running")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .setSmallIcon(R.drawable.ic_launcher_foreground)

        val channel = NotificationChannel(
            LOC_CHANNEL_ID,
            "LOC SERVICE",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        startForeground(LOC_SERVICE_ID, builder.build())
    }

    override fun stopService(name: Intent?): Boolean {
        fusedLocation.removeLocationUpdates(locationCallback)
        val stopIntent = Intent("pl.podkal.citycaller.LOCALIZATION_STOPPED")
        sendBroadcast(stopIntent)

        // Remove the ongoing notification
        stopForeground(true)  // Removes the foreground notification

        return super.stopService(name)
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocation.removeLocationUpdates(locationCallback)
        stopForeground(true) // Remove notification
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
