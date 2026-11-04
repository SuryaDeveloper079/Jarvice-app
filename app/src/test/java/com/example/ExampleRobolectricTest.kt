package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ConversationEntity
import com.example.data.local.JarvisDatabase
import com.example.manager.AppDiscoveryManager
import com.example.manager.FlashlightManager
import com.example.manager.PhoneAutomationManager
import com.example.model.IntentAction
import com.example.model.JarvisLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("JARVIS AI", appName)
    }

    @Test
    fun `test 15 supported languages are configured`() {
        val languages = JarvisLanguage.SUPPORTED_LANGUAGES
        val codes = languages.map { it.code }

        assertTrue(codes.contains("ta")) // Tamil
        assertTrue(codes.contains("hi")) // Hindi
        assertTrue(codes.contains("te")) // Telugu
        assertTrue(codes.contains("kn")) // Kannada
        assertTrue(codes.contains("ml")) // Malayalam
        assertTrue(codes.contains("bn")) // Bengali
        assertTrue(codes.contains("mr")) // Marathi
        assertTrue(codes.contains("gu")) // Gujarati
        assertTrue(codes.contains("pa")) // Punjabi
        assertTrue(codes.contains("ur")) // Urdu
        assertTrue(codes.contains("ar")) // Arabic
        assertTrue(codes.contains("fr")) // French
        assertTrue(codes.contains("es")) // Spanish
        assertTrue(codes.contains("ja")) // Japanese
        assertTrue(codes.contains("en")) // English
    }

    @Test
    fun `test phone automation manager prepares whatsapp message with confirmation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val flashlightManager = FlashlightManager(context)
        val appDiscoveryManager = AppDiscoveryManager(context)
        val automationManager = PhoneAutomationManager(context, flashlightManager, appDiscoveryManager)

        val pending = automationManager.prepareWhatsAppMessage(
            recipientName = "Rahul",
            messageBody = "I will arrive tomorrow",
            explicitPhone = "9876543210"
        )

        assertEquals(IntentAction.WHATSAPP_MESSAGE, pending.action)
        assertTrue(pending.recipient?.contains("Rahul") == true)
        assertTrue(pending.content?.contains("I will arrive tomorrow") == true)
        assertNotNull(pending.intentToLaunch)
    }

    @Test
    fun `test phone automation manager prepares phone call`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val flashlightManager = FlashlightManager(context)
        val appDiscoveryManager = AppDiscoveryManager(context)
        val automationManager = PhoneAutomationManager(context, flashlightManager, appDiscoveryManager)

        val pending = automationManager.preparePhoneCall(
            targetName = "Rahul",
            explicitPhone = "9876543210"
        )

        assertEquals(IntentAction.PHONE_CALL, pending.action)
        assertNotNull(pending.intentToLaunch)
    }

    @Test
    fun `test room database persists and retrieves conversation turns`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = JarvisDatabase.getDatabase(context)
        val dao = db.conversationDao()

        val entity = ConversationEntity(
            userQuery = "Open WhatsApp and send Rahul a message",
            aiResponse = "I have prepared the WhatsApp message for Rahul.",
            intentType = "WHATSAPP_MESSAGE",
            languageCode = "en"
        )

        dao.insertConversation(entity)
        val items = dao.getAllConversations().first()
        assertTrue(items.isNotEmpty())
        assertEquals("Open WhatsApp and send Rahul a message", items.first().userQuery)
    }
}
