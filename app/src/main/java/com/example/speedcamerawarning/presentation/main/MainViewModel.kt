package com.example.speedcamerawarning.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speedcamerawarning.data.repository.SpeedCameraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主畫面 ViewModel
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: SpeedCameraRepository
) : ViewModel() {
    
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()
    
    val cameraCount = repository.getCameraCountFlow()
    
    /**
     * 檢查資料庫是否為空
     */
    suspend fun isDatabaseEmpty(): Boolean {
        return repository.isDatabaseEmpty()
    }
    
    /**
     * 下載測速照相資料
     */
    suspend fun downloadSpeedCameraData() {
        _downloadState.value = DownloadState.Loading
        
        viewModelScope.launch {
            val result = repository.downloadAndUpdateCameras()
            
            _downloadState.value = result.fold(
                onSuccess = { count ->
                    DownloadState.Success(count)
                },
                onFailure = { exception ->
                    DownloadState.Error(exception.message ?: "未知錯誤")
                }
            )
        }
    }
    
    /**
     * 插入測試資料（開發用）
     */
    fun insertTestData() {
        viewModelScope.launch {
            repository.insertTestData()
        }
    }
}

/**
 * 下載狀態
 */
sealed class DownloadState {
    object Idle : DownloadState()
    object Loading : DownloadState()
    data class Success(val count: Int) : DownloadState()
    data class Error(val message: String) : DownloadState()
}
