package com.aos.core.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.aos.core.common.result.Result
import com.aos.core.data.preferences.UserPreferencesDataStore
import com.aos.core.domain.model.AppUpdateInfo
import com.aos.core.domain.model.UpdateStatus
import com.aos.core.domain.repository.AppUpdateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesDataStore: UserPreferencesDataStore
) : AppUpdateRepository {

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    override val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    override val isAutoUpdateCheckEnabled: Flow<Boolean> = preferencesDataStore.isAutoUpdateCheckEnabled

    override suspend fun setAutoUpdateCheckEnabled(enabled: Boolean) {
        preferencesDataStore.setAutoUpdateCheckEnabled(enabled)
    }

    override fun resetUpdateStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }

    private fun getCurrentVersionName(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.4.0"
        } catch (_: Exception) {
            "1.4.0"
        }
    }

    override suspend fun checkForUpdates(): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        _updateStatus.value = UpdateStatus.Checking
        try {
            val currentVersion = getCurrentVersionName()
            val apiUrl = "https://api.github.com/repos/aturkk/aos-launcher/releases/latest"
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "AOS-Launcher-Updater")
                connectTimeout = 12000
                readTimeout = 12000
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorMsg = "GitHub API yanıt vermedi (HTTP $responseCode)"
                _updateStatus.value = UpdateStatus.Error(errorMsg)
                return@withContext Result.Error(message = errorMsg)
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            val tagName = json.optString("tag_name", "").trim()
            val releaseNotes = json.optString("body", "Yeni özellikler ve hata düzeltmeleri.")
            val publishedAt = json.optString("published_at", "")

            var apkUrl = ""
            var apkSize = 0L

            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url", "")
                        apkSize = asset.optLong("size", 0L)
                        break
                    }
                }
            }

            val isUpdateAvailable = isNewerVersion(tagName, currentVersion) && apkUrl.isNotBlank()

            val updateInfo = AppUpdateInfo(
                currentVersion = currentVersion,
                latestVersion = tagName,
                isUpdateAvailable = isUpdateAvailable,
                releaseNotes = releaseNotes,
                apkDownloadUrl = apkUrl,
                apkSize = apkSize,
                publishedAt = publishedAt
            )

            if (isUpdateAvailable) {
                _updateStatus.value = UpdateStatus.UpdateAvailable(updateInfo)
            } else {
                _updateStatus.value = UpdateStatus.UpToDate(currentVersion)
            }

            Result.Success(updateInfo)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Bağlantı hatası oluştu"
            _updateStatus.value = UpdateStatus.Error(msg)
            Result.Error(exception = e, message = msg)
        }
    }

    override suspend fun downloadAndInstallUpdate(updateInfo: AppUpdateInfo): Unit = withContext(Dispatchers.IO) {
        if (updateInfo.apkDownloadUrl.isBlank()) {
            _updateStatus.value = UpdateStatus.Error("İndirme bağlantısı bulunamadı.")
            return@withContext
        }

        try {
            _updateStatus.value = UpdateStatus.Downloading(
                progressPercent = 0,
                downloadedBytes = 0L,
                totalBytes = updateInfo.apkSize
            )

            val updatesDir = File(context.getExternalFilesDir(null), "updates").apply { mkdirs() }
            val cleanVersion = updateInfo.latestVersion.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val targetFile = File(updatesDir, "aos-launcher-$cleanVersion.apk")

            val url = URL(updateInfo.apkDownloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "AOS-Launcher-Updater")
                instanceFollowRedirects = true
                connectTimeout = 15000
                readTimeout = 30000
            }

            var actualConnection = connection
            var redirectCount = 0
            while (actualConnection.responseCode in listOf(HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_MOVED_TEMP, 307, 308) && redirectCount < 5) {
                val newUrl = actualConnection.getHeaderField("Location")
                actualConnection.disconnect()
                actualConnection = (URL(newUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "AOS-Launcher-Updater")
                    connectTimeout = 15000
                    readTimeout = 30000
                }
                redirectCount++
            }

            val totalLength = if (actualConnection.contentLengthLong > 0) actualConnection.contentLengthLong else updateInfo.apkSize
            var downloadedBytes = 0L

            actualConnection.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var lastUpdatePercent = -1

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        val percent = if (totalLength > 0) ((downloadedBytes * 100) / totalLength).toInt() else 0
                        if (percent != lastUpdatePercent) {
                            lastUpdatePercent = percent
                            _updateStatus.value = UpdateStatus.Downloading(
                                progressPercent = percent,
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalLength
                            )
                        }
                    }
                    output.flush()
                }
            }

            _updateStatus.value = UpdateStatus.ReadyToInstall(targetFile, updateInfo)
            installApk(targetFile)
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "İndirme sırasında hata oluştu"
            _updateStatus.value = UpdateStatus.Error(msg)
        }
    }

    override fun installApk(apkFile: File) {
        try {
            if (!apkFile.exists()) return

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, apkFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _updateStatus.value = UpdateStatus.Error("Kurulum başlatılamadı: ${e.localizedMessage}")
        }
    }

    private fun isNewerVersion(latestTag: String, current: String): Boolean {
        val cleanLatest = latestTag.trim().removePrefix("v").removePrefix("V")
        val cleanCurrent = current.trim().removePrefix("v").removePrefix("V")

        val latestParts = cleanLatest.split("-")[0].split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = cleanCurrent.split("-")[0].split(".").mapNotNull { it.toIntOrNull() }

        val maxLength = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLength) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
