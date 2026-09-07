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
package com.ejsstudios.socialhub

import android.app.Application
import com.ejsstudios.socialhub.apiservices.AIGenerationApiService
import com.ejsstudios.socialhub.apiservices.EmbeddingApiService
import com.ejsstudios.socialhub.error.ExceptionHandler
import com.ejsstudios.socialhub.firebase.RemoteConfigManager
import com.ejsstudios.socialhub.firebase.db.FirestoreManager
import com.ejsstudios.socialhub.firebase.db.RealtimeSyncManager
import com.ejsstudios.socialhub.firebase.db.local.database.AppDatabase
import com.ejsstudios.socialhub.firebase.db.repository.CampaignRepository
import com.ejsstudios.socialhub.firebase.db.repository.ContentRepository
import com.ejsstudios.socialhub.firebase.db.repository.ContextRepository
import com.ejsstudios.socialhub.firebase.login.LoginRepository
import com.ejsstudios.socialhub.firebase.db.repository.SchemeRepository
import com.ejsstudios.socialhub.firebase.db.repository.SocialRepository
import com.ejsstudios.socialhub.firebase.login.AuthManager
import com.ejsstudios.socialhub.managers.NotificationHandler
import com.ejsstudios.socialhub.managers.PreferencesManager
import com.google.firebase.FirebaseApp

class App : Application() {
    lateinit var notificationHandler: NotificationHandler
    lateinit var db: AppDatabase
    lateinit var aiGenerationApiService: AIGenerationApiService
    lateinit var embeddingApiService: EmbeddingApiService
    /*lateinit var imageSelectionService: ImageSelectionService
    lateinit var aiGenerationService: AIGenerationService*/

    // Infraestructura Global
    lateinit var preferencesManager: PreferencesManager
    lateinit var authManager: AuthManager

    // Repositorios Globales
    lateinit var loginRepository: LoginRepository
    lateinit var socialRepository: SocialRepository
    lateinit var campaignRepository: CampaignRepository
    lateinit var schemeRepository: SchemeRepository
    lateinit var contentRepository: ContentRepository
    lateinit var contextRepository: ContextRepository
    lateinit var syncManager: RealtimeSyncManager

    override fun onCreate() {
        super.onCreate()
        // Inicializar Firebase
        ExceptionHandler.initialize(this)
        FirebaseApp.initializeApp(this)
        notificationHandler = NotificationHandler(this)
        RemoteConfigManager.initialize(this)

        try {
            db = AppDatabase.getInstance(this)
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
        }
        preferencesManager = PreferencesManager(this)
        authManager = AuthManager()

        loginRepository = LoginRepository(this, authManager, FirestoreManager, db.userDao())
        contextRepository = ContextRepository(db.contextDao())
        schemeRepository = SchemeRepository(db.schemeDao())
        contentRepository = ContentRepository(db.contentDao())
        campaignRepository = CampaignRepository(db.campaignDao())
        socialRepository = SocialRepository(this, db.socialMediaDao(), preferencesManager, loginRepository.user)

        syncManager = RealtimeSyncManager(
            db.userDao(),
            db.socialMediaDao(),
            db.campaignDao(),
            db.schemeDao(),
            db.contentDao(),
            db.contextDao(),
            preferencesManager
        )

        /*initApiServices()

        // Inicializar Módulo de IA con Gemini
        AIModule.initialize(
            this,
            AIProvider.GEMINI
        )

        setupBackgroundWorkers()

        // Ejecutar revisión inmediata al abrir la app
        UserMasterWorker.runNow(this)*/
    }

    /*private fun setupBackgroundWorkers() {
        val workManager = WorkManager.getInstance(this)
        val prefs = PreferencesManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            val hours = try {
                prefs.vigilantePeriodicityFlow.first()
            } catch (e: Exception) {
                4
            }

            // 1. Orquestador del Usuario (Periodicidad Dinámica)
            val masterRequest = PeriodicWorkRequestBuilder<UserMasterWorker>(
                hours.toLong(), TimeUnit.HOURS
            ).build()

            workManager.enqueueUniquePeriodicWork(
                "UserMasterOrchestrator",
                ExistingPeriodicWorkPolicy.KEEP,
                masterRequest
            )
        }

        // 2. Resumen Diario (A las 8 AM)
        scheduleDailySummary(workManager)
    }

    private fun scheduleDailySummary(workManager: WorkManager) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()

        // Configurar a las 8:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 8)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        val dailySummaryRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag("DailySummaryReport")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "DailySummaryReport",
            ExistingPeriodicWorkPolicy.KEEP,
            dailySummaryRequest
        )
    }

    private fun initApiServices() {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.WEB_SERVER_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        aiGenerationApiService = retrofit.create(AIGenerationApiService::class.java)
        embeddingApiService = retrofit.create(EmbeddingApiService::class.java)
        imageSelectionService = ImageSelectionService(contextRepository, aiGenerationApiService)

        aiGenerationService = AIGenerationService(
            aiRepo = AIGenerationRepository(aiGenerationApiService),
            schemeRepo = schemeRepository,
            contentRepo = contentRepository,
            contextRepo = contextRepository,
            socialRepo = socialRepository,
            embeddingApi = embeddingApiService,
            imageSelector = imageSelectionService,
            notificationHandler = notificationHandler,
            workManager = WorkManager.getInstance(this)
        )
    }*/
}

