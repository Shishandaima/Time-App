package com.timemaster.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {
    @Test
    fun detectsNewerReleaseVersionsWithOptionalVPrefix() {
        assertTrue(isReleaseNewer(currentVersion = "0.4.6", releaseVersion = "v0.4.7"))
        assertTrue(isReleaseNewer(currentVersion = "0.4.6", releaseVersion = "0.5.0"))
        assertFalse(isReleaseNewer(currentVersion = "0.4.6", releaseVersion = "0.4.6"))
        assertFalse(isReleaseNewer(currentVersion = "0.4.6", releaseVersion = "0.4.5"))
    }

    @Test
    fun selectsApkAssetIgnoringOtherReleaseFiles() {
        val asset = findApkAsset(
            listOf(
                ReleaseAsset(name = "notes.txt", downloadUrl = "https://example.test/notes.txt", sizeBytes = 10),
                ReleaseAsset(name = "time-master.apk", downloadUrl = "https://example.test/time-master.apk", sizeBytes = 20)
            )
        )

        assertTrue(asset?.name == "time-master.apk")
    }
}
