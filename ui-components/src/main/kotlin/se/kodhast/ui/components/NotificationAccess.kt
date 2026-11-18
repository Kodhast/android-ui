package se.kodhast.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * Om appen får visa notiser ([allowed]), kontrollerat varje gång skärmen visas, och hur man ber om det:
 * [ask] frågar (Android 13+) om det inte redan är tillåtet, [openSettings] öppnar appens notisinställningar.
 * Från [rememberNotificationAccess].
 */
@Stable
class NotificationAccess(val allowed: Boolean, val ask: () -> Unit, val openSettings: () -> Unit)

/**
 * [NotificationAccess] för skärmen: läses om när skärmen visas igen (efter systemets inställningar)
 * och när svaret på frågan kommer. Appens exakta larm och liknande bygger på samma mönster i appen.
 */
@Composable
fun rememberNotificationAccess(): NotificationAccess {
    val context = LocalContext.current
    var allowed by remember { mutableStateOf(context.canPostNotifications()) }
    LifecycleResumeEffect(context) {
        allowed = context.canPostNotifications()
        onPauseOrDispose {}
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed = context.canPostNotifications() }
    return NotificationAccess(
        allowed = allowed,
        ask = { if (!allowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
        openSettings = { context.openNotificationSettings() }
    )
}

/** Notiserna är påslagna för appen och, från Android 13, behörigheten given. */
fun Context.canPostNotifications(): Boolean = NotificationManagerCompat.from(this).areNotificationsEnabled() &&
    (
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        )

private fun Context.openNotificationSettings() {
    startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
