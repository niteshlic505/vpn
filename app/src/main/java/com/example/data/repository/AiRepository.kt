package com.example.data.repository

import com.example.data.model.AiChatMessage
import com.example.data.model.ChatSender
import com.example.data.model.VpnServer
import com.example.data.remote.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AiRepository(
    private val geminiService: GeminiService,
    private val serverRepository: ServerRepository
) {
    private val _messages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                id = "welcome",
                sender = ChatSender.ASSISTANT,
                message = "Hello! I am your **ShieldAI Privacy Sentinel** powered by Google Gemini.\n\nI can analyze your connection, suggest the fastest & safest servers, explain DNS/IP risks, and diagnose network leaks with High Thinking mode.",
                suggestedAction = "Recommend fastest server"
            )
        )
    )
    val messages = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    suspend fun sendMessage(userText: String, enableHighThinking: Boolean = true) {
        val userMsg = AiChatMessage(
            id = UUID.randomUUID().toString(),
            sender = ChatSender.USER,
            message = userText
        )
        _messages.value = _messages.value + userMsg
        _isGenerating.value = true

        try {
            // Check if user is asking for server recommendation
            val isServerQuery = userText.contains("recommend", ignoreCase = true) ||
                    userText.contains("fastest", ignoreCase = true) ||
                    userText.contains("best server", ignoreCase = true)

            // Select model according to system instructions:
            // gemini-3.1-pro-preview for complex tasks (with thinkingLevel = HIGH)
            // gemini-3.5-flash for general tasks
            // gemini-3.1-flash-lite for fast tasks
            val isComplex = userText.length > 50 ||
                    userText.contains("explain", ignoreCase = true) ||
                    userText.contains("risk", ignoreCase = true) ||
                    userText.contains("compare", ignoreCase = true) ||
                    userText.contains("inspect", ignoreCase = true) ||
                    userText.contains("diagnose", ignoreCase = true) ||
                    userText.contains("why", ignoreCase = true) ||
                    enableHighThinking

            val chosenModel = if (isComplex) {
                GeminiService.MODEL_PRO_COMPLEX
            } else if (isServerQuery) {
                GeminiService.MODEL_FLASH_GENERAL
            } else {
                GeminiService.MODEL_LITE_FAST
            }

            val currentServers = serverRepository.servers.value.take(6).joinToString("\n") { s ->
                "- ${s.city}, ${s.countryName}: Ping ${s.pingMs}ms, Load ${s.loadPercent}%, Protocol ${s.protocol.displayName}"
            }

            val systemInstruction = """
                You are ShieldAI, a world-class VPN security and cyber-privacy advisor.
                Provide clear, accurate, and reassuring security explanations.
                Never claim 100% anonymity, but explain defense-in-depth principles (encryption, DNS leak blocking, zero-logs, kill switch).
                Current verified nodes available:
                $currentServers
            """.trimIndent()

            val result = geminiService.generateContent(
                prompt = userText,
                model = chosenModel,
                systemInstruction = systemInstruction,
                enableHighThinking = isComplex
            )

            val assistantMsg = AiChatMessage(
                id = UUID.randomUUID().toString(),
                sender = ChatSender.ASSISTANT,
                message = result.text,
                thinkingProcess = result.thinking
            )
            _messages.value = _messages.value + assistantMsg
        } catch (e: Exception) {
            val errorMsg = AiChatMessage(
                id = UUID.randomUUID().toString(),
                sender = ChatSender.ASSISTANT,
                message = "ShieldAI telemetry encountered a temporary connection issue. Your VPN tunnel encryption remains fully active."
            )
            _messages.value = _messages.value + errorMsg
        } finally {
            _isGenerating.value = false
        }
    }

    suspend fun getAiServerRecommendation(usageGoal: String): String {
        val servers = serverRepository.servers.value.take(8).joinToString("\n") {
            "${it.city}, ${it.countryName} (${it.protocol.displayName}): ${it.pingMs}ms, ${it.loadPercent}% load"
        }
        val prompt = "User goal: $usageGoal.\nAnalyze these servers and recommend the top choice in 2 concise sentences:\n$servers"

        val result = geminiService.generateContent(
            prompt = prompt,
            model = GeminiService.MODEL_FLASH_GENERAL
        )
        return result.text
    }

    suspend fun analyzeConnectionIssue(ping: Int, load: Int, errorMsg: String?): String {
        val prompt = "Diagnose VPN connection telemetry: Ping = ${ping}ms, Server load = $load%, Error reported: ${errorMsg ?: "High jitter"}. Suggest 2 troubleshooting actions in simple bullet points."
        val result = geminiService.generateContent(
            prompt = prompt,
            model = GeminiService.MODEL_PRO_COMPLEX,
            enableHighThinking = true
        )
        return result.text
    }
}
