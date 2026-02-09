package com.example.speedcamerawarning.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.speedcamerawarning.data.local.dao.SpeedCameraDao
import com.example.speedcamerawarning.data.local.entity.SpeedCameraEntity

/**
 * Room Database 主要資料庫類別
 */
@Database(
    entities = [SpeedCameraEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun speedCameraDao(): SpeedCameraDao
    
    companion object {
        private const val DATABASE_NAME = "speed_camera_db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // 資料庫版本升級時清空資料重建
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
