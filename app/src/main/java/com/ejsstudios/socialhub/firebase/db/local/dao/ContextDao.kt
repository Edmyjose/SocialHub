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
package com.ejsstudios.socialhub.firebase.db.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ejsstudios.socialhub.firebase.db.local.entity.ContextConfigEntity
import com.ejsstudios.socialhub.firebase.db.local.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContextDao {

    // Configuración de Contexto
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: ContextConfigEntity)

    @Query("SELECT * FROM context_config WHERE channelId = :channelId")
    fun getConfigBychannelIdFlow(channelId: String): Flow<ContextConfigEntity?>

    @Query("DELETE FROM context_config WHERE channelId = :channelId")
    suspend fun deleteConfigBychannelId(channelId: String)

    // Media (Imágenes/Videos)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: List<MediaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleMedia(media: MediaEntity)

    @Query("SELECT * FROM media WHERE channelId = :channelId ORDER BY createdAt DESC")
    fun getMediaBychannelIdFlow(channelId: String): Flow<List<MediaEntity>>

    @Query("DELETE FROM media WHERE id = :mediaId")
    suspend fun deleteMediaById(mediaId: String)

    @Query("DELETE FROM media WHERE channelId = :channelId")
    suspend fun deleteMediaBychannelId(channelId: String)
}
