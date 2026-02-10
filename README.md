# Android 測速照相警告 App

基於政府開放資料平台的測速照相地點資料，提供即時定位警告功能的 Android 應用程式。

## 資料來源

- **資料集**: 測速執法設置點 (政府資料開放平台)
- **資料集ID**: 7320
- **下載網址**: https://data.gov.tw/dataset/7320
- **API端點**: https://opdadm.moi.gov.tw/api/v1/no-auth/resource/api/dataset/EA5E6FCD-B82D-43B7-A5CF-E9893253187E/resource/264ACCFF-7C6A-4274-A543-1F226DEE5756/download

## 資料欄位說明

| 欄位名稱 | 說明 | 範例 |
|---------|------|------|
| CityName | 設置縣市 | 台北市 |
| RegionName | 設置市區鄉鎮 | 中正區 |
| Address | 設置地址 | 忠孝東路一段 |
| DeptNm | 管轄警局 | 臺北市政府警察局 |
| BranchNm | 管轄分局 | 中正第一分局 |
| Longitude | 經度 | 121.5198 |
| Latitude | 緯度 | 25.0478 |
| direct | 拍攝方向 | 雙向 |
| limit | 速限 | 50 |

## 功能特色

### 核心功能
- ✅ **即時定位監控**: 使用 GPS 持續追蹤使用者位置
- ✅ **智慧警告系統**: 根據距離測速照相點的遠近，提供分級警告
  - 1000m: 提前通知
  - 500m: 警告提示
  - 300m: 緊急警告
- ✅ **語音播報**: 自動語音提示「前方500公尺有測速照相，速限XX公里」
- ✅ **背景運作**: 使用 Foreground Service 確保 App 在背景持續運作
- ✅ **離線運作**: 資料儲存在本地 SQLite 資料庫，無需網路連線
- ✅ **地圖顯示**: 在地圖上標示附近的測速照相點位置
- ✅ **懸浮視窗速度顯示**: 類似「神盾測速照相」的浮動速度表
  - 即時顯示當前車速 (km/h)
  - 根據超速程度自動變色警示：
    - ⚪ **白色**: 正常行駛（未超速）
    - 🔵 **藍色**: 輕微超速（速限 +0~10 km/h）
    - 🟡 **黃色**: 中度超速（速限 +10~20 km/h）
    - 🔴 **紅色**: 嚴重超速（速限 +20 km/h 以上）
  - 可拖曳移動位置
  - 懸浮於所有 App 之上

### 進階功能
- 🔔 **通知系統**: 狀態列通知顯示最近的測速照相點
- 📊 **統計資訊**: 顯示已通過的測速照相點數量
- 🎨 **懸浮速度表**: 
  - 一鍵開啟/關閉速度顯示
  - 自動判斷速限並變色警示
  - 圓形設計，簡潔美觀
  - 長按顯示關閉按鈕
- ⚙️ **自訂設定**:
  - 調整警告距離 (300m ~ 1500m)
  - 開啟/關閉語音播報
  - 選擇警告音效
  - 設定最低觸發速度
  - 開啟/關閉懸浮視窗速度顯示

## 技術架構

### Architecture Pattern
- **MVVM (Model-View-ViewModel)**: 清晰的職責分離
- **Repository Pattern**: 統一的資料存取介面
- **Dependency Injection**: 使用 Hilt/Dagger 進行依賴注入

### 主要技術棧

#### Android Components
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Language**: Kotlin 1.9+

#### Libraries & Frameworks
```kotlin
// 資料庫
implementation "androidx.room:room-runtime:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"

// 定位服務
implementation "com.google.android.gms:play-services-location:21.1.0"
implementation "com.google.android.gms:play-services-maps:18.2.0"

// 協程 & Flow
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"

// ViewModel & LiveData
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.7.0"

// WorkManager (定期資料更新)
implementation "androidx.work:work-runtime-ktx:2.9.0"

// Dependency Injection
implementation "com.google.dagger:hilt-android:2.48"
kapt "com.google.dagger:hilt-compiler:2.48"

// Network (資料下載)
implementation "com.squareup.retrofit2:retrofit:2.9.0"
implementation "com.squareup.retrofit2:converter-gson:2.9.0"
implementation "com.squareup.okhttp3:logging-interceptor:4.12.0"

// UI
implementation "androidx.constraintlayout:constraintlayout:2.1.4"
implementation "com.google.android.material:material:1.11.0"
```

## 專案結構

```
app/src/main/java/com/example/speedcamerawarning/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   └── SpeedCameraDao.kt
│   │   ├── database/
│   │   │   └── AppDatabase.kt
│   │   └── entity/
│   │       └── SpeedCameraEntity.kt
│   ├── remote/
│   │   ├── api/
│   │   │   └── DataGovApi.kt
│   │   └── model/
│   │       └── SpeedCameraResponse.kt
│   └── repository/
│       └── SpeedCameraRepository.kt
├── domain/
│   ├── model/
│   │   └── SpeedCamera.kt
│   └── usecase/
│       ├── GetNearbySpeedCamerasUseCase.kt
│       └── CalculateDistanceUseCase.kt
├── presentation/
│   ├── main/
│   │   ├── MainActivity.kt
│   │   └── MainViewModel.kt
│   ├── map/
│   │   ├── MapFragment.kt
│   │   └── MapViewModel.kt
│   └── settings/
│       ├── SettingsFragment.kt
│       └── SettingsViewModel.kt
├── service/
│   ├── LocationTrackingService.kt
│   ├── SpeedOverlayService.kt
│   └── NotificationHelper.kt
├── util/
│   ├── DistanceCalculator.kt
│   ├── PermissionHelper.kt
│   └── Constants.kt
└── di/
    ├── AppModule.kt
    ├── DatabaseModule.kt
    └── NetworkModule.kt
```

## 權限需求

```xml
<!-- 必要權限 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

<!-- Android 10+ 背景定位 -->
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- Android 14+ 前景服務類型 -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
```

## 使用流程

### 首次啟動
1. App 啟動後自動下載最新測速照相資料
2. 資料儲存至本地 SQLite 資料庫
3. 請求必要權限（定位、通知、前景服務）

### 日常使用
1. 開啟 App 或點擊「開始監控」
2. 前景服務開始運作，狀態列顯示通知
3. App 持續監控位置，接近測速照相點時:
   - 發送通知
   - 語音播報 (可選)
   - 震動提醒 (可選)
4. 可切換到地圖頁面查看附近測速照相點

### 資料更新
- **自動更新**: 每週自動從政府開放平台下載最新資料
- **手動更新**: 設定頁面提供「立即更新」按鈕

## 距離計算演算法

使用 **Haversine公式** 計算兩個經緯度座標之間的距離：

```kotlin
fun calculateDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val R = 6371000.0 // 地球半徑（公尺）
    val φ1 = Math.toRadians(lat1)
    val φ2 = Math.toRadians(lat2)
    val Δφ = Math.toRadians(lat2 - lat1)
    val Δλ = Math.toRadians(lon2 - lon1)

    val a = sin(Δφ / 2).pow(2) +
            cos(φ1) * cos(φ2) *
            sin(Δλ / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c // 距離（公尺）
}
```

## 警告分級系統

| 距離 | 警告等級 | 動作 |
|------|---------|------|
| > 1000m | 無 | 無動作 |
| 500m - 1000m | 提前通知 | 顯示通知 |
| 300m - 500m | 警告 | 通知 + 語音播報 |
| < 300m | 緊急警告 | 通知 + 語音 + 震動 |

## 效能優化

### 電池優化
- 使用 `PRIORITY_BALANCED_POWER_ACCURACY` 定位模式
- 動態調整定位頻率：
  - 高速移動: 每2秒更新一次
  - 低速移動: 每5秒更新一次
  - 靜止: 每10秒更新一次
- 只查詢周圍1公里內的測速照相點

### 記憶體優化
- 使用 Room 資料庫分頁載入
- 只保留螢幕可見範圍的地圖標記
- 及時釋放不需要的資源

### 網路優化
- 資料壓縮傳輸
- 僅在 WiFi 環境下自動更新（可設定）
- 失敗重試機制

## 安裝與建置

### 前置需求
- Android Studio Hedgehog | 2023.1.1+
- JDK 17+
- Android SDK 34+
- Gradle 8.0+

### 建置步驟

```bash
# 1. Clone 專案
git clone https://github.com/yourusername/SpeedCameraWarning.git
cd SpeedCameraWarning

# 2. 開啟 Android Studio
# File -> Open -> 選擇專案資料夾

# 3. 同步 Gradle
# 等待 Gradle sync 完成

# 4. 連接裝置或啟動模擬器

# 5. 執行 App
# Run -> Run 'app'
```

### 建置 APK

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (需要簽署)
./gradlew assembleRelease
```

## 測試

```bash
# 執行單元測試
./gradlew test

# 執行 UI 測試
./gradlew connectedAndroidTest
```

## 貢獻指南

歡迎提交 Pull Request 或開 Issue 回報問題！

### 開發規範
- 遵循 Kotlin coding conventions
- 使用 MVVM 架構模式
- 新增功能需包含單元測試
- Commit message 使用中文或英文清楚描述

## 授權

本專案採用 MIT License

資料來源：政府資料開放平台 (CC BY 4.0)

## 聯絡資訊

- 開發者: Your Name
- Email: your.email@example.com
- GitHub: https://github.com/yourusername

## 更新日誌

### v1.0.0 (2024-02-09)
- 初始版本發布
- 基本測速照相警告功能
- 地圖顯示功能
- 語音播報功能
- 背景監控功能

## 已知問題

- [ ] 部分 Android 廠商的省電模式可能影響背景定位
- [ ] 首次下載資料需要較長時間
- [ ] 隧道內 GPS 訊號弱可能無法準確定位

## 未來規劃

- [ ] 支援區間測速警告
- [ ] 加入使用者回報功能
- [ ] 支援車速顯示
- [ ] 整合導航功能
- [ ] 支援 Android Auto
- [ ] 加入行車記錄功能
