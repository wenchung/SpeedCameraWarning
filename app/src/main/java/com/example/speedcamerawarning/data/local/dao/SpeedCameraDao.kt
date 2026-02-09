package com.example.speedcamerawarning.data.local.dao

import androidx.room.*
import com.example.speedcamerawarning.data.local.entity.SpeedCameraEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for SpeedCamera
 * 提供資料庫操作方法
 */
@Dao
interface SpeedCameraDao {
    
    /**
     * 插入單筆測速照相點資料
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(camera: SpeedCameraEntity): Long
    
    /**
     * 批次插入測速照相點資料
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cameras: List<SpeedCameraEntity>)
    
    /**
     * 更新測速照相點資料
     */
    @Update
    suspend fun update(camera: SpeedCameraEntity)
    
    /**
     * 刪除測速照相點資料
     */
    @Delete
    suspend fun delete(camera: SpeedCameraEntity)
    
    /**
     * 清空所有測速照相點資料
     */
    @Query("DELETE FROM speed_cameras")
    suspend fun deleteAll()
    
    /**
     * 根據ID查詢測速照相點
     */
    @Query("SELECT * FROM speed_cameras WHERE id = :id")
    suspend fun getById(id: Long): SpeedCameraEntity?
    
    /**
     * 取得所有測速照相點（Flow 即時更新）
     */
    @Query("SELECT * FROM speed_cameras ORDER BY cityName, regionName")
    fun getAllFlow(): Flow<List<SpeedCameraEntity>>
    
    /**
     * 取得所有測速照相點（一次性查詢）
     */
    @Query("SELECT * FROM speed_cameras ORDER BY cityName, regionName")
    suspend fun getAll(): List<SpeedCameraEntity>
    
    /**
     * 根據縣市查詢測速照相點
     */
    @Query("SELECT * FROM speed_cameras WHERE cityName = :cityName ORDER BY regionName")
    suspend fun getByCity(cityName: String): List<SpeedCameraEntity>
    
    /**
     * 根據速限查詢測速照相點
     */
    @Query("SELECT * FROM speed_cameras WHERE limit = :limit")
    suspend fun getBySpeedLimit(limit: Int): List<SpeedCameraEntity>
    
    /**
     * 查詢指定範圍內的測速照相點（使用經緯度邊界框）
     * 
     * @param minLat 最小緯度
     * @param maxLat 最大緯度
     * @param minLon 最小經度
     * @param maxLon 最大經度
     */
    @Query("""
        SELECT * FROM speed_cameras 
        WHERE latitude BETWEEN :minLat AND :maxLat 
        AND longitude BETWEEN :minLon AND :maxLon
        ORDER BY latitude, longitude
    """)
    suspend fun getInBounds(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<SpeedCameraEntity>
    
    /**
     * 查詢附近的測速照相點（使用經緯度粗略估算）
     * 
     * 注意：此方法使用簡單的經緯度範圍篩選，實際距離需在應用層用 Haversine 公式計算
     * 1度約等於111公里，這裡用0.01度（約1.1公里）作為初步篩選
     * 
     * @param latitude 使用者當前緯度
     * @param longitude 使用者當前經度
     * @param radiusDegrees 搜尋半徑（度數）預設0.02 約2.2公里
     */
    @Query("""
        SELECT * FROM speed_cameras 
        WHERE latitude BETWEEN :latitude - :radiusDegrees AND :latitude + :radiusDegrees
        AND longitude BETWEEN :longitude - :radiusDegrees AND :longitude + :radiusDegrees
    """)
    suspend fun getNearby(
        latitude: Double,
        longitude: Double,
        radiusDegrees: Double = 0.02
    ): List<SpeedCameraEntity>
    
    /**
     * 取得資料庫中的總筆數
     */
    @Query("SELECT COUNT(*) FROM speed_cameras")
    suspend fun getCount(): Int
    
    /**
     * 取得資料庫中的總筆數（Flow）
     */
    @Query("SELECT COUNT(*) FROM speed_cameras")
    fun getCountFlow(): Flow<Int>
    
    /**
     * 檢查資料庫是否為空
     */
    @Query("SELECT COUNT(*) FROM speed_cameras")
    suspend fun isEmpty(): Int
    
    /**
     * 取得各縣市的測速照相點數量統計
     */
    @Query("""
        SELECT cityName, COUNT(*) as count 
        FROM speed_cameras 
        GROUP BY cityName 
        ORDER BY count DESC
    """)
    suspend fun getCityStatistics(): List<CityStatistic>
    
    /**
     * 取得速限分布統計
     */
    @Query("""
        SELECT limit, COUNT(*) as count 
        FROM speed_cameras 
        GROUP BY limit 
        ORDER BY limit
    """)
    suspend fun getSpeedLimitStatistics(): List<SpeedLimitStatistic>
}

/**
 * 縣市統計資料
 */
data class CityStatistic(
    val cityName: String,
    val count: Int
)

/**
 * 速限統計資料
 */
data class SpeedLimitStatistic(
    val limit: Int,
    val count: Int
)
