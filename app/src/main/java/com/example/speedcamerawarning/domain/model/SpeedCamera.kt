package com.example.speedcamerawarning.domain.model

/**
 * Domain Model - 測速照相點
 * 
 * 此為應用層使用的資料模型，與資料庫 Entity 分離
 */
data class SpeedCamera(
    val id: Long,
    val cityName: String,
    val regionName: String,
    val address: String,
    val deptNm: String,
    val branchNm: String,
    val longitude: Double,
    val latitude: Double,
    val direct: String,
    val limit: Int
) {
    /**
     * 取得完整地址
     */
    fun getFullAddress(): String {
        return "$cityName$regionName$address"
    }
    
    /**
     * 取得簡短描述
     */
    fun getShortDescription(): String {
        return "$address (限速${limit}km/h)"
    }
}
