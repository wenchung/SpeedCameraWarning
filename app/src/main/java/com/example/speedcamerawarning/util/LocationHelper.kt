package com.example.speedcamerawarning.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * GPS 定位輔助工具
 */
class LocationHelper(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    /**
     * 檢查是否有定位權限
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 檢查是否有背景定位權限 (Android 10+)
     */
    fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 10 以下不需要額外的背景定位權限
        }
    }
    
    /**
     * 取得最後一次已知位置
     */
    suspend fun getLastLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 建立定位請求設定（高精度模式）
     */
    fun createHighAccuracyLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L) // 每2秒更新
            .setMinUpdateIntervalMillis(1000L) // 最快1秒更新一次
            .setMaxUpdateDelayMillis(5000L)
            .build()
    }
    
    /**
     * 建立定位請求設定（平衡模式）
     */
    fun createBalancedLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5000L) // 每5秒更新
            .setMinUpdateIntervalMillis(3000L)
            .setMaxUpdateDelayMillis(10000L)
            .build()
    }
    
    /**
     * 建立定位請求設定（低耗電模式）
     */
    fun createLowPowerLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, 10000L) // 每10秒更新
            .setMinUpdateIntervalMillis(5000L)
            .setMaxUpdateDelayMillis(20000L)
            .build()
    }
    
    /**
     * 取得持續的位置更新（Flow）
     * 
     * @param locationRequest 定位請求設定
     */
    fun getLocationUpdates(locationRequest: LocationRequest): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("沒有定位權限"))
            return@callbackFlow
        }
        
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            context.mainLooper
        )
        
        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
    
    /**
     * 根據速度動態調整定位頻率
     * 
     * @param speedKmh 當前速度（km/h）
     */
    fun getAdaptiveLocationRequest(speedKmh: Float): LocationRequest {
        return when {
            speedKmh < 10 -> createLowPowerLocationRequest() // 靜止或慢速
            speedKmh < 40 -> createBalancedLocationRequest() // 市區速度
            else -> createHighAccuracyLocationRequest() // 高速
        }
    }
}
