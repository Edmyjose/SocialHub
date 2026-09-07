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
import com.ejsstudios.socialhub.R


/**
 * Tipos de publicaciones soportadas por cada red social.
 */
@Keep
enum class SocialPostType(val id: String, val stringRes: Int) {
    // Genéricos / Facebook
    TEXT_ONLY("text_only", R.string.post_type_text),
    IMAGE("image", R.string.post_type_image),
    VIDEO("video", R.string.post_type_video),
    REELS("reels", R.string.post_type_reels),
    STORY("story", R.string.post_type_story),
    LINK_POST("link_post", R.string.post_type_link),

    // Instagram específico
    CAROUSEL("carousel", R.string.post_type_carousel),

    // X / Twitter
    THREAD("thread", R.string.post_type_thread),
    POLL("poll", R.string.post_type_poll),
    GIF("gif", R.string.post_type_gif),

    // Reddit
    REDDIT_DISCUSSION("reddit_discussion", R.string.post_type_discussion),
    REDDIT_GALLERY("reddit_gallery", R.string.post_type_gallery),
    REDDIT_COMMENT("reddit_comment", R.string.post_type_comment);

    companion object {
        fun fromId(id: String): SocialPostType? = entries.find { it.id == id }

        /**
         * Retorna los tipos de post permitidos para cada plataforma.
         */
        fun getAvailableTypesForPlatform(platform: SocialPlatform): List<SocialPostType> {
            return when (platform) {
                SocialPlatform.FACEBOOK_PAGE -> listOf(
                    TEXT_ONLY,
                    IMAGE,
                    VIDEO,
                    REELS,
                    STORY,
                    LINK_POST
                )

                SocialPlatform.FACEBOOK_GROUP -> listOf(TEXT_ONLY, IMAGE, VIDEO, LINK_POST)
                SocialPlatform.INSTAGRAM -> listOf(IMAGE, VIDEO, REELS, CAROUSEL, STORY)
                SocialPlatform.X_TWITTER -> listOf(
                    TEXT_ONLY,
                    IMAGE,
                    VIDEO,
                    GIF,
                    LINK_POST,
                    THREAD,
                    POLL
                )

                SocialPlatform.THREADS -> listOf(
                    TEXT_ONLY,
                    IMAGE,
                    VIDEO,
                    CAROUSEL,
                    THREAD,
                    LINK_POST
                )

                SocialPlatform.REDDIT -> listOf(
                    REDDIT_DISCUSSION,
                    LINK_POST,
                    REDDIT_GALLERY,
                    VIDEO,
                    POLL,
                    REDDIT_COMMENT
                )
            }
        }
    }
}
