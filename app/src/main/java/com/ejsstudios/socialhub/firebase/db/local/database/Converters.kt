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

package com.ejsstudios.socialhub.firebase.db.local.database

import androidx.room.TypeConverter
import com.ejsstudios.socialhub.firebase.db.model.CampaignStatus
import com.ejsstudios.socialhub.firebase.db.model.ContentStatus
import com.ejsstudios.socialhub.firebase.db.model.PostIdea
import com.ejsstudios.socialhub.firebase.db.model.SchemeStatus
import com.ejsstudios.socialhub.firebase.db.model.SchemeType
import com.ejsstudios.socialhub.firebase.db.model.SocialPlatform
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    private val postIdeaListType =
        Types.newParameterizedType(List::class.java, PostIdea::class.java)
    private val postIdeaListAdapter = moshi.adapter<List<PostIdea>>(postIdeaListType)

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { stringListAdapter.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { stringListAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromPostIdeaList(value: List<PostIdea>?): String? {
        return value?.let { postIdeaListAdapter.toJson(it) }
    }

    @TypeConverter
    fun toPostIdeaList(value: String?): List<PostIdea>? {
        return value?.let { postIdeaListAdapter.fromJson(it) }
    }

    private val floatListType =
        Types.newParameterizedType(List::class.java, Float::class.javaObjectType)
    private val floatListAdapter = moshi.adapter<List<Float>>(floatListType)

    @TypeConverter
    fun fromFloatList(value: List<Float>?): String? {
        return value?.let { floatListAdapter.toJson(it) }
    }

    @TypeConverter
    fun toFloatList(value: String?): List<Float>? {
        return value?.let { floatListAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromSocialPlatform(value: SocialPlatform?): String? = value?.id

    @TypeConverter
    fun toSocialPlatform(value: String?): SocialPlatform? =
        value?.let { SocialPlatform.fromId(it) }

    @TypeConverter
    fun fromCampaignStatus(value: CampaignStatus?): String? = value?.id

    @TypeConverter
    fun toCampaignStatus(value: String?): CampaignStatus? =
        value?.let { CampaignStatus.fromId(it) }

    @TypeConverter
    fun fromContentStatus(value: ContentStatus?): String? = value?.id

    @TypeConverter
    fun toContentStatus(value: String?): ContentStatus? =
        value?.let { ContentStatus.fromId(it) }

    @TypeConverter
    fun fromSchemeStatus(value: SchemeStatus?): String? = value?.id

    @TypeConverter
    fun toSchemeStatus(value: String?): SchemeStatus? =
        value?.let { SchemeStatus.fromId(it) }

    @TypeConverter
    fun fromSchemeType(value: SchemeType?): String? = value?.name

    @TypeConverter
    fun toSchemeType(value: String?): SchemeType? =
        value?.let { valueStr ->
            try {
                SchemeType.valueOf(valueStr)
            } catch (e: Exception) {
                null
            }
        }
}
