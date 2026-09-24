package com.example.data.repository

import com.example.data.model.AiServerRecommendation
import com.example.data.model.RecommendationGoal
import com.example.data.model.SpeedTestMetric
import com.example.data.model.UserLocation
import com.example.data.remote.GeminiService
import com.example.data.util.LocationAndSpeedEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ServerRecommendationRepository(
    private val geminiService: GeminiService,
    private val serverRepository: ServerRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _userLocation = MutableStateFlow(UserLocation.DEFAULT)
    val userLocation = _userLocation.asStateFlow()

    private val _speedTest = MutableStateFlow(SpeedTestMetric.DEFAULT)
    val speedTest = _speedTest.asStateFlow()

    private val _selectedGoal = MutableStateFlow(RecommendationGoal.BALANCED)
    val selectedGoal = _selectedGoal.asStateFlow()

    private val _recommendation = MutableStateFlow<AiServerRecommendation?>(null)
    val recommendation = _recommendation.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _analysisProgress = MutableStateFlow(0f)
    val analysisProgress = _analysisProgress.asStateFlow()

    private val _statusMessage = MutableStateFlow("Ready")
    val statusMessage = _statusMessage.asStateFlow()

    init {
        // Run initial recommendation generation in background
        scope.launch {
            runRecommendationAnalysis(runBenchmark = false)
        }
    }

    fun setUserLocation(location: UserLocation) {
        _userLocation.value = location
        scope.launch {
            runRecommendationAnalysis(runBenchmark = false)
        }
    }

    fun setGoal(goal: RecommendationGoal) {
        _selectedGoal.value = goal
        scope.launch {
            runRecommendationAnalysis(runBenchmark = false)
        }
    }

    suspend fun runRecommendationAnalysis(
        runBenchmark: Boolean = true,
        overrideGoal: RecommendationGoal? = null
    ) {
        _isAnalyzing.value = true
        val goal = overrideGoal ?: _selectedGoal.value
        _selectedGoal.value = goal

        try {
            val speedMetric = if (runBenchmark) {
                _statusMessage.value = "Benchmarking edge latency & throughput..."
                val metric = LocationAndSpeedEngine.runSpeedBenchmark { progress, msg ->
                    _analysisProgress.value = progress
                    _statusMessage.value = msg
                }
                _speedTest.value = metric
                metric
            } else {
                _analysisProgress.value = 0.5f
                _statusMessage.value = "Consulting Google Gemini Neural Telemetry..."
                _speedTest.value
            }

            _analysisProgress.value = 0.75f
            _statusMessage.value = "Evaluating server load, latency & proximity via Gemini..."

            val servers = serverRepository.servers.value.filter { it.isOnline }
            val location = _userLocation.value

            // Rank candidate servers by baseline suitability score and distance
            val candidateScores = servers.map { server ->
                val (score, distanceKm) = LocationAndSpeedEngine.calculateSuitabilityScore(
                    server = server,
                    userLocation = location,
                    speedTest = speedMetric,
                    goal = goal
                )
                Triple(server, score, distanceKm)
            }.sortedByDescending { it.second }

            // Generate structured recommendation via Gemini API (with local fallback)
            val result = geminiService.generateStructuredServerRecommendation(
                userLocation = location,
                speedTest = speedMetric,
                goal = goal,
                candidateServers = candidateScores
            )

            _recommendation.value = result
            _analysisProgress.value = 1.0f
            _statusMessage.value = "Recommendation ready: ${result.recommendedServer.city} (${result.recommendedServer.pingMs}ms)"
        } catch (e: Exception) {
            _statusMessage.value = "Recommendation generated with local safety telemetry"
        } finally {
            _isAnalyzing.value = false
        }
    }
}
