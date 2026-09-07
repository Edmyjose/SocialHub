/*******************************************************************************
 * Copyright (c) 2025. EJS Studios. Todos los derechos reservados.
 * Este código fuente es propiedad de EJS Studios y está protegido por las leyes de derechos de autor.
 * No se permite la copia, distribución, modificación o uso de este código fuente, total o parcialmente, sin
 * el consentimiento previo y por escrito de EJS Studios.
 *
 * Cualquier uso no autorizado de este código fuente será perseguido legalmente según las leyes aplicables.
 *
 * Para obtener una licencia de uso, contacta a: info@ejsstudios.com
 ******************************************************************************/

package com.ejsstudios.socialhub.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.ejsstudios.socialhub.R
import com.ejsstudios.socialhub.utils.Constants.PLAYSTOREAPPURL

/**
 * Proporciona una API unificada y robusta para la gestión de notificaciones en toda la aplicación.
 */
class NotificationHandler(private val context: Context) {

    companion object {
        // Vibration Patterns
        val PATTERN_HEARTBEAT = longArrayOf(0, 200, 100, 300, 400, 200, 100, 200, 100, 300, 400, 200, 100, 200, 100, 300, 400, 200)
        val PATTERN_URGENT_ALARM = longArrayOf(0, 500, 200, 500, 200, 500, 200, 500)
        val PATTERN_SOFT_REMINDER = longArrayOf(0, 300, 300, 300)
    }

    object Channels {
        const val GENERAL_CHANNEL_ID: String = "general_notifications"
        const val UPDATES_CHANNEL_ID: String = "updates_notifications"
        const val REMINDER_CHANNEL_ID: String = "reminder_notifications"
        const val AUTO_APPROVAL_CHANNEL_ID: String = "auto_approval_notifications"
        const val PUBLISH_SIMULATOR_CHANNEL_ID: String = "publish_simulator_notifications"
        const val DAILY_SUMMARY_CHANNEL_ID: String = "daily_summary_notifications"
        const val SYSTEM_ERRORS_CHANNEL_ID: String = "system_error_notifications"
    }

    object NotificationIds {
        const val GENERAL: Int = 0
        const val UPDATE: Int = 3
        const val AUTO_APPROVAL: Int = 100
        const val PUBLISH_SIMULATOR: Int = 200
        const val DAILY_SUMMARY: Int = 300
        const val SERVER_ERROR: Int = 400
        const val AI_ERROR: Int = 500
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                createChannel(Channels.GENERAL_CHANNEL_ID, "Notificaciones Generales", "Avisos generales de la aplicación."),
                createChannel(Channels.UPDATES_CHANNEL_ID, "Actualizaciones", "Mejoras y nuevas versiones.", vibrationPattern = PATTERN_URGENT_ALARM, lightColor = Color.YELLOW),
                createChannel(Channels.REMINDER_CHANNEL_ID, "Recordatorios", "Avisos de tareas pendientes.", vibrationPattern = PATTERN_HEARTBEAT, lightColor = Color.GREEN),
                createChannel(Channels.AUTO_APPROVAL_CHANNEL_ID, "Auto-Aprobación IA", "Estrategias aprobadas por el piloto automático.", vibrationPattern = PATTERN_SOFT_REMINDER, lightColor = Color.CYAN),
                createChannel(Channels.PUBLISH_SIMULATOR_CHANNEL_ID, "Simulador de Publicación", "Simulacros de envíos a redes sociales.", vibrationPattern = PATTERN_HEARTBEAT, lightColor = Color.MAGENTA),
                createChannel(Channels.DAILY_SUMMARY_CHANNEL_ID, "Resumen Diario", "Reporte matutino de actividades.", vibrationPattern = PATTERN_SOFT_REMINDER, lightColor = Color.BLUE),
                createChannel(Channels.SYSTEM_ERRORS_CHANNEL_ID, "Errores del Sistema", "Alertas de conexión y fallos técnicos.", vibrationPattern = PATTERN_URGENT_ALARM, lightColor = Color.RED)
            )

            notificationManager.createNotificationChannels(channels)
        }
    }

    fun showNotification(
        notificationId: Int = NotificationIds.GENERAL,
        channelId: String = Channels.GENERAL_CHANNEL_ID,
        icon: Int,
        title: String,
        message: String,
        largeIcon: Bitmap? = null,
        pendingIntent: PendingIntent? = null,
        color: Int = Color.BLUE,
        autoCancel: Boolean = true,
        onlyAlertOnce: Boolean = false,
        actions: List<NotificationCompat.Action> = emptyList()
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = notificationBuilder(channelId, title, message, icon, largeIcon, pendingIntent, color, autoCancel = autoCancel, onlyAlertOnce = onlyAlertOnce, actions = actions).build()
        notificationManager.notify(notificationId, notification)
    }

    fun showUpdateNotification(title: String, message: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, (PLAYSTOREAPPURL + context.packageName).toUri())
        val pendingIntent = PendingIntent.getActivity(context, 0, browserIntent, PendingIntent.FLAG_IMMUTABLE)
        showNotification(NotificationIds.UPDATE, Channels.UPDATES_CHANNEL_ID, R.drawable.ic_launcher_foreground, title, message, pendingIntent = pendingIntent, color = Color.YELLOW)
    }

    fun sendReminderNotification(title: String, message: String, notificationId: Int, actions: List<NotificationCompat.Action> = emptyList()) {
        showNotification(notificationId, Channels.REMINDER_CHANNEL_ID, R.drawable.ic_launcher_foreground, title, message, color = Color.GREEN, actions = actions)
    }

    fun showAutoApprovalNotification(title: String, message: String, itemId: String) {
        showNotification(itemId.hashCode(), Channels.AUTO_APPROVAL_CHANNEL_ID, R.drawable.ic_launcher_foreground, title, message, color = Color.CYAN)
    }

    fun showSimulatedPublishNotification(postTitle: String, pageName: String) {
        showNotification(postTitle.hashCode(), Channels.PUBLISH_SIMULATOR_CHANNEL_ID, R.drawable.ic_launcher_foreground, "🚀 Simulación: Post Publicado", "El contenido '$postTitle' ha sido enviado a $pageName.", color = Color.MAGENTA)
    }

    fun showDailySummaryNotification(scheduledCount: Int, pendingSchemes: Int) {
        val msg = "Hoy: $scheduledCount programados, $pendingSchemes pendientes."
        showNotification(NotificationIds.DAILY_SUMMARY, Channels.DAILY_SUMMARY_CHANNEL_ID, R.drawable.ic_launcher_foreground, "Buenos días ☀️ Tu agenda", msg, color = Color.BLUE)
    }

    fun showServerErrorNotification() {
        showNotification(NotificationIds.SERVER_ERROR, Channels.SYSTEM_ERRORS_CHANNEL_ID, android.R.drawable.stat_notify_error, "Servidor Offline ⚠️", "No se pudo conectar con XAMPP. Verifica tu red.", color = Color.RED, onlyAlertOnce = true)
    }

    fun showAIErrorNotification(errorDetail: String) {
        val isQuota = errorDetail.contains("quota", ignoreCase = true)
        val title = if (isQuota) "Cuota Agotada 📉" else "IA Bajo Demanda 🧠"
        val message = if (isQuota) "Has alcanzado el límite gratuito de Gemini." else "Gemini está saturado. Reintentando..."
        showNotification(NotificationIds.AI_ERROR, Channels.SYSTEM_ERRORS_CHANNEL_ID, android.R.drawable.stat_notify_error, title, message, color = Color.MAGENTA, onlyAlertOnce = true)
    }

    fun removeNotification(notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(id: String, name: String, desc: String, vibrationPattern: LongArray? = null, lightColor: Int? = null): NotificationChannel {
        val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH).apply {
            description = desc
            lightColor?.let { enableLights(true); this.lightColor = it }
            vibrationPattern?.let { enableVibration(true); this.vibrationPattern = it }
        }
        return channel
    }

    private fun notificationBuilder(channelId: String, title: String?, msg: String?, icon: Int, largeIcon: Bitmap? = null, pendingIntent: PendingIntent? = null, color: Int = Color.BLUE, autoCancel: Boolean = false, onlyAlertOnce: Boolean = false, actions: List<NotificationCompat.Action> = emptyList()): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, channelId).setSmallIcon(icon).setContentTitle(title).setContentText(msg).setStyle(NotificationCompat.BigTextStyle().bigText(msg)).setColor(color).setAutoCancel(autoCancel).setOnlyAlertOnce(onlyAlertOnce)
        largeIcon?.let { builder.setLargeIcon(it) }
        pendingIntent?.let { builder.setContentIntent(it) }
        if (actions.isNotEmpty()) actions.forEach { builder.addAction(it) }
        return builder
    }
}
