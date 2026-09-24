package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ConnectionHistoryEntity::class,
        FavoriteServerEntity::class,
        PrivacySettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ShieldDatabase : RoomDatabase() {

    abstract fun connectionHistoryDao(): ConnectionHistoryDao
    abstract fun favoriteServerDao(): FavoriteServerDao
    abstract fun privacySettingsDao(): PrivacySettingsDao

    companion object {
        @Volatile
        private var INSTANCE: ShieldDatabase? = null

        fun getDatabase(context: Context): ShieldDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShieldDatabase::class.java,
                    "shield_vpn_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
