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

package com.ejsstudios.socialhub.managers

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ejsstudios.socialhub.firebase.db.model.SocialMediaFirestore
import com.ejsstudios.socialhub.firebase.db.model.UserFirestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_prefs_v2")

/**
 * Fuente Única de Verdad (SSOT) reactiva usando DataStore y Moshi.
 */
class PreferencesManager(private val context: Context) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private object Keys {
        val USER = stringPreferencesKey("session_user")
        val CHANNELS = stringPreferencesKey("session_channels")
        val SELECTED_CHANNEL = stringPreferencesKey("session_selected_channel")
        val IS_PREMIUM = booleanPreferencesKey("is_premium_user")
        val DEFAULT_REVIEW_HOURS = intPreferencesKey("default_review_hours")
        val GLOBAL_AUTO_APPROVE = booleanPreferencesKey("global_auto_approve")
        val LAST_VIGILANTE_RUN = stringPreferencesKey("last_vigilante_run")
        val VIGILANTE_PERIODICITY_HOURS = intPreferencesKey("vigilante_periodicity_hours")
        val APP_LAUNCH_COUNT = intPreferencesKey("app_launch_count")
        val FIRST_LAUNCH_DATE = stringPreferencesKey("first_launch_date")
        val REVIEW_FLOW_COMPLETED = booleanPreferencesKey("review_flow_completed")
        val USE_12H_FORMAT = booleanPreferencesKey("use_12h_format")
    }

    // --- Flows Reactivos ---

    val appLaunchCount: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.APP_LAUNCH_COUNT] ?: 0
    }.distinctUntilChanged()

    val firstLaunchDate: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.FIRST_LAUNCH_DATE]?.toLong() ?: 0L
    }.distinctUntilChanged()

    val reviewFlowCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.REVIEW_FLOW_COMPLETED] ?: false
    }.distinctUntilChanged()

    val lastVigilanteRunFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_VIGILANTE_RUN] ?: "Nunca"
    }.distinctUntilChanged()

    val vigilantePeriodicityFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.VIGILANTE_PERIODICITY_HOURS] ?: 4 // Default 4 horas
    }.distinctUntilChanged()

    val defaultReviewHoursFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_REVIEW_HOURS] ?: 2 // Valor por defecto 2 horas
    }.distinctUntilChanged()

    val globalAutoApproveFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.GLOBAL_AUTO_APPROVE] ?: false
    }.distinctUntilChanged()

    val userFlow: Flow<UserFirestore?> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER]?.let { moshi.adapter(UserFirestore::class.java).fromJson(it) }
    }.distinctUntilChanged()

    val channelsFlow: Flow<List<SocialMediaFirestore>> = context.dataStore.data.map { prefs ->
        prefs[Keys.CHANNELS]?.let { json ->
            val type =
                Types.newParameterizedType(List::class.java, SocialMediaFirestore::class.java)
            moshi.adapter<List<SocialMediaFirestore>>(type).fromJson(json)
        } ?: emptyList()
    }.distinctUntilChanged()

    val selectedPageFlow: Flow<SocialMediaFirestore?> = context.dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_CHANNEL]?.let { json ->
            moshi.adapter(SocialMediaFirestore::class.java).fromJson(json)
        }
    }.distinctUntilChanged()

    val isPremiumFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_PREMIUM] ?: false
    }.distinctUntilChanged()

    val use12hFormatFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.USE_12H_FORMAT] ?: false // Default 24h
    }.distinctUntilChanged()

    // --- Métodos de Lectura Directa (One-shot) ---

    /**
     * Obtiene el usuario actual de forma directa.
     */
    suspend fun getUser(): UserFirestore? {
        val prefs = context.dataStore.data.first()
        return prefs[Keys.USER]?.let { moshi.adapter(UserFirestore::class.java).fromJson(it) }
    }

    /**
     * Obtiene si el usuario es premium de forma directa.
     */
    suspend fun isPremium(): Boolean {
        return context.dataStore.data.first()[Keys.IS_PREMIUM] ?: false
    }

    // --- Métodos de Escritura (Suspend) ---

    suspend fun saveUser(user: UserFirestore?) {
        val json = user?.let { moshi.adapter(UserFirestore::class.java).toJson(it) }
        context.dataStore.edit { prefs ->
            if (json != null) prefs[Keys.USER] = json else prefs.remove(Keys.USER)
        }
    }

    suspend fun savechannels(channels: List<SocialMediaFirestore>) {
        val type = Types.newParameterizedType(List::class.java, SocialMediaFirestore::class.java)
        val json = moshi.adapter<List<SocialMediaFirestore>>(type).toJson(channels)
        context.dataStore.edit { prefs -> prefs[Keys.CHANNELS] = json }
    }

    suspend fun saveSelectedPage(page: SocialMediaFirestore?) {
        val json = page?.let { moshi.adapter(SocialMediaFirestore::class.java).toJson(it) }
        context.dataStore.edit { prefs ->
            if (json != null) prefs[Keys.SELECTED_CHANNEL] =
                json else prefs.remove(Keys.SELECTED_CHANNEL)
        }
    }

    suspend fun setPremiumStatus(isPremium: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_PREMIUM] = isPremium }
    }

    suspend fun saveDefaultReviewHours(hours: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.DEFAULT_REVIEW_HOURS] = hours }
    }

    suspend fun saveGlobalAutoApprove(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.GLOBAL_AUTO_APPROVE] = enabled }
    }

    suspend fun saveLastVigilanteRun(timestamp: String) {
        context.dataStore.edit { prefs -> prefs[Keys.LAST_VIGILANTE_RUN] = timestamp }
    }

    suspend fun saveVigilantePeriodicity(hours: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.VIGILANTE_PERIODICITY_HOURS] = hours }
    }

    suspend fun incrementAppLaunchCount() {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.APP_LAUNCH_COUNT] ?: 0
            prefs[Keys.APP_LAUNCH_COUNT] = current + 1
        }
    }

    suspend fun setFirstLaunchDate(date: Long) {
        context.dataStore.edit { prefs -> prefs[Keys.FIRST_LAUNCH_DATE] = date.toString() }
    }

    suspend fun setReviewFlowCompleted(completed: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.REVIEW_FLOW_COMPLETED] = completed }
    }

    suspend fun saveUse12hFormat(use12h: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.USE_12H_FORMAT] = use12h }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
