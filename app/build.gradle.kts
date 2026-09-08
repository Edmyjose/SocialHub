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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// Carga de local.properties para manejar llaves de API de forma segura
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {  
    namespace = "com.ejsstudios.socialhub"
    compileSdk {
        version = release(37)
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.ejsstudios.socialhub"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "0.0.001"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inyección de API Keys desde local.properties
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProperties.getProperty("GEMINI_API_KEY") ?: ""}\""
        )
        buildConfigField(
            "String",
            "OPENROUTER_API_KEY",
            "\"${localProperties.getProperty("OPENROUTER_API_KEY") ?: ""}\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // --- Core Android & Lifecycle ---
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.core:core-splashscreen:1.2.0")

    // --- Jetpack Compose (BOM) ---
    implementation(platform("androidx.compose:compose-bom:2026.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.activity:activity-compose:1.13.0")

    // Lottie Animations
    implementation("com.airbnb.android:lottie-compose:6.7.1")

    // --- Navigation ---
    implementation("androidx.navigation:navigation-compose:2.10.0")

    // --- UI Accompanist & Helpers ---
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
    implementation("com.google.accompanist:accompanist-pager:0.36.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.36.0")

    // --- Image Loading & Processing ---
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.exifinterface:exifinterface:1.4.2")

    // --- Networking & API (Retrofit, OkHttp, Moshi) ---
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-moshi:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.5.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.5.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.2")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.11.2")

    // Preferences DataStore
    implementation("androidx.datastore:datastore-preferences:1.2.1")

    // --- Room Database (Local Cache) ---
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    // --- Firebase & Ads ---
    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))
    //noinspection LoginCredentials
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-crashlytics")

    // Si usas Google Auth o servicios específicos
    //noinspection LoginCredentials
    implementation("com.google.android.gms:play-services-auth:22.0.0")
    implementation("com.google.android.gms:play-services-base:18.10.1")

    // --- Google Authentication & Identity (Modern Credential Manager) ---
    //noinspection LoginCredentials
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")
    //noinspection LoginCredentials
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
    //noinspection LoginCredentials
    implementation("androidx.credentials:credentials:1.6.0")

    // --- Social SDKs ---
    implementation("com.facebook.android:facebook-login:18.3.0")

    // --- Play Store Services & Billing ---
    implementation("com.google.android.play:review-ktx:2.0.2")

    // --- Testing (Unit Tests) ---
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation("androidx.test:core:1.7.0")
    testImplementation("androidx.test.ext:junit:1.3.0")
    testImplementation("io.mockk:mockk:1.14.11")
    testImplementation("io.mockk:mockk-android:1.14.11")

    // --- Testing (Android Instrumentation Tests) ---
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test:runner:1.7.0")

    // --- Debug ---
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
