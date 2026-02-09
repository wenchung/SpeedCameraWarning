# 如何執行測速照相警示 App

## 前置需求

1. **Android Studio** (推薦最新穩定版)
   - 下載: https://developer.android.com/studio
   - 安裝 Android SDK 34 (API Level 34)

2. **測試裝置** (擇一):
   - 實體 Android 手機 (Android 7.0+ / API 24+)
   - Android 模擬器 (AVD)

## 步驟 1: 開啟專案

```bash
# 1. 複製專案到本地
cd /path/to/your/workspace

# 2. 用 Android Studio 開啟
File → Open → 選擇 SpeedCameraWarning 資料夾
```

## 步驟 2: Gradle 同步

Android Studio 會自動偵測並提示同步 Gradle：
- 點擊 "Sync Now" 
- 等待依賴下載完成（首次可能需要數分鐘）

如果沒有自動提示：
```
File → Sync Project with Gradle Files
```

## 步驟 3: 設定執行裝置

### 選項 A: 使用實體手機

1. **啟用開發者模式**:
   - 設定 → 關於手機 → 連續點擊「版本號碼」7次
   
2. **啟用 USB 偵錯**:
   - 設定 → 開發者選項 → USB 偵錯 (開啟)
   
3. **連接手機**:
   - USB 線連接電腦
   - 手機上允許 USB 偵錯授權

4. **驗證連接**:
   ```bash
   adb devices
   # 應該會顯示你的裝置
   ```

### 選項 B: 使用 Android 模擬器

1. **建立 AVD**:
   - Tools → Device Manager
   - Create Device
   - 選擇 Pixel 4 或更新機型
   - 選擇 API Level 34 (Android 14)
   - 完成建立

2. **啟動模擬器**:
   - 在 Device Manager 點擊播放按鈕

## 步驟 4: 執行 App

1. **選擇目標裝置**:
   - 工具列頂部下拉選單選擇你的裝置/模擬器

2. **執行**:
   - 點擊綠色播放按鈕 (Run)
   - 或按快捷鍵: `Shift + F10` (Windows/Linux) / `Ctrl + R` (Mac)

3. **首次安裝**:
   - Gradle 會編譯專案
   - APK 會自動安裝到裝置
   - App 會自動啟動

## 步驟 5: 授予權限

App 首次執行時需要授予權限：

1. **位置權限** (必須):
   - 選擇「使用期間允許」或「一律允許」
   - 建議選擇「一律允許」以便背景追蹤

2. **通知權限** (Android 13+):
   - 允許顯示通知

3. **前景服務權限** (Android 14+):
   - 允許前景服務執行

## 步驟 6: 測試功能

### 基本測試

1. **同步測速照相資料**:
   - 點擊「同步測速照相資料」按鈕
   - 等待資料下載完成（約 5-10 秒）
   - 查看「測速照相點數量」更新

2. **啟動監控服務**:
   - 點擊「開始監控」按鈕
   - 狀態應顯示「監控中」
   - 通知欄會顯示前景服務通知

3. **查看即時位置**:
   - 移動裝置或在模擬器中模擬位置
   - 觀察「當前速度」和「最近測速照相」更新

### 模擬器位置模擬

如果使用模擬器測試：

1. **開啟 Extended Controls**:
   - 模擬器側邊欄 → ... (更多) → Location

2. **設定 GPS 座標**:
   - 輸入台灣的座標，例如:
     - 台北 101: 25.0330, 121.5654
     - 台中: 24.1477, 120.6736
   - 點擊 "Send"

3. **模擬移動**:
   - 選擇 "Routes" 標籤
   - 載入 GPX 路徑檔案
   - 或手動點擊地圖建立路徑

## 故障排除

### Gradle 同步失敗

```bash
# 清除快取並重建
Build → Clean Project
Build → Rebuild Project
```

### 編譯錯誤

檢查 SDK 版本:
```
File → Project Structure → SDK Location
確認 Android SDK 路徑正確
```

### App 閃退

查看 Logcat:
```
View → Tool Windows → Logcat
篩選 "com.example.speedcamerawarning"
查看錯誤訊息
```

### 無法取得位置

1. 確認權限已授予
2. 檢查 GPS 是否開啟（實體裝置）
3. 模擬器: 確認已設定 GPS 座標

### 資料同步失敗

1. 檢查網路連線
2. 查看 Logcat 錯誤訊息
3. API 端點可能需要調整（參考 DataGovApi.kt）

## 開發工具

### 查看資料庫

```bash
# 匯出資料庫
adb pull /data/data/com.example.speedcamerawarning/databases/speed_camera_db.db

# 使用 SQLite Browser 開啟
# 下載: https://sqlitebrowser.org/
```

### 查看 Log

在 Android Studio Logcat 中篩選:
```
tag:SpeedCamera level:debug
```

### 效能監控

```
View → Tool Windows → Profiler
選擇你的 App 進程
監控 CPU、記憶體、網路使用
```

## 建置 APK

### Debug APK (測試用)

```bash
./gradlew assembleDebug
# 輸出: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (發布用)

1. **建立金鑰庫** (首次):
   ```bash
   keytool -genkey -v -keystore speedcamera.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias speedcamera
   ```

2. **設定簽署** (app/build.gradle.kts):
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               storeFile = file("speedcamera.jks")
               storePassword = "your_password"
               keyAlias = "speedcamera"
               keyPassword = "your_password"
           }
       }
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
           }
       }
   }
   ```

3. **建置**:
   ```bash
   ./gradlew assembleRelease
   # 輸出: app/build/outputs/apk/release/app-release.apk
   ```

## 下一步

- [ ] 實測不同道路速限場景
- [ ] 調整警示距離參數
- [ ] 新增地圖顯示功能（Google Maps）
- [ ] 優化電池使用效率
- [ ] 新增使用統計和歷史記錄

## 需要協助？

如遇到問題，請檢查:
1. Logcat 錯誤訊息
2. Gradle 建置輸出
3. 裝置系統版本是否符合最低需求 (API 24+)
