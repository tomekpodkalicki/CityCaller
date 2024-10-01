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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
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

    private val LOC_SERVICE_ID  = 1
    private val LOC_SERVICE_DANGER_ID  = 2
    private val LOC_CHANNEL_ID = "LOC_CHANNEL_ID"
    private val UPDATE_INTERVAL_LOCATION: Long = 1_000 * 10
    private lateinit var locationRequest: LocationRequest
    private lateinit var susedLocation: FusedLocationProviderClient
    private val db = FirebaseFirestore.getInstance()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            val locationModel = LocationModel(
                locationResult.lastLocation?.latitude,
                locationResult.lastLocation?.longitude
            )
            CoroutineScope(Dispatchers.IO).launch {
                if(isNearIncident(locationModel)) createWarningNotification()

            }
        }
    }

    private fun createWarningNotification() {

        val builder = NotificationCompat.Builder(this, LOC_CHANNEL_ID)
            .setContentTitle("Uwaga, jestes w poblizu wydarzenia!")
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
        db.collection("incidents")
            .get()
            .await()
            .toObjects(IncidentModel::class.java)
            .forEach { inc ->
                if(calculateDistance(locationModel, inc.location) <= 1 ) return true
            }
        return false

    }

    private fun calculateDistance(userLocation: LocationModel, location: LocationModel?): Double {

        userLocation.lat?: return -1.0
        userLocation.long?: return -1.0
        val userLat = userLocation.lat
        val userLng = userLocation.long

        location?.lat?: return -1.0
        location?.long?: return -1.0

        val venueLat = location.lat
        val venueLng = location.long

        val latDistance = Math.toRadians(userLat - venueLat)
        val longDistance= Math.toRadians(userLng - venueLng)

        val a = (sin(latDistance/ 2)
                * sin(latDistance/ 2)
                + (cos(Math.toRadians(userLat))
                * cos(Math.toRadians(venueLat))
                * sin(longDistance/ 2)
                * sin(longDistance / 2)))

        val c = 2 * Math.atan2(sqrt(a), sqrt(1 - a))

        val result = 6371 * c
        Log.d("LOC_D", "distance: $result")
        return result


    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        initData()
    }

    private fun initData() {
        val locationRequest = LocationRequest.Builder(
            UPDATE_INTERVAL_LOCATION
        )
            .build()

        susedLocation = LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    override fun stopService(name: Intent?): Boolean {
        susedLocation.removeLocationUpdates(locationCallback)
        return super.stopService(name)
    }

    override fun onDestroy() {
        super.onDestroy()
        susedLocation.removeLocationUpdates(locationCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
       intent?.action?.let {
           if(it=="STOP_SERVICE") stopSelf()
       }

        startLocationUpdate()
        prepareForegorundNotification()
        return START_NOT_STICKY
    }

    private fun startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        susedLocation.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    @SuppressLint("ForegroundServiceType")
    private fun prepareForegorundNotification() {
        val serviceChannel = NotificationChannel(LOC_CHANNEL_ID,
            "Location Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)

        val stopSelfIntent = Intent(applicationContext, LocalizationBackgroundService
        ::class.java).apply { action = "STOP_SERVICE"}

        val pendingStopIntent = PendingIntent.getService(
            applicationContext,
            0,
            stopSelfIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notifcation = NotificationCompat.Builder(this, LOC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Serwis lokalizujacy")
            .addAction(R.drawable.ic_launcher_foreground, "Zastopuj", pendingStopIntent)
            .setOngoing(true)
            .build()

        startForeground(LOC_SERVICE_ID, notifcation)
    }
    }

