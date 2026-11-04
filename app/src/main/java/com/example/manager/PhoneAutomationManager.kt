package com.example.manager

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import com.example.model.IntentAction
import com.example.model.PendingAction
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ContactResult(
    val displayName: String,
    val phoneNumber: String
)

class PhoneAutomationManager(
    private val context: Context,
    private val flashlightManager: FlashlightManager,
    private val appDiscoveryManager: AppDiscoveryManager
) {
    private val TAG = "PhoneAutomationManager"

    fun searchContact(nameQuery: String): ContactResult? {
        return try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$nameQuery%"),
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val name = if (nameIdx >= 0) it.getString(nameIdx) else nameQuery
                    val number = if (numIdx >= 0) it.getString(numIdx) else ""
                    return ContactResult(name, number)
                }
            }
            null
        } catch (e: SecurityException) {
            Log.w(TAG, "Contacts permission not granted", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error querying contacts", e)
            null
        }
    }

    fun preparePhoneCall(targetName: String, explicitPhone: String? = null): PendingAction {
        val contact = explicitPhone?.let { ContactResult(targetName, it) } ?: searchContact(targetName)
        val phoneNumber = contact?.phoneNumber ?: explicitPhone ?: ""
        val resolvedName = contact?.displayName ?: targetName

        val dialIntent = if (phoneNumber.isNotBlank()) {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Intent.ACTION_DIAL).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        return PendingAction(
            action = IntentAction.PHONE_CALL,
            title = "Confirm Communication Link",
            recipient = "$resolvedName ${if (phoneNumber.isNotBlank()) "($phoneNumber)" else ""}",
            content = "Place phone call to recipient via system dialer?",
            confirmButtonText = "OPEN DIALER",
            intentToLaunch = dialIntent
        )
    }

    fun prepareWhatsAppMessage(
        recipientName: String,
        messageBody: String,
        explicitPhone: String? = null
    ): PendingAction {
        val contact = explicitPhone?.let { ContactResult(recipientName, it) } ?: searchContact(recipientName)
        val phoneNumber = contact?.phoneNumber ?: explicitPhone ?: ""
        val resolvedName = contact?.displayName ?: recipientName

        val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "").removePrefix("+")
        val encodedMessage = try {
            URLEncoder.encode(messageBody, "UTF-8")
        } catch (e: Exception) {
            messageBody
        }

        val launchIntent = if (cleanPhone.isNotBlank()) {
            val waUri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
            Intent(Intent.ACTION_VIEW, waUri).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, messageBody)
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        return PendingAction(
            action = IntentAction.WHATSAPP_MESSAGE,
            title = "Confirm WhatsApp Message",
            recipient = "$resolvedName ${if (cleanPhone.isNotBlank()) "(+$cleanPhone)" else ""}",
            content = "\"$messageBody\"",
            confirmButtonText = "SEND VIA WHATSAPP",
            intentToLaunch = launchIntent
        )
    }

    fun createReminder(title: String, minutesFromNow: Int = 10): Intent {
        val seconds = (minutesFromNow * 60).coerceAtLeast(60)
        return Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, title)
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun openWebsite(url: String): Intent {
        val formatted = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        return Intent(Intent.ACTION_VIEW, Uri.parse(formatted)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun searchWeb(query: String): Intent {
        return Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun openDeviceSetting(settingType: String): Intent {
        val action = when (settingType.lowercase(Locale.ROOT)) {
            "wifi", "wi-fi", "internet" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "sound", "volume", "audio" -> Settings.ACTION_SOUND_SETTINGS
            "display", "brightness" -> Settings.ACTION_DISPLAY_SETTINGS
            "voice_assistant", "assistant" -> Settings.ACTION_VOICE_INPUT_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        return Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getFormattedTimeAndDate(languageCode: String): Pair<String, String> {
        val locale = try {
            Locale.forLanguageTag(languageCode)
        } catch (e: Exception) {
            Locale.getDefault()
        }
        val timeFormat = SimpleDateFormat("h:mm a", locale)
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", locale)
        val now = Date()
        return Pair(timeFormat.format(now), dateFormat.format(now))
    }
}
