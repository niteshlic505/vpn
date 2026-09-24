package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionHistoryDao {
    @Query("SELECT * FROM connection_history ORDER BY startTime DESC")
    fun getAllHistory(): Flow<List<ConnectionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: ConnectionHistoryEntity): Long

    @Query("DELETE FROM connection_history")
    suspend fun clearAllHistory()

    @Query("SELECT COUNT(*) FROM connection_history")
    fun getHistoryCount(): Flow<Int>

    @Query("SELECT SUM(durationSeconds) FROM connection_history")
    fun getTotalDuration(): Flow<Long?>

    @Query("SELECT SUM(bytesDownloaded + bytesUploaded) FROM connection_history")
    fun getTotalBytesTransferred(): Flow<Long?>
}

@Dao
interface FavoriteServerDao {
    @Query("SELECT serverId FROM favorite_servers")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteServerEntity)

    @Query("DELETE FROM favorite_servers WHERE serverId = :serverId")
    suspend fun removeFavorite(serverId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_servers WHERE serverId = :serverId)")
    fun isFavorite(serverId: String): Flow<Boolean>
}

@Dao
interface PrivacySettingsDao {
    @Query("SELECT * FROM privacy_settings WHERE id = 1")
    fun getSettings(): Flow<PrivacySettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: PrivacySettingsEntity)
}
