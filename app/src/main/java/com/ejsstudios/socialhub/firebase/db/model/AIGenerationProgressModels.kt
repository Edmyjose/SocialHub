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

import androidx.annotation.Keep

/**
 * Representa un paso en el proceso de generación de IA con checklist.
 */
@Keep
data class AIGenerationStep(
    val id: String,
    val title: String,
    val description: String? = null,
    val status: StepStatus = StepStatus.PENDING,
    val error: String? = null
)

/**
 * Estado completo del progreso de generación de IA.
 */
@Keep
data class AIGenerationProgressState(
    val steps: List<AIGenerationStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val isComplete: Boolean = false,
    val hasFailed: Boolean = false
) {
    val currentStep: AIGenerationStep? get() = steps.getOrNull(currentStepIndex)
}

/**
 * Helper para crear los pasos de generación de un esquema.
 */
fun createSchemaGenerationSteps(): List<AIGenerationStep> {
    return listOf(
        AIGenerationStep(
            "validate_novelty",
            "Verificando novedad temática",
            "Asegurando que el tema sea original"
        ),
        AIGenerationStep(
            "semantic_search",
            "Buscando contexto relevante",
            "Consultando tus libros entrenados"
        ),
        AIGenerationStep("prepare_prompt", "Optimizando mensaje", "Calculando tamaño y tokens"),
        AIGenerationStep("execute_ai", "Redactando con IA", "Generando esquema creativo"),
        AIGenerationStep(
            "update_memory",
            "Actualizando memoria",
            "Guardando el contexto para mañana"
        )
    )
}

/**
 * Helper para crear los pasos de generación de un post.
 */
fun createPostGenerationSteps(): List<AIGenerationStep> {
    return listOf(
        AIGenerationStep(
            "semantic_search",
            "Buscando contexto",
            "Extrayendo info del esquema y marca"
        ),
        AIGenerationStep("prepare_prompt", "Calculando tokens", "Optimizando el prompt del post"),
        AIGenerationStep("execute_ai", "Redactando contenido", "Creando el copy persuasivo"),
        AIGenerationStep(
            "update_memory",
            "Sincronizando memoria",
            "Actualizando base de conocimientos"
        )
    )
}
