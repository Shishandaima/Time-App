package com.timemaster.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class GitHubReleaseClient(
    private val latestReleaseUrl: String = LATEST_RELEASE_URL
) {
    suspend fun checkForUpdate(currentVersion: String): UpdateCheckResult =
        withContext(Dispatchers.IO) {
            val release = fetchLatestRelease()
            val apkAsset = release.apkAsset
                ?: error("\u6700\u65b0 Release \u4e2d\u672a\u627e\u5230 APK \u6587\u4ef6")
            if (isReleaseNewer(currentVersion, release.version)) {
                UpdateCheckResult.UpdateAvailable(release, apkAsset)
            } else {
                UpdateCheckResult.Latest
            }
        }

    suspend fun downloadApk(
        asset: ReleaseAsset,
        destination: File,
        onProgress: (DownloadProgress) -> Unit
    ): File = withContext(Dispatchers.IO) {
        destination.parentFile?.mkdirs()
        val connection = URL(asset.downloadUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("Accept", "application/octet-stream")
        try {
            connection.requireSuccess()
            val totalBytes = connection.contentLengthLong.takeIf { it > 0L } ?: asset.sizeBytes
            var downloadedBytes = 0L
            BufferedInputStream(connection.inputStream).use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        onProgress(DownloadProgress(downloadedBytes, totalBytes))
                    }
                }
            }
            destination
        } catch (error: Throwable) {
            destination.delete()
            throw error
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchLatestRelease(): ReleaseInfo {
        val connection = URL(latestReleaseUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        connection.setRequestProperty("User-Agent", "TimeMaster-Android")
        try {
            connection.requireSuccess()
            val json = connection.inputStream.bufferedReader().use { it.readText() }
            return parseRelease(JSONObject(json))
        } finally {
            connection.disconnect()
        }
    }

    private fun parseRelease(json: JSONObject): ReleaseInfo {
        val assets = json.optJSONArray("assets").orEmptyList { item ->
            ReleaseAsset(
                name = item.optString("name"),
                downloadUrl = item.optString("browser_download_url"),
                sizeBytes = item.optLong("size", 0L)
            )
        }
        return ReleaseInfo(
            version = json.optString("tag_name").ifBlank { json.optString("name") },
            publishedAt = json.optString("published_at"),
            releaseNotes = json.optString("body"),
            assets = assets
        )
    }

    private fun HttpURLConnection.requireSuccess() {
        if (responseCode !in 200..299) {
            val body = errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            error("GitHub \u8bf7\u6c42\u5931\u8d25\uff1aHTTP $responseCode $body")
        }
    }

    private fun JSONArray?.orEmptyList(mapper: (JSONObject) -> ReleaseAsset): List<ReleaseAsset> {
        if (this == null) return emptyList()
        return List(length()) { index -> mapper(getJSONObject(index)) }
    }

    private companion object {
        const val LATEST_RELEASE_URL =
            "https://api.github.com/repos/Shishandaima/Time-App/releases/latest"
    }
}
