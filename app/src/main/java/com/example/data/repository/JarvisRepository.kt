package com.example.data.repository

import com.example.data.local.ConversationDao
import com.example.data.local.ConversationEntity
import com.example.data.local.UserPreferenceDao
import com.example.data.local.UserPreferenceEntity
import kotlinx.coroutines.flow.Flow

class JarvisRepository(
    private val conversationDao: ConversationDao,
    private val preferenceDao: UserPreferenceDao
) {
    val conversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()

    suspend fun saveConversation(item: ConversationEntity): Long {
        return conversationDao.insertConversation(item)
    }

    suspend fun deleteConversation(id: Long) {
        conversationDao.deleteConversationById(id)
    }

    suspend fun clearConversations() {
        conversationDao.clearAll()
    }

    fun getPreference(key: String): Flow<UserPreferenceEntity?> {
        return preferenceDao.getPreference(key)
    }

    suspend fun getPreferenceDirect(key: String): String? {
        return preferenceDao.getPreferenceDirect(key)?.value
    }

    suspend fun setPreference(key: String, value: String) {
        preferenceDao.setPreference(UserPreferenceEntity(key = key, value = value))
    }
}
