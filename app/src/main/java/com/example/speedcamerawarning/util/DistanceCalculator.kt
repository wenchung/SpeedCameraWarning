package com.example.speedcamerawarning.util

import android.location.Location
import kotlin.math.*

/**
 * 距離計算工具
 * 
 * 使用 Haversine 公式計算地球表面兩點之間的距離
 */
object DistanceCalculator {
    
    private const val EARTH_RADIUS_METERS = 6371000.0 // 地球半徑（公尺）
    
    /**
     * 計算兩個經緯度座標之間的距離（Haversine 公式）
     * 
     * @param lat1 起點緯度
     * @param lon1 起點經度
     * @param lat2 終點緯度
     * @param lon2 終點經度
     * @return 距離（公尺）
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val φ1 = Math.toRadians(lat1)
        val φ2 = Math.toRadians(lat2)
        val Δφ = Math.toRadians(lat2 - lat1)
        val Δλ = Math.toRadians(lon2 - lon1)

        val a = sin(Δφ / 2).pow(2) +
                cos(φ1) * cos(φ2) *
                sin(Δλ / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }
    
    /**
     * 使用 Android Location 物件計算距離
     */
    fun calculateDistance(location1: Location, location2: Location): Float {
        return location1.distanceTo(location2)
    }
    
    /**
     * 格式化距離顯示
     * 
     * @param distanceMeters 距離（公尺）
     * @return 格式化後的字串（例如："500m" 或 "1.2km"）
     */
    fun formatDistance(distanceMeters: Double): String {
        return when {
            distanceMeters < 1000 -> "${distanceMeters.toInt()}m"
            else -> String.format("%.1fkm", distanceMeters / 1000)
        }
    }
    
    /**
     * 判斷距離等級（用於警告分級）
     */
    fun getDistanceLevel(distanceMeters: Double): DistanceLevel {
        return when {
            distanceMeters <= 300 -> DistanceLevel.CRITICAL
            distanceMeters <= 500 -> DistanceLevel.WARNING
            distanceMeters <= 1000 -> DistanceLevel.NOTICE
            else -> DistanceLevel.SAFE
        }
    }
    
    /**
     * 計算方位角（從北方順時針旋轉的角度）
     * 
     * @return 角度 (0-360)
     */
    fun calculateBearing(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val φ1 = Math.toRadians(lat1)
        val φ2 = Math.toRadians(lat2)
        val Δλ = Math.toRadians(lon2 - lon1)

        val y = sin(Δλ) * cos(φ2)
        val x = cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(Δλ)
        
        val θ = atan2(y, x)
        return (Math.toDegrees(θ) + 360) % 360
    }
    
    /**
     * 將方位角轉換為方向文字
     */
    fun bearingToDirection(bearing: Double): String {
        return when {
            bearing < 22.5 || bearing >= 337.5 -> "北方"
            bearing < 67.5 -> "東北方"
            bearing < 112.5 -> "東方"
            bearing < 157.5 -> "東南方"
            bearing < 202.5 -> "南方"
            bearing < 247.5 -> "西南方"
            bearing < 292.5 -> "西方"
            bearing < 337.5 -> "西北方"
            else -> "未知"
        }
    }
}

/**
 * 距離等級（用於警告分級）
 */
enum class DistanceLevel {
    CRITICAL,  // < 300m 緊急
    WARNING,   // 300-500m 警告
    NOTICE,    // 500-1000m 提示
    SAFE       // > 1000m 安全
}
