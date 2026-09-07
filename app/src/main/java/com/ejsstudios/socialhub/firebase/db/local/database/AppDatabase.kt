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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ejsstudios.socialhub.firebase.db.local.dao.CampaignDao
import com.ejsstudios.socialhub.firebase.db.local.dao.ContentDao
import com.ejsstudios.socialhub.firebase.db.local.dao.SchemeDao
import com.ejsstudios.socialhub.firebase.db.local.dao.SocialMediaDao
import com.ejsstudios.socialhub.firebase.db.local.dao.UserDao
import com.ejsstudios.socialhub.firebase.db.local.entity.CampaignEntity
import com.ejsstudios.socialhub.firebase.db.local.entity.ContentEntity
import com.ejsstudios.socialhub.firebase.db.local.entity.ContextConfigEntity
import com.ejsstudios.socialhub.firebase.db.local.entity.MediaEntity
import com.ejsstudios.socialhub.firebase.db.local.entity.SchemeEntity
import com.ejsstudios.socialhub.firebase.db.local.entity.SocialMediaEntity
import com.ejsstudios.socialhub.firebase.db.local.entity.UserEntity
import com.ejsstudios.socialhub.firebase.db.local.dao.ContextDao

@Database(
    entities = [
        UserEntity::class,
        SocialMediaEntity::class,
        MediaEntity::class,
        CampaignEntity::class,
        SchemeEntity::class,
        ContentEntity::class,
        ContextConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun socialMediaDao(): SocialMediaDao
    abstract fun campaignDao(): CampaignDao
    abstract fun schemeDao(): SchemeDao
    abstract fun contentDao(): ContentDao
    abstract fun contextDao(): ContextDao

    companion object {
        private const val DB_NAME = "social_media_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        DB_NAME
                    )
                        .fallbackToDestructiveMigration(true)
                        .build()

                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    throw e
                }
            }
        }
    }
}
