package com.example

import android.app.Application
import com.example.data.local.ShieldDatabase
import com.example.data.remote.GeminiService
import com.example.data.repository.AiRepository
import com.example.data.repository.ServerRepository
import com.example.data.repository.VpnRepository

class ShieldApplication : Application() {

    lateinit var database: ShieldDatabase
        private set

    lateinit var serverRepository: ServerRepository
        private set

    lateinit var vpnRepository: VpnRepository
        private set

    lateinit var aiRepository: AiRepository
        private set

    lateinit var recommendationRepository: com.example.data.repository.ServerRecommendationRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = ShieldDatabase.getDatabase(this)
        serverRepository = ServerRepository(database.favoriteServerDao())
        vpnRepository = VpnRepository(this, database.connectionHistoryDao(), database.privacySettingsDao())
        val geminiService = GeminiService()
        aiRepository = AiRepository(geminiService, serverRepository)
        recommendationRepository = com.example.data.repository.ServerRecommendationRepository(geminiService, serverRepository)
    }

    companion object {
        lateinit var instance: ShieldApplication
            private set
    }
}
