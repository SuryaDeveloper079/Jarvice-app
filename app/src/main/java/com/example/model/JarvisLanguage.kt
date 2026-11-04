package com.example.model

import java.util.Locale

data class JarvisLanguage(
    val code: String,
    val name: String,
    val nativeName: String,
    val locale: Locale
) {
    companion object {
        val AUTO = JarvisLanguage("auto", "Auto Detect", "Automatic Language Detection", Locale.getDefault())

        val SUPPORTED_LANGUAGES = listOf(
            AUTO,
            JarvisLanguage("en", "English", "English (Global / US)", Locale("en", "US")),
            JarvisLanguage("ta", "Tamil", "தமிழ் (Tamil)", Locale("ta", "IN")),
            JarvisLanguage("hi", "Hindi", "हिन्दी (Hindi / Hinglish)", Locale("hi", "IN")),
            JarvisLanguage("te", "Telugu", "తెలుగు (Telugu)", Locale("te", "IN")),
            JarvisLanguage("kn", "Kannada", "ಕನ್ನಡ (Kannada)", Locale("kn", "IN")),
            JarvisLanguage("ml", "Malayalam", "മലയാളം (Malayalam)", Locale("ml", "IN")),
            JarvisLanguage("bn", "Bengali", "বাংলা (Bengali)", Locale("bn", "IN")),
            JarvisLanguage("mr", "Marathi", "मराठी (Marathi)", Locale("mr", "IN")),
            JarvisLanguage("gu", "Gujarati", "ગુજરાતી (Gujarati)", Locale("gu", "IN")),
            JarvisLanguage("pa", "Punjabi", "ਪੰਜਾਬੀ (Punjabi)", Locale("pa", "IN")),
            JarvisLanguage("ur", "Urdu", "اردو (Urdu)", Locale("ur", "PK")),
            JarvisLanguage("ar", "Arabic", "العربية (Arabic)", Locale("ar", "SA")),
            JarvisLanguage("fr", "French", "Français (French)", Locale("fr", "FR")),
            JarvisLanguage("es", "Spanish", "Español (Spanish)", Locale("es", "ES")),
            JarvisLanguage("ja", "Japanese", "日本語 (Japanese)", Locale("ja", "JP"))
        )

        fun findByCode(code: String): JarvisLanguage {
            return SUPPORTED_LANGUAGES.find { it.code.equals(code, ignoreCase = true) } ?: AUTO
        }
    }
}
