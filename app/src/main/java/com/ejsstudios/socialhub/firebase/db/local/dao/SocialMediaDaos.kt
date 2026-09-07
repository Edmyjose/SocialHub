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
import androidx.room.Transaction
import com.ejsstudios.socialhub.firebase.db.local.entity.CampaignEntity
import com.ejsstudios.socialhub.firebase.db.local.entity.CampaignWithDetails
import com.ejsstudios.socialhub.firebase.db.local.entity.ContentEntity
import com.ejsstudios.socialhub.firebase.db.local.entity.SchemeEntity
import com.ejsstudios.socialhub.firebase.db.local.entity.SocialMediaEntity
import com.ejsstudios.socialhub.firebase.db.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}

@Dao
interface SocialMediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pages: List<SocialMediaEntity>)

    @Query("SELECT * FROM socialmedia WHERE userId = :userId")
    fun getPagesByUserIdFlow(userId: String): Flow<List<SocialMediaEntity>>

    @Query("DELETE FROM socialmedia WHERE userId = :userId")
    suspend fun deletePagesByUserId(userId: String)
}

@Dao
interface CampaignDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(campaign: CampaignEntity)

    @Transaction
    @Query("SELECT * FROM campaigns WHERE channelId = :channelId")
    fun getCampaignsByPageIdFlow(channelId: String): Flow<List<CampaignWithDetails>>

    @Transaction
    @Query("SELECT * FROM campaigns WHERE status = 'ACTIVE'")
    suspend fun getAllActiveCampaigns(): List<CampaignWithDetails>

    @Transaction
    @Query("SELECT * FROM campaigns WHERE id = :campaignId")
    suspend fun getCampaignById(campaignId: String): CampaignWithDetails?

    @Query("DELETE FROM campaigns WHERE id = :campaignId")
    suspend fun deleteById(campaignId: String)
}

@Dao
interface SchemeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scheme: SchemeEntity)

    @Query("SELECT * FROM schemes WHERE campaignId = :campaignId")
    fun getSchemesByCampaignIdFlow(campaignId: String): Flow<List<SchemeEntity>>

    @Query("SELECT * FROM schemes WHERE id = :schemeId")
    suspend fun getSchemeById(schemeId: String): SchemeEntity?

    @Query("SELECT * FROM schemes WHERE campaignId = :campaignId AND targetDate = :date")
    suspend fun getSchemeByDate(campaignId: String, date: String): SchemeEntity?

    @Query("SELECT COUNT(*) FROM schemes WHERE status = 'PENDING'")
    suspend fun getPendingSchemesCount(): Int

    @Query("DELETE FROM schemes WHERE id = :schemeId")
    suspend fun deleteById(schemeId: String)

    @Query("DELETE FROM schemes WHERE campaignId = :campaignId")
    suspend fun deleteByCampaignId(campaignId: String)
}

@Dao
interface ContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(content: ContentEntity)

    @Query("SELECT * FROM contents WHERE channelId = :channelId ORDER BY createdAt DESC")
    fun getContentsByPageIdFlow(channelId: String): Flow<List<ContentEntity>>

    @Query("SELECT COUNT(*) FROM contents WHERE status = 'SCHEDULED' AND scheduledTimestamp >= :startOfDay AND scheduledTimestamp <= :endOfDay")
    suspend fun getScheduledPostsCountForDay(startOfDay: Long, endOfDay: Long): Int

    @Query("DELETE FROM contents WHERE id = :contentId")
    suspend fun deleteById(contentId: String)

    @Query("DELETE FROM contents WHERE campaignId = :campaignId")
    suspend fun deleteByCampaignId(campaignId: String)

    @Query("DELETE FROM contents WHERE schemeId = :schemeId")
    suspend fun deleteBySchemeId(schemeId: String)
}
