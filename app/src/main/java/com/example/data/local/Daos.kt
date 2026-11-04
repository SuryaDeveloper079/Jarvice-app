package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(item: ConversationEntity): Long

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Long)

    @Query("DELETE FROM conversations")
    suspend fun clearAll()
}

@Dao
interface UserPreferenceDao {
    @Query("SELECT * FROM user_preferences WHERE `key` = :key LIMIT 1")
    fun getPreference(key: String): Flow<UserPreferenceEntity?>

    @Query("SELECT * FROM user_preferences WHERE `key` = :key LIMIT 1")
    suspend fun getPreferenceDirect(key: String): UserPreferenceEntity?

    @Query("SELECT * FROM user_preferences")
    fun getAllPreferences(): Flow<List<UserPreferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(item: UserPreferenceEntity)
}
