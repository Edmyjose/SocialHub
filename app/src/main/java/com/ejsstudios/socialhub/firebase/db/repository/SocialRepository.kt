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
package com.ejsstudios.socialhub.firebase.db.repository

import android.content.Context
import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.firebase.CloudStorageManager
import com.ejsstudios.socialhub.firebase.db.FirestoreManager
import com.ejsstudios.socialhub.firebase.db.local.dao.SocialMediaDao
import com.ejsstudios.socialhub.firebase.db.model.SocialMediaFirestore
import com.ejsstudios.socialhub.firebase.db.model.UserFirestore
import com.ejsstudios.socialhub.managers.PreferencesManager
import com.ejsstudios.socialhub.utils.Constants.COLLECTION_SOCIAL_MEDIA
import com.ejsstudios.socialhub.utils.ImageUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de Cuentas Sociales.
 * Lectura 100% desde Room, Escritura directa a Firestore.
 */
class SocialRepository(
    private val context: Context,
    private val socialMediaDao: SocialMediaDao,
    private val prefs: PreferencesManager,
    val userFlow: StateFlow<UserFirestore?>
) {
    private val TAG = "SocialRepository"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val firestoreManager = FirestoreManager

    @OptIn(ExperimentalCoroutinesApi::class)
    val allPages: StateFlow<List<SocialMediaFirestore>> = userFlow.flatMapLatest { currentUser ->
        if (currentUser != null) {
            Loggers.log("e", TAG, "Observando Room para todas las páginas del usuario: ${currentUser.id}")
            socialMediaDao.getPagesByUserIdFlow(currentUser.id).map { list ->
                list.map { it.toFirestore() }
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Flow que emite solo las cuentas que están marcadas como activas. Útil para el selector del Drawer. */
    val activePages: StateFlow<List<SocialMediaFirestore>> = allPages.map { list ->
        list.filter { it.isActive }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedPage: StateFlow<SocialMediaFirestore?> = prefs.selectedPageFlow
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Observamos cambios en la lista completa para deseleccionar si una cuenta se inactiva
        scope.launch {
            allPages.collect { pages ->
                val currentSelected = prefs.selectedPageFlow.first()
                if (currentSelected != null) {
                    val matchingPage = pages.find { it.id == currentSelected.id }
                    // Si la página ya no existe o se marcó como inactiva, la deseleccionamos
                    if (matchingPage == null || !matchingPage.isActive) {
                        Loggers.log("e", TAG, "Deseleccionando cuenta inactiva o eliminada: ${currentSelected.name}")
                        prefs.saveSelectedPage(null)
                    }
                }
            }
        }
    }

    fun selectPage(page: SocialMediaFirestore) {
        scope.launch { prefs.saveSelectedPage(page) }
    }

    suspend fun checkPageOwnership(pageId: String): String? {
        val currentUser = prefs.getUser() ?: return null
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collectionGroup(COLLECTION_SOCIAL_MEDIA)
                .whereEqualTo("id", pageId)
                .get().await()

            val otherDoc = snapshot.documents.find { it.getString("userId") != currentUser.id }
            otherDoc?.getString("userId")
        } catch (e: Exception) {
            Loggers.log("e", TAG, "Error checking ownership: ${e.message}")
            null
        }
    }

    /**
     * Vínculo de página: Solo escribimos en Firestore.
     * El Listener de RealtimeSyncManager poblará Room automáticamente.
     */
    suspend fun linkPage(page: SocialMediaFirestore): Result<Boolean> {
        val currentUser = prefs.getUser() ?: return Result.failure(Exception("Sesión no iniciada"))
        return try {
            val finalPic = page.picture?.let { url ->
                if (url.startsWith("http")) {
                    ImageUtils.urlToByteArray(url)?.let { bytes ->
                        try {
                            CloudStorageManager.uploadPageProfileImage(
                                currentUser.id,
                                page.id,
                                bytes
                            )
                        } catch (e: Exception) {
                            url
                        }
                    } ?: url
                } else url
            } ?: page.picture

            val finalPage = page.copy(picture = finalPic)
            val pageData = finalPage.toMap().toMutableMap()
            pageData["userId"] = currentUser.id

            firestoreManager.addDocument(
                "users/${currentUser.id}/$COLLECTION_SOCIAL_MEDIA",
                pageData,
                finalPage.id
            )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun togglePageActivation(pageId: String, isActive: Boolean) {
        val currentUser = prefs.getUser() ?: return
        try {
            // FLUJO UNIDIRECCIONAL: Solo Firestore
            firestoreManager.updateDocument(
                "users/${currentUser.id}/$COLLECTION_SOCIAL_MEDIA",
                pageId,
                mapOf("isActive" to isActive)
            )
        } catch (e: Exception) {
            Loggers.log("e", TAG, "Error: ${e.message}")
        }
    }
}
