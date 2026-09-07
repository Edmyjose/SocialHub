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
package com.ejsstudios.socialhub.firebase.db.model

/** Estado de un paso en el proceso de guardado */
enum class StepStatus {
    PENDING, // Pendiente
    IN_PROGRESS, // En progreso
    COMPLETED, // Completado
    FAILED // Fallido
}

/** Representa un paso en el proceso de guardado */
data class SaveStep(
    val id: String,
    val title: String,
    val description: String,
    val status: StepStatus = StepStatus.PENDING,
    val progress: Float = 0f, // 0.0 a 1.0
    val error: String? = null
)

/** Estado completo del proceso de guardado */
data class SaveProgressState(
    val steps: List<SaveStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val isComplete: Boolean = false,
    val hasFailed: Boolean = false,
    val overallProgress: Float = 0f // 0.0 a 1.0
) {
    val currentStep: SaveStep? get() = steps.getOrNull(currentStepIndex)
}

/** Crea los pasos del proceso de guardado según el contenido */
fun createSaveSteps(hasContextFile: Boolean, imageCount: Int, videoCount: Int): List<SaveStep> {
    val steps = mutableListOf<SaveStep>()

    // Paso 1: Validar
    steps.add(
        SaveStep(
            id = "validate",
            title = "Validar datos",
            description = "Verificando consistencia..."
        )
    )

    // Paso 2: Procesar archivo de contexto
    steps.add(
        SaveStep(
            id = "upload_context_file",
            title = "Procesar contexto",
            description = "Sincronizando con la nube..."
        )
    )

    // Paso 3: Procesar imágenes (si existen)
    if (imageCount > 0) {
        steps.add(
            SaveStep(
                id = "upload_images",
                title = "Subir imágenes ($imageCount)",
                description = "Enviando archivos a Storage..."
            )
        )
    }

    // Paso 4: Procesar videos (si existen)
    if (videoCount > 0) {
        steps.add(
            SaveStep(
                id = "upload_videos",
                title = "Subir videos ($videoCount)",
                description = "Enviando archivos a Storage..."
            )
        )
    }

    // Paso 5: Finalizar
    steps.add(
        SaveStep(
            id = "finalize",
            title = "Finalizar",
            description = "Completando sincronización..."
        )
    )

    return steps
}
