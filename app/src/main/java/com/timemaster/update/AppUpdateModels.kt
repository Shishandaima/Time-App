package com.timemaster.update

data class ReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long
)

data class ReleaseInfo(
    val version: String,
    val publishedAt: String,
    val releaseNotes: String,
    val assets: List<ReleaseAsset>
) {
    val apkAsset: ReleaseAsset?
        get() = findApkAsset(assets)
}

data class DownloadProgress(
    val downloadedBytes: Long,
    val totalBytes: Long
) {
    val percent: Int =
        if (totalBytes <= 0L) 0 else ((downloadedBytes * 100L) / totalBytes).toInt().coerceIn(0, 100)
}

sealed interface UpdateCheckResult {
    data object Latest : UpdateCheckResult
    data class UpdateAvailable(val release: ReleaseInfo, val apkAsset: ReleaseAsset) : UpdateCheckResult
}

fun findApkAsset(assets: List<ReleaseAsset>): ReleaseAsset? =
    assets.firstOrNull { asset ->
        asset.name.endsWith(".apk", ignoreCase = true) ||
            asset.downloadUrl.substringBefore('?').endsWith(".apk", ignoreCase = true)
    }

fun isReleaseNewer(currentVersion: String, releaseVersion: String): Boolean =
    compareVersions(releaseVersion, currentVersion) > 0

private fun compareVersions(left: String, right: String): Int {
    val leftParts = left.versionParts()
    val rightParts = right.versionParts()
    val count = maxOf(leftParts.size, rightParts.size)
    for (index in 0 until count) {
        val leftPart = leftParts.getOrElse(index) { 0 }
        val rightPart = rightParts.getOrElse(index) { 0 }
        if (leftPart != rightPart) return leftPart.compareTo(rightPart)
    }
    return 0
}

private fun String.versionParts(): List<Int> =
    trim()
        .removePrefix("v")
        .removePrefix("V")
        .split('.', '-', '_')
        .mapNotNull { part -> part.takeWhile { it.isDigit() }.toIntOrNull() }
