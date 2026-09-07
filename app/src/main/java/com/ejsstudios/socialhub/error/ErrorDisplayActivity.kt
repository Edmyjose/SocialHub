/*
 * Copyright (c) 2025-2026. EJS Studios. Todos los derechos reservados.
 * Este código fuente es propiedad de EJS Studios y está protegido por las leyes de derechos de autor.
 * No se permite la copia, distribución, modificación o uso de este código fuente, total o parcialmente, sin
 * el consentimiento previo y por escrito de EJS Studios.
 *
 * Cualquier uso no autorizado de este código fuente será perseguido legalmente según las leyes aplicables.
 *
 * Para obtener una licencia de uso, contacta a: info@ejsstudios.com
 */
package com.ejsstudios.socialhub.error

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ejsstudios.socialhub.BuildConfig
import com.ejsstudios.socialhub.R
import androidx.compose.ui.tooling.preview.Preview
import com.ejsstudios.socialhub.ui.theme.SocialHubTheme
import kotlin.system.exitProcess

/**
 * Activity que muestra información detallada sobre errores no capturados en la aplicación.
 */
class ErrorDisplayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val errorCode = intent.getIntExtra("error_code", -1)
        val errorType = intent.getStringExtra("error_type") ?: getString(R.string.error_title_attention)

        val errorMessage = if (errorCode != -1) {
            ErrorCodes.getFriendlyMessage(this, errorCode)
        } else {
            intent.getStringExtra("error_message") ?: getString(R.string.error_unknown)
        }

        setContent {
            SocialHubTheme(darkTheme = true) {
                ErrorScreen(
                    errorMessage = errorMessage, 
                    errorType = errorType,
                    errorCode = if (errorCode != -1) errorCode else null
                ) 
            }
        }
    }

    /**
     * Reinicia la aplicación de forma limpia.
     */
    private fun restartApp() {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
            finishAffinity()
            exitProcess(0)
        } catch (e: Exception) {
            Toast.makeText(this, "Error restarting: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Borra todos los datos de la aplicación.
     */
    private fun clearAppData() {
        try {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            activityManager.clearApplicationUserData()
            Toast.makeText(this, getString(R.string.msg_operation_success), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Copia el mensaje de error al portapapeles para facilitar el soporte.
     */
    private fun copyErrorToClipboard(message: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Error Log", message)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.lbl_copied_to_clipboard), Toast.LENGTH_SHORT).show()
    }

    @Composable
    fun ErrorScreen(
        errorMessage: String,
        errorType: String,
        errorCode: Int? = null,
        isDebug: Boolean = BuildConfig.DEBUG
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.error_title_attention),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = errorType,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            if (errorCode != null) {
                Text(
                    text = "Code: $errorCode",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Si es Debug, mostramos la caja técnica. Si no, solo el mensaje simple.
            if (isDebug) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botones de acción
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Fila de botones principales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { restartApp() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.error_button_restart))
                    }

                    Button(
                        onClick = { copyErrorToClipboard("$errorType: $errorMessage") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.lbl_copy_error))
                    }
                }

                Button(
                    onClick = { clearAppData() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.error_button_delete_data))
                }

                TextButton(onClick = { finish() }) {
                    Text(stringResource(R.string.error_button_close))
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_5", name = "Debug Mode")
@Composable
private fun ErrorScreenDebugPreview() {
    SocialHubTheme(darkTheme = true) {
        ErrorDisplayActivity().ErrorScreen(
            errorMessage = "java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String com.ejsstudios.socialhub.data.entity.UserEntity.getUsername()' on a null object reference\n" +
                    "\tat com.ejsstudios.socialhub.viewmodels.MainViewModel.loadData(MainViewModel.kt:452)",
            errorType = "NullPointerException",
            errorCode = 500,
            isDebug = true
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_5", name = "Release Mode")
@Composable
private fun ErrorScreenReleasePreview() {
    SocialHubTheme(darkTheme = true) {
        ErrorDisplayActivity().ErrorScreen(
            errorMessage = "java.lang.NullPointerException: Attempt to invoke virtual method",
            errorType = "NullPointerException",
            errorCode = 404,
            isDebug = false
        )
    }
}
