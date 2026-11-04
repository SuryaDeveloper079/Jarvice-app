package com.example.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

class JarvisAiAssistant {
    private val TAG = "JarvisAiAssistant"

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), the iconic, highly sophisticated, polite, and witty AI created by Tony Stark.
                    Guidelines:
                    - Always address the user respectfully as 'Sir' (or provide a polite greeting).
                    - Speak with calm, British-inflected elegance, subtle dry humor, and absolute competence.
                    - Keep your responses concise (1 to 3 short sentences maximum) because your answers are spoken aloud through Android Text-To-Speech.
                    - Answer general knowledge questions, explain concepts simply, tell witty tech jokes, and assist intelligently.
                    - If asked about system status or Iron Man lore, respond in character as Stark Industries' chief digital butler.
                    - Do not use markdown bullet lists, asterisks, or formatting symbols that would sound unnatural when read by TTS.
                    """.trimIndent()
                )
            },
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 250
            }
        )
    }

    suspend fun generateResponse(userPrompt: String, userTitle: String = "Sir"): String {
        return try {
            withTimeout(45_000L) {
                val enhancedPrompt = "User ($userTitle) asks: $userPrompt"
                val response = model.generateContent(enhancedPrompt)
                val reply = response.text?.trim()
                if (!reply.isNullOrBlank()) {
                    reply
                } else {
                    "My cognitive subroutines processed your query, $userTitle, but yielded no data."
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI inference timed out", e)
            "Apologies $userTitle, neural network latency timed out while analyzing that inquiry."
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI inference failed", e)
            "I seem to be encountering interference with my neural relays, $userTitle. ${e.localizedMessage ?: "Unable to complete request."}"
        }
    }
}
