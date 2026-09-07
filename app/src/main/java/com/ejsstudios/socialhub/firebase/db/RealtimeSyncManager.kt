/*
 * Copyright (c) 2026. EJS Studios. Todos los derechos reservados.  
 * Este código fuente es propiedad de EJS Studios y está protegido por las leyes de derechos de autor.  
 * No se permite la copia, distribución, modificación o uso de este código fuente, total o parcialmente, sin  
 * el consentimiento previo y por escrito de EJS Studios.  
 *
 * Cualquier uso no autorizado de este código fuente será perseguido legalmente según las leyes aplicables.  
 *
 * Para obtener una licencia de uso, contacta a: info@ejsstudios.com  
 */
package com.ejsstudios.socialhub.firebase.db

import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.firebase.db.local.dao.CampaignDao
import com.ejsstudios.socialhub.firebase.db.local.dao.ContentDao
import com.ejsstudios.socialhub.firebase.db.local.dao.ContextDao
import com.ejsstudios.socialhub.firebase.db.local.dao.SchemeDao
import com.ejsstudios.socialhub.firebase.db.local.dao.SocialMediaDao
import com.ejsstudios.socialhub.firebase.db.local.dao.UserDao
import com.ejsstudios.socialhub.firebase.db.local.entity.toEntity
import com.ejsstudios.socialhub.firebase.db.model.CampaignFirestore
import com.ejsstudios.socialhub.firebase.db.model.ChannelAIContext
import com.ejsstudios.socialhub.firebase.db.model.ContentPost
import com.ejsstudios.socialhub.firebase.db.model.ContextMedia
import com.ejsstudios.socialhub.firebase.db.model.SchemeFirestore
import com.ejsstudios.socialhub.firebase.db.model.SocialMediaFirestore
import com.ejsstudios.socialhub.firebase.db.model.UserFirestore
import com.ejsstudios.socialhub.firebase.db.states.FirestoreState
import com.ejsstudios.socialhub.managers.PreferencesManager
import com.ejsstudios.socialhub.utils.Constants.COLLECTION_USERS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Director de Sincronización en Tiempo Real.
 * Mantiene a Room como un espejo fiel de Firestore para los 5 niveles de datos.
 */
class RealtimeSyncManager(
    private val userDao: UserDao,
    private val socialMediaDao: SocialMediaDao,
    private val campaignDao: CampaignDao,
    private val schemeDao: SchemeDao,
    private val contentDao: ContentDao,
    private val contextDao: ContextDao,
    private val prefs: PreferencesManager
) {
    private val TAG = "RealtimeSyncManager"
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJobs = mutableMapOf<String, Job>()
    private var currentUserId: String? = null

    private val _isInitialSyncComplete = MutableStateFlow(false)
    val isInitialSyncComplete: StateFlow<Boolean> = _isInitialSyncComplete.asStateFlow()

    /**
     * Inicia los 5 canales de sincronización para un usuario.
     */
    fun startSync(userId: String) {
        if (userId == currentUserId && syncJobs.isNotEmpty()) {
            Loggers.log("e", TAG, "Sincronización ya activa para $userId. Omitiendo reinicio.")
            return
        }

        if (syncJobs.isNotEmpty()) stopSync()
        currentUserId = userId
        Loggers.log("e", TAG, ">>> INICIANDO SINCRONIZACIÓN MAESTRA PARA: $userId <<<")
        _isInitialSyncComplete.value = false

        // Nivel 0: Perfil (Documento Único)
        syncJobs["profile"] = syncScope.launch {
            FirestoreManager.getCollectionDocumentByIdFlow(
                userId,
                COLLECTION_USERS,
                UserFirestore::class
            )
                .collect { state ->
                    if (state is FirestoreState.Success) {
                        state.data?.let { user ->
                            Loggers.log("e", TAG, "Sincronizando Perfil: ${user.name}")
                            userDao.insert(user.toEntity())
                            prefs.saveUser(user)
                        }
                        // La primera emisión (incluso null) marca que el nivel 0 respondió
                        if (!_isInitialSyncComplete.value) checkInitialSyncStatus(state)
                    } else if (state is FirestoreState.Error) {
                        Loggers.log(
                            "e",
                            TAG,
                            "❌ Error Nivel 0 (Perfil): ${state.exception.message}"
                        )
                    }
                }
        }


        // Nivel 1: Social Media (Subcolección)
        syncJobs["socialmedia"] = syncScope.launch {
            FirestoreManager.getSubcollectionDocumentsFlow(
                userId, "socialmedia",
                SocialMediaFirestore::class
            )
                .collect { state ->
                    if (state is FirestoreState.Success) {
                        Loggers.log("e", TAG, "Sincronizando ${state.data.size} Cuentas Sociales")
                        socialMediaDao.insertAll(state.data.map { it.toEntity() })
                    } else if (state is FirestoreState.Error) {
                        Loggers.log(
                            "e",
                            TAG,
                            "❌ Error Nivel 1 (socialmedia): ${state.exception.message}"
                        )
                    }
                }
        }

        // Nivel 2: Campañas (Group Query)
        syncJobs["campaigns"] = syncScope.launch {
            FirestoreManager.getCollectionGroupDocumentsFlow(
                "campaigns",
                "user_id",
                userId,
                CampaignFirestore::class
            ).collect { state ->
                if (state is FirestoreState.Success) {
                    Loggers.log("e", TAG, "Sincronizando ${state.data.size} Campañas")
                    state.data.forEach { campaignDao.insert(it.toEntity()) }
                } else if (state is FirestoreState.Error) {
                    Loggers.log(
                        "e",
                        TAG,
                        "❌ Error Nivel 2 (Campañas Group): ${state.exception.message}"
                    )
                    Loggers.log("e", TAG, "👉 Link para índice: ${state.exception.localizedMessage}")
                }
            }
        }

        // Nivel 3: Esquemas (Group Query)
        syncJobs["schemes"] = syncScope.launch {
            FirestoreManager.getCollectionGroupDocumentsFlow(
                "schemes",
                "user_id",
                userId,
                SchemeFirestore::class
            ).collect { state ->
                if (state is FirestoreState.Success) {
                    Loggers.log("e", TAG, "Sincronizando ${state.data.size} Esquemas")
                    state.data.forEach { schemeDao.insert(it.toEntity()) }
                } else if (state is FirestoreState.Error) {
                    Loggers.log(
                        "e",
                        TAG,
                        "❌ Error Nivel 3 (Esquemas Group): ${state.exception.message}"
                    )
                    Loggers.log("e", TAG, "👉 Link para índice: ${state.exception.localizedMessage}")
                }
            }
        }

        // Nivel 4: Contenidos (Group Query)
        syncJobs["contents"] = syncScope.launch {
            FirestoreManager.getCollectionGroupDocumentsFlow(
                "contents",
                "user_id",
                userId,
                ContentPost::class
            ).collect { state ->
                if (state is FirestoreState.Success) {
                    Loggers.log("e", TAG, "Sincronizando ${state.data.size} Contenidos")
                    state.data.forEach { contentDao.insert(it.toEntity()) }
                } else if (state is FirestoreState.Error) {
                    Loggers.log(
                        "e",
                        TAG,
                        "❌ Error Nivel 4 (Contenidos Group): ${state.exception.message}"
                    )
                    Loggers.log("e", TAG, "👉 Link para índice: ${state.exception.localizedMessage}")
                }
            }
        }

        // Nivel 5: Contexto Config (Group Query)
        syncJobs["context"] = syncScope.launch {
            FirestoreManager.getCollectionGroupDocumentsFlow(
                "context",
                "userId",
                userId,
                ChannelAIContext::class
            ).collect { state ->
                if (state is FirestoreState.Success) {
                    Loggers.log(
                        "e",
                        TAG,
                        "Sincronizando ${state.data.size} Configuraciones de Contexto"
                    )
                    state.data.forEach { contextDao.insertConfig(it.toEntity()) }
                } else if (state is FirestoreState.Error) {
                    Loggers.log(
                        "e",
                        TAG,
                        "❌ Error Nivel 5 (Context Group): ${state.exception.message}"
                    )
                    Loggers.log("e", TAG, "👉 Link para índice: ${state.exception.localizedMessage}")
                }
            }
        }

        // Nivel 6: Media (Group Query)
        syncJobs["media"] = syncScope.launch {
            FirestoreManager.getCollectionGroupDocumentsFlow(
                "media",
                "userId",
                userId,
                ContextMedia::class
            ).collect { state ->
                if (state is FirestoreState.Success) {
                    Loggers.log("e", TAG, "Sincronizando ${state.data.size} Archivos de Media")
                    contextDao.insertMedia(state.data.map { it.toEntity() })
                } else if (state is FirestoreState.Error) {
                    Loggers.log(
                        "e",
                        TAG,
                        "❌ Error Nivel 6 (Media Group): ${state.exception.message}"
                    )
                    Loggers.log("e", TAG, "👉 Link para índice: ${state.exception.localizedMessage}")
                }
            }
        }
    }

    private fun checkInitialSyncStatus(profileState: FirestoreState<UserFirestore?>) {
        // En un flujo real, aquí podríamos esperar a que todos los niveles emitan su primer snapshot.
        // Por ahora, el Perfil (Nivel 0) es el bloqueante para el Login.
        _isInitialSyncComplete.value = true
    }

    /**
     * Detiene todas las escuchas activas.
     */
    fun stopSync() {
        Loggers.log("e", TAG, "Deteniendo Sincronización Maestra.")
        syncJobs.values.forEach { it.cancel() }
        syncJobs.clear()
        currentUserId = null
        _isInitialSyncComplete.value = false
    }
}
