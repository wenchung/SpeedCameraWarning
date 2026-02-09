package com.example.speedcamerawarning.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Room Entity 代表測速照相點的資料庫表格
 * 
 * 資料來源：政府開放資料平台 - 測速執法設置點
 * 資料集ID: 7320
 */
@Entity(
    tableName = "speed_cameras",
    indices = [
        Index(value = ["latitude", "longitude"]),
        Index(value = ["cityName"]),
        Index(value = ["limit"])
    ]
)
data class SpeedCameraEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** 設置縣市 */
    val cityName: String,
    
    /** 設置市區鄉鎮 */
    val regionName: String,
    
    /** 設置地址 */
    val address: String,
    
    /** 管轄警局 */
    val deptNm: String,
    
    /** 管轄分局 */
    val branchNm: String,
    
    /** 經度 */
    val longitude: Double,
    
    /** 緯度 */
    val latitude: Double,
    
    /** 拍攝方向（例如：雙向、南向、北向等） */
    val direct: String,
    
    /** 速限 (km/h) */
    val limit: Int,
    
    /** 資料最後更新時間 */
    val updatedAt: Long = System.currentTimeMillis()
)
