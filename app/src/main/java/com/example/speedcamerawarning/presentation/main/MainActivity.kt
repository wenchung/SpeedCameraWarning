package com.example.speedcamerawarning.presentation.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.speedcamerawarning.databinding.ActivityMainBinding
import com.example.speedcamerawarning.service.LocationTrackingService
import com.example.speedcamerawarning.util.Constants
import com.example.speedcamerawarning.util.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 主要 Activity
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    private var isTracking = false
    
    // 權限請求啟動器
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            checkBackgroundLocationPermission()
        } else {
            showPermissionDeniedDialog()
        }
    }
    
    private val backgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            checkNotificationPermission()
        }
    }
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "通知權限被拒絕，可能無法接收警告", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupObservers()
        checkAndRequestPermissions()
        
        // 檢查資料庫是否為空，若為空則下載資料
        lifecycleScope.launch {
            if (viewModel.isDatabaseEmpty()) {
                showDownloadDataDialog()
            }
        }
    }
    
    private fun setupUI() {
        binding.apply {
            // 開始/停止監控按鈕
            btnToggleTracking.setOnClickListener {
                toggleTracking()
            }
            
            // 更新資料按鈕
            btnUpdateData.setOnClickListener {
                updateSpeedCameraData()
            }
            
            // 設定按鈕
            btnSettings.setOnClickListener {
                // TODO: 開啟設定頁面
                Toast.makeText(this@MainActivity, "設定功能開發中", Toast.LENGTH_SHORT).show()
            }
            
            // 地圖按鈕
            btnMap.setOnClickListener {
                // TODO: 開啟地圖頁面
                Toast.makeText(this@MainActivity, "地圖功能開發中", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupObservers() {
        // 觀察資料庫筆數
        lifecycleScope.launch {
            viewModel.cameraCount.collect { count ->
                binding.tvCameraCount.text = "已載入 $count 個測速照相點"
            }
        }
        
        // 觀察下載狀態
        lifecycleScope.launch {
            viewModel.downloadState.collect { state ->
                when (state) {
                    is DownloadState.Loading -> {
                        binding.btnUpdateData.isEnabled = false
                        binding.tvStatus.text = "正在下載資料..."
                    }
                    is DownloadState.Success -> {
                        binding.btnUpdateData.isEnabled = true
                        binding.tvStatus.text = "資料更新成功！共 ${state.count} 筆"
                        Toast.makeText(this@MainActivity, "下載成功", Toast.LENGTH_SHORT).show()
                    }
                    is DownloadState.Error -> {
                        binding.btnUpdateData.isEnabled = true
                        binding.tvStatus.text = "下載失敗：${state.message}"
                        Toast.makeText(this@MainActivity, "下載失敗", Toast.LENGTH_SHORT).show()
                    }
                    is DownloadState.Idle -> {
                        binding.btnUpdateData.isEnabled = true
                        binding.tvStatus.text = "就緒"
                    }
                }
            }
        }
    }
    
    /**
     * 檢查並請求權限
     */
    private fun checkAndRequestPermissions() {
        if (!PermissionHelper.hasLocationPermission(this)) {
            requestLocationPermission()
        } else if (!PermissionHelper.hasBackgroundLocationPermission(this)) {
            checkBackgroundLocationPermission()
        } else if (!PermissionHelper.hasNotificationPermission(this)) {
            checkNotificationPermission()
        }
    }
    
    private fun requestLocationPermission() {
        if (PermissionHelper.shouldShowLocationPermissionRationale(this)) {
            AlertDialog.Builder(this)
                .setTitle("需要定位權限")
                .setMessage("本應用需要定位權限來監控您與測速照相點的距離，並在適當時機發出警告。")
                .setPositiveButton("授權") { _, _ ->
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
                .setNegativeButton("取消", null)
                .show()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    private fun checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !PermissionHelper.hasBackgroundLocationPermission(this)) {
            AlertDialog.Builder(this)
                .setTitle("需要背景定位權限")
                .setMessage("為了在您開車時持續監控測速照相點，需要授予背景定位權限。請在下一個畫面選擇「一律允許」。")
                .setPositiveButton("授權") { _, _ ->
                    backgroundLocationPermissionLauncher.launch(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                }
                .setNegativeButton("稍後", null)
                .show()
        } else {
            checkNotificationPermission()
        }
    }
    
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !PermissionHelper.hasNotificationPermission(this)) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("權限被拒絕")
            .setMessage("沒有定位權限，應用程式無法正常運作。請到設定中手動開啟權限。")
            .setPositiveButton("確定", null)
            .show()
    }
    
    /**
     * 切換追蹤狀態
     */
    private fun toggleTracking() {
        if (!PermissionHelper.hasAllRequiredPermissions(this)) {
            Toast.makeText(this, "請先授予所有必要權限", Toast.LENGTH_SHORT).show()
            checkAndRequestPermissions()
            return
        }
        
        isTracking = !isTracking
        
        if (isTracking) {
            startTrackingService()
            binding.btnToggleTracking.text = "停止監控"
            binding.tvStatus.text = "監控中..."
        } else {
            stopTrackingService()
            binding.btnToggleTracking.text = "開始監控"
            binding.tvStatus.text = "已停止"
        }
    }
    
    /**
     * 啟動追蹤服務
     */
    private fun startTrackingService() {
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = Constants.ACTION_START_TRACKING
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    /**
     * 停止追蹤服務
     */
    private fun stopTrackingService() {
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = Constants.ACTION_STOP_TRACKING
        }
        startService(intent)
    }
    
    /**
     * 顯示下載資料對話框
     */
    private fun showDownloadDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要下載資料")
            .setMessage("首次使用需要下載測速照相點資料，約需1-2分鐘，是否現在下載？")
            .setPositiveButton("下載") { _, _ ->
                updateSpeedCameraData()
            }
            .setNegativeButton("稍後", null)
            .show()
    }
    
    /**
     * 更新測速照相資料
     */
    private fun updateSpeedCameraData() {
        lifecycleScope.launch {
            viewModel.downloadSpeedCameraData()
        }
    }
}
