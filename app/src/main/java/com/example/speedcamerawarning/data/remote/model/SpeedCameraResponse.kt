package com.example.speedcamerawarning.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * 政府開放資料 API 回應的資料模型
 */
data class SpeedCameraResponse(
    @SerializedName("CityName")
    val cityName: String = "",
    
    @SerializedName("RegionName")
    val regionName: String = "",
    
    @SerializedName("Address")
    val address: String = "",
    
    @SerializedName("DeptNm")
    val deptNm: String = "",
    
    @SerializedName("BranchNm")
    val branchNm: String = "",
    
    @SerializedName("Longitude")
    val longitude: String = "",
    
    @SerializedName("Latitude")
    val latitude: String = "",
    
    @SerializedName("direct")
    val direct: String = "",
    
    @SerializedName("limit")
    val limit: String = ""
)
