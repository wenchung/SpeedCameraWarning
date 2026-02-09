package com.example.speedcamerawarning.data.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Streaming

/**
 * 政府開放資料平台 API 介面
 */
interface DataGovApi {
    
    /**
     * 下載測速照相點 CSV 資料
     * 
     * 資料集: 測速執法設置點
     * 資料集ID: 7320
     * 格式: CSV
     */
    @Streaming
    @GET("api/v1/no-auth/resource/api/dataset/EA5E6FCD-B82D-43B7-A5CF-E9893253187E/resource/264ACCFF-7C6A-4274-A543-1F226DEE5756/download")
    suspend fun downloadSpeedCamerasCsv(): Response<ResponseBody>
    
    companion object {
        const val BASE_URL = "https://opdadm.moi.gov.tw/"
    }
}
