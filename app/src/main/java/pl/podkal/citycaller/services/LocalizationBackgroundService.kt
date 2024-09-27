package pl.podkal.citycaller.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.podkal.citycaller.data.models.LocationModel

class LocalizationBackgroundService : Service() {
    private val UPDATE_INTERVAL_LOCATION: Long = 1_000 * 60
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
                isNearIncident(locationModel)

            }
        }
    }

    private fun isNearIncident(locationModel: LocationModel) {
        //TODO("Not yet implemented")
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdate()
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
    }
}
