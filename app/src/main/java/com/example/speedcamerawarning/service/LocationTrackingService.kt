package com.example.speedcamerawarning.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import androidx.core.app.ServiceCompat
import com.example.speedcamerawarning.data.repository.SpeedCameraRepository
import com.example.speedcamerawarning.domain.model.SpeedCamera
import com.example.speedcamerawarning.util.Constants
import com.example.speedcamerawarning.util.DistanceCalculator
import com.example.speedcamerawarning.util.DistanceLevel
import com.example.speedcamerawarning.util.LocationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import java.util.*
import javax.inject.Inject

/**
 * 前景服務 - 持續監控位置並檢測測速照相點
 */
@AndroidEntryPoint
class LocationTrackingService : Service(), OnInitListener {
    
    @Inject
    lateinit var repository: SpeedCameraRepository
    
    @Inject
    lateinit var locationHelper: LocationHelper
    
    private lateinit var notificationHelper: NotificationHelper
    private var textToSpeech: TextToSpeech? = null
    private var isTextToSpeechReady = false
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var locationJob: Job? = null
    
    // 記錄已警告過的測速照相點（避免重複警告）
    private val warnedCameras = mutableSetOf<Long>()
    private var lastLocation: Location? = null
    private var nearestCamera: SpeedCamera? = null
    
    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        textToSpeech = TextToSpeech(this, this)
        
        // 啟動前景服務
        startForeground(
            Constants.FOREGROUND_SERVICE_NOTIFICATION_ID,
            notificationHelper.createForegroundNotification()
        )
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_TRACKING -> startTracking()
            Constants.ACTION_STOP_TRACKING -> stopTracking()
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
        textToSpeech?.shutdown()
        serviceScope.cancel()
    }
    
    /**
     * TextToSpeech 初始化回調
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.TRADITIONAL_CHINESE
            isTextToSpeechReady = true
        }
    }
    
    /**
     * 開始追蹤位置
     */
    private fun startTracking() {
        if (locationJob?.isActive == true) return
        
        locationJob = serviceScope.launch {
            // 建立定位請求
            val locationRequest = locationHelper.createBalancedLocationRequest()
            
            // 監聽位置更新
            locationHelper.getLocationUpdates(locationRequest)
                .catch { e ->
                    // 處理錯誤
                    e.printStackTrace()
                }
                .collect { location ->
                    handleLocationUpdate(location)
                }
        }
    }
    
    /**
     * 停止追蹤位置
     */
    private fun stopTracking() {
        locationJob?.cancel()
        locationJob = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    /**
     * 處理位置更新
     */
    private suspend fun handleLocationUpdate(location: Location) {
        lastLocation = location
        
        // 查詢附近的測速照相點
        val nearbyCameras = repository.getNearbyCameras(
            location.latitude,
            location.longitude,
            Constants.SEARCH_RADIUS_KM
        )
        
        if (nearbyCameras.isEmpty()) {
            nearestCamera = null
            warnedCameras.clear()
            updateForegroundNotification(null, null)
            return
        }
        
        // 計算每個測速照相點的距離
        val camerasWithDistance = nearbyCameras.map { camera ->
            val distance = DistanceCalculator.calculateDistance(
                location.latitude,
                location.longitude,
                camera.latitude,
                camera.longitude
            )
            camera to distance
        }
        
        // 找出最近的測速照相點
        val (closestCamera, closestDistance) = camerasWithDistance.minByOrNull { it.second }
            ?: return
        
        nearestCamera = closestCamera
        
        // 更新前景服務通知
        updateForegroundNotification(
            DistanceCalculator.formatDistance(closestDistance),
            closestCamera.address
        )
        
        // 檢查是否需要警告
        checkAndWarnIfNeeded(closestCamera, closestDistance)
        
        // 清理已遠離的測速照相點記錄
        cleanupWarnedCameras(camerasWithDistance)
    }
    
    /**
     * 檢查並發出警告
     */
    private fun checkAndWarnIfNeeded(camera: SpeedCamera, distance: Double) {
        // 如果已經警告過這個測速照相點，則不再警告
        if (warnedCameras.contains(camera.id)) {
            return
        }
        
        val distanceLevel = DistanceCalculator.getDistanceLevel(distance)
        
        when (distanceLevel) {
            DistanceLevel.CRITICAL -> {
                showWarning(camera, distance, WarningLevel.CRITICAL)
                speakWarning(camera, distance)
                warnedCameras.add(camera.id)
            }
            DistanceLevel.WARNING -> {
                showWarning(camera, distance, WarningLevel.WARNING)
                speakWarning(camera, distance)
                warnedCameras.add(camera.id)
            }
            DistanceLevel.NOTICE -> {
                showWarning(camera, distance, WarningLevel.NOTICE)
                warnedCameras.add(camera.id)
            }
            DistanceLevel.SAFE -> {
                // 安全距離，不警告
            }
        }
    }
    
    /**
     * 顯示警告通知
     */
    private fun showWarning(camera: SpeedCamera, distance: Double, level: WarningLevel) {
        notificationHelper.showWarningNotification(
            camera.address,
            DistanceCalculator.formatDistance(distance),
            camera.limit,
            level
        )
    }
    
    /**
     * 語音播報警告
     */
    private fun speakWarning(camera: SpeedCamera, distance: Double) {
        if (!isTextToSpeechReady) return
        
        val distanceText = if (distance < 1000) {
            "${distance.toInt()}公尺"
        } else {
            String.format("%.1f公里", distance / 1000)
        }
        
        val message = "前方 $distanceText 有測速照相，速限 ${camera.limit} 公里"
        
        textToSpeech?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "warning_${camera.id}"
        )
    }
    
    /**
     * 更新前景服務通知
     */
    private fun updateForegroundNotification(distance: String?, address: String?) {
        notificationHelper.updateForegroundNotification(distance, address)
    }
    
    /**
     * 清理已遠離的測速照相點記錄
     */
    private fun cleanupWarnedCameras(camerasWithDistance: List<Pair<SpeedCamera, Double>>) {
        // 移除距離超過 1.5km 的已警告測速照相點
        val farCameraIds = camerasWithDistance
            .filter { it.second > 1500 }
            .map { it.first.id }
        
        warnedCameras.removeAll(farCameraIds.toSet())
    }
}
