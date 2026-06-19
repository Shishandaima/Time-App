# Time Management Master Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the Android MVP for "时间管理大师": local recurring reminders with large accessible UI, exact alarm scheduling, strong/normal alert modes, and bundled local ringtones.

**Architecture:** A single-module native Android app in Kotlin and Jetpack Compose. Keep pure reminder rule calculation independent from Android APIs, store reminder state in Room, and bridge Android alarm/notification/audio APIs through small service classes.

**Tech Stack:** Kotlin, Android Gradle Plugin, Jetpack Compose, Room, Kotlin coroutines/Flow, AlarmManager, BroadcastReceiver, NotificationCompat, MediaPlayer, JUnit.

---

## File Structure

- `settings.gradle.kts`: Gradle project declaration.
- `build.gradle.kts`: Root plugin versions.
- `gradle.properties`: AndroidX and Kotlin build settings.
- `app/build.gradle.kts`: Android app module, dependencies, test config.
- `app/src/main/AndroidManifest.xml`: Permissions, activities, receivers.
- `app/src/main/java/com/timemaster/MainActivity.kt`: Compose entry point.
- `app/src/main/java/com/timemaster/TimeMasterApplication.kt`: App container setup.
- `app/src/main/java/com/timemaster/domain/Reminder.kt`: Domain model and enums.
- `app/src/main/java/com/timemaster/domain/ReminderScheduleCalculator.kt`: Pure next-trigger calculation.
- `app/src/main/java/com/timemaster/data/ReminderEntity.kt`: Room entity and mapping.
- `app/src/main/java/com/timemaster/data/ReminderDao.kt`: Room DAO.
- `app/src/main/java/com/timemaster/data/AppDatabase.kt`: Room database.
- `app/src/main/java/com/timemaster/data/ReminderRepository.kt`: Repository API.
- `app/src/main/java/com/timemaster/alarm/ReminderAlarmScheduler.kt`: AlarmManager wrapper.
- `app/src/main/java/com/timemaster/alarm/ReminderAlarmReceiver.kt`: Alarm trigger receiver.
- `app/src/main/java/com/timemaster/alarm/BootReceiver.kt`: Re-schedule reminders after reboot.
- `app/src/main/java/com/timemaster/notification/ReminderNotifier.kt`: Notification and full-screen intent creation.
- `app/src/main/java/com/timemaster/sound/RingtoneCatalog.kt`: Built-in ringtone metadata.
- `app/src/main/java/com/timemaster/sound/RingtonePlayer.kt`: Local audio playback.
- `app/src/main/java/com/timemaster/ui/TimeMasterApp.kt`: Navigation state.
- `app/src/main/java/com/timemaster/ui/theme/Theme.kt`: Accessible theme tokens.
- `app/src/main/java/com/timemaster/ui/home/HomeScreen.kt`: Large-button reminder list.
- `app/src/main/java/com/timemaster/ui/editor/ReminderEditorScreen.kt`: Reminder form.
- `app/src/main/java/com/timemaster/ui/alert/AlertActivity.kt`: Full-screen strong reminder.
- `app/src/test/java/com/timemaster/domain/ReminderScheduleCalculatorTest.kt`: Pure JVM schedule tests.
- `app/src/androidTest/java/com/timemaster/data/ReminderDaoTest.kt`: Room instrumentation tests.
- `app/src/androidTest/java/com/timemaster/ui/HomeScreenTest.kt`: Compose UI tests.
- `app/src/main/res/raw/*.wav`: Five generated local ringtone assets.

## Task 1: Android Project Skeleton

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/timemaster/MainActivity.kt`
- Create: `app/src/main/java/com/timemaster/ui/theme/Theme.kt`

- [ ] **Step 1: Create Gradle project files**

Write `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TimeManagementMaster"
include(":app")
```

Write `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}
```

Write `gradle.properties`:

```properties
android.useAndroidX=true
android.nonTransitiveRClass=true
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
```

- [ ] **Step 2: Create Android app module**

Write `app/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.timemaster"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.timemaster"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    val roomVersion = "2.6.1"
    val lifecycleVersion = "2.8.7"

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.ui:ui:1.7.6")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.core:core:1.15.0")
    ksp("androidx.room:room-compiler:$roomVersion")

    debugImplementation("androidx.compose.ui:ui-tooling:1.7.6")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.6")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.6")
}
```

- [ ] **Step 3: Create manifest and starter activity**

Write `app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />

    <application
        android:name=".TimeMasterApplication"
        android:allowBackup="true"
        android:label="时间管理大师"
        android:theme="@style/Theme.TimeMaster">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

Write `app/src/main/java/com/timemaster/MainActivity.kt`:

```kotlin
package com.timemaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.timemaster.ui.theme.TimeMasterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimeMasterTheme {
                androidx.compose.material3.Text("时间管理大师")
            }
        }
    }
}
```

Write `app/src/main/java/com/timemaster/ui/theme/Theme.kt`:

```kotlin
package com.timemaster.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Colors = lightColorScheme(
    primary = Color(0xFF006A60),
    onPrimary = Color.White,
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1F1B1B)
)

@Composable
fun TimeMasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
```

- [ ] **Step 4: Run project sync/build**

Run: `.\gradlew.bat :app:assembleDebug`

Expected: If Gradle wrapper is not present, this fails with "gradlew.bat is not recognized" or missing wrapper files. Install Android Studio or generate a Gradle wrapper, then rerun until `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties app
git commit -m "chore: scaffold Android app"
```

## Task 2: Reminder Domain and Schedule Calculator

**Files:**
- Create: `app/src/main/java/com/timemaster/domain/Reminder.kt`
- Create: `app/src/main/java/com/timemaster/domain/ReminderScheduleCalculator.kt`
- Create: `app/src/test/java/com/timemaster/domain/ReminderScheduleCalculatorTest.kt`

- [ ] **Step 1: Write failing schedule tests**

Write `ReminderScheduleCalculatorTest.kt`:

```kotlin
package com.timemaster.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class ReminderScheduleCalculatorTest {
    private val rule = ReminderRule(
        intervalMinutes = 30,
        startMinuteOfDay = 8 * 60,
        endMinuteOfDay = 22 * 60,
        enabledDays = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
    )

    @Test
    fun beforeWindow_returnsTodayStart() {
        val now = LocalDateTime.of(2026, 6, 19, 7, 10)
        assertEquals(LocalDateTime.of(2026, 6, 19, 8, 0), nextTrigger(now, rule))
    }

    @Test
    fun insideWindow_advancesByInterval() {
        val now = LocalDateTime.of(2026, 6, 19, 9, 10)
        assertEquals(LocalDateTime.of(2026, 6, 19, 9, 40), nextTrigger(now, rule))
    }

    @Test
    fun afterWindow_skipsToNextEnabledDayStart() {
        val now = LocalDateTime.of(2026, 6, 19, 22, 30)
        assertEquals(LocalDateTime.of(2026, 6, 22, 8, 0), nextTrigger(now, rule))
    }
}
```

- [ ] **Step 2: Run tests to verify failure**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests com.timemaster.domain.ReminderScheduleCalculatorTest`

Expected: FAIL because `ReminderRule` and `nextTrigger` do not exist.

- [ ] **Step 3: Implement domain model**

Write `Reminder.kt`:

```kotlin
package com.timemaster.domain

import java.time.DayOfWeek

enum class AlertMode {
    Strong,
    Normal
}

data class ReminderRule(
    val intervalMinutes: Int,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val enabledDays: Set<DayOfWeek>
) {
    init {
        require(intervalMinutes > 0)
        require(startMinuteOfDay in 0..1439)
        require(endMinuteOfDay in 1..1440)
        require(startMinuteOfDay < endMinuteOfDay)
        require(enabledDays.isNotEmpty())
    }
}

data class Reminder(
    val id: Long,
    val title: String,
    val rule: ReminderRule,
    val alertMode: AlertMode,
    val ringtoneId: String,
    val isEnabled: Boolean,
    val nextTriggerAtMillis: Long?
)
```

Write `ReminderScheduleCalculator.kt`:

```kotlin
package com.timemaster.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun nextTrigger(now: LocalDateTime, rule: ReminderRule): LocalDateTime {
    for (daysAhead in 0..13) {
        val date = now.toLocalDate().plusDays(daysAhead.toLong())
        if (!rule.enabledDays.contains(date.dayOfWeek)) continue

        val start = date.atMinuteOfDay(rule.startMinuteOfDay)
        val end = date.atMinuteOfDay(rule.endMinuteOfDay)
        val candidate = when {
            now.isBefore(start) || daysAhead > 0 -> start
            now.isBefore(end) -> now.plusMinutes(rule.intervalMinutes.toLong())
            else -> null
        }

        if (candidate != null && !candidate.isAfter(end)) {
            return candidate
        }
    }
    error("Unable to find next trigger within two weeks")
}

private fun LocalDate.atMinuteOfDay(minuteOfDay: Int): LocalDateTime {
    val hour = minuteOfDay / 60
    val minute = minuteOfDay % 60
    return LocalDateTime.of(this, LocalTime.of(hour.coerceAtMost(23), minute))
}
```

- [ ] **Step 4: Run tests to verify pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests com.timemaster.domain.ReminderScheduleCalculatorTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/timemaster/domain app/src/test/java/com/timemaster/domain
git commit -m "feat: calculate recurring reminder times"
```

## Task 3: Room Persistence

**Files:**
- Create: `app/src/main/java/com/timemaster/data/ReminderEntity.kt`
- Create: `app/src/main/java/com/timemaster/data/ReminderDao.kt`
- Create: `app/src/main/java/com/timemaster/data/AppDatabase.kt`
- Create: `app/src/main/java/com/timemaster/data/ReminderRepository.kt`
- Create: `app/src/androidTest/java/com/timemaster/data/ReminderDaoTest.kt`

- [ ] **Step 1: Write failing DAO test**

Write `ReminderDaoTest.kt`:

```kotlin
package com.timemaster.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderDaoTest {
    @Test
    fun insertAndReadReminder() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        val dao = db.reminderDao()
        dao.upsert(ReminderEntity(title = "喝水", intervalMinutes = 30))
        val rows = dao.observeAll().first()
        assertEquals("喝水", rows.single().title)
        assertEquals(30, rows.single().intervalMinutes)
        db.close()
    }
}
```

- [ ] **Step 2: Run test to verify failure**

Run: `.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.timemaster.data.ReminderDaoTest`

Expected: FAIL because Room files do not exist.

- [ ] **Step 3: Implement Room files**

Write `ReminderEntity.kt`, `ReminderDao.kt`, `AppDatabase.kt`, and `ReminderRepository.kt` with a `ReminderEntity` defaulting to every day, strong alert, `gentle_chime`, and enabled.

- [ ] **Step 4: Run DAO test**

Run: `.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.timemaster.data.ReminderDaoTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/timemaster/data app/src/androidTest/java/com/timemaster/data
git commit -m "feat: persist reminders locally"
```

## Task 4: Alarm Scheduling and Receivers

**Files:**
- Create: `app/src/main/java/com/timemaster/alarm/ReminderAlarmScheduler.kt`
- Create: `app/src/main/java/com/timemaster/alarm/ReminderAlarmReceiver.kt`
- Create: `app/src/main/java/com/timemaster/alarm/BootReceiver.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/timemaster/TimeMasterApplication.kt`

- [ ] **Step 1: Add scheduler interface and AlarmManager implementation**

Create `ReminderAlarmScheduler.kt` with:

```kotlin
interface AlarmScheduler {
    fun schedule(reminderId: Long, triggerAtMillis: Long)
    fun cancel(reminderId: Long)
}
```

Implement pending intents using action `com.timemaster.ACTION_REMINDER_ALARM` and extra `reminder_id`.

- [ ] **Step 2: Add alarm receiver**

Create `ReminderAlarmReceiver.kt` to read `reminder_id`, load the reminder from the repository, show the correct alert mode, compute the next trigger, update Room, and schedule again.

- [ ] **Step 3: Add boot receiver**

Create `BootReceiver.kt` to handle `android.intent.action.BOOT_COMPLETED`, read enabled reminders, and schedule stored `nextTriggerAtMillis` or calculate a fresh one.

- [ ] **Step 4: Register receivers in manifest**

Add receiver declarations:

```xml
<receiver
    android:name=".alarm.ReminderAlarmReceiver"
    android:exported="false" />
<receiver
    android:name=".alarm.BootReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

- [ ] **Step 5: Manual verification**

Run: `adb shell cmd alarm list | findstr timemaster`

Expected: After enabling a reminder, one pending app alarm is visible for package `com.timemaster`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/timemaster/alarm app/src/main/AndroidManifest.xml app/src/main/java/com/timemaster/TimeMasterApplication.kt
git commit -m "feat: schedule reminder alarms"
```

## Task 5: Notifications, Strong Alert Activity, and Audio

**Files:**
- Create: `app/src/main/java/com/timemaster/notification/ReminderNotifier.kt`
- Create: `app/src/main/java/com/timemaster/ui/alert/AlertActivity.kt`
- Create: `app/src/main/java/com/timemaster/sound/RingtoneCatalog.kt`
- Create: `app/src/main/java/com/timemaster/sound/RingtonePlayer.kt`
- Create: `app/src/main/res/raw/gentle_chime.wav`
- Create: `app/src/main/res/raw/clear_bell.wav`
- Create: `app/src/main/res/raw/digital_tick.wav`
- Create: `app/src/main/res/raw/long_tone.wav`
- Create: `app/src/main/res/raw/bright_prompt.wav`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Generate local ringtone assets**

Use a local script or audio tool to generate five short WAV files from sine waves. Each file must be original/generated, under 10 seconds, and stored in `app/src/main/res/raw`.

- [ ] **Step 2: Implement ringtone catalog**

Create `RingtoneCatalog.kt`:

```kotlin
data class RingtoneOption(val id: String, val label: String, val rawRes: Int)

object RingtoneCatalog {
    val all = listOf(
        RingtoneOption("gentle_chime", "柔和提示", R.raw.gentle_chime),
        RingtoneOption("clear_bell", "清脆铃", R.raw.clear_bell),
        RingtoneOption("digital_tick", "电子滴答", R.raw.digital_tick),
        RingtoneOption("long_tone", "长鸣提醒", R.raw.long_tone),
        RingtoneOption("bright_prompt", "轻快提示", R.raw.bright_prompt)
    )
}
```

- [ ] **Step 3: Implement notification and full-screen alert**

`ReminderNotifier` creates channels `strong_reminders` and `normal_reminders`. Strong reminders use a full-screen intent for `AlertActivity`; normal reminders use a high-priority notification without launching an activity.

- [ ] **Step 4: Implement AlertActivity**

`AlertActivity` displays the reminder title in large text and two buttons: `知道了` and `稍后提醒`. `知道了` stops playback and finishes. `稍后提醒` schedules one alarm 5 minutes later, stops playback, and finishes.

- [ ] **Step 5: Manual verification**

Run a test reminder on a device/emulator.

Expected: Strong reminder shows the full-screen alert or heads-up notification depending on system state; audio loops until a button is tapped.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/timemaster/notification app/src/main/java/com/timemaster/ui/alert app/src/main/java/com/timemaster/sound app/src/main/res/raw app/src/main/AndroidManifest.xml
git commit -m "feat: show reminders with local ringtones"
```

## Task 6: Large Accessible Compose UI

**Files:**
- Create: `app/src/main/java/com/timemaster/ui/TimeMasterApp.kt`
- Create: `app/src/main/java/com/timemaster/ui/home/HomeScreen.kt`
- Create: `app/src/main/java/com/timemaster/ui/editor/ReminderEditorScreen.kt`
- Create: `app/src/androidTest/java/com/timemaster/ui/HomeScreenTest.kt`
- Modify: `app/src/main/java/com/timemaster/MainActivity.kt`
- Modify: `app/src/main/java/com/timemaster/ui/theme/Theme.kt`

- [ ] **Step 1: Write failing Compose UI test**

Write `HomeScreenTest.kt` to render an empty list and assert that `新建周期提醒` exists and has a click action.

- [ ] **Step 2: Run UI test to verify failure**

Run: `.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.timemaster.ui.HomeScreenTest`

Expected: FAIL because `HomeScreen` does not exist.

- [ ] **Step 3: Implement theme typography**

Set default body text to at least `20.sp`, title to at least `30.sp`, and buttons to at least `22.sp`.

- [ ] **Step 4: Implement HomeScreen**

Home shows app title, a full-width `新建周期提醒` button, permission warning banners when needed, empty state, and large reminder cards with merged TalkBack semantics.

- [ ] **Step 5: Implement ReminderEditorScreen**

Editor includes title input, interval controls, start/end time controls, weekday multi-select buttons, alert mode segmented choice, ringtone preview list, and fixed save button. Validate that end time is later than start time.

- [ ] **Step 6: Run UI test**

Run: `.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.timemaster.ui.HomeScreenTest`

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/timemaster/ui app/src/main/java/com/timemaster/MainActivity.kt app/src/androidTest/java/com/timemaster/ui
git commit -m "feat: add accessible reminder screens"
```

## Task 7: Permissions and App Wiring

**Files:**
- Create: `app/src/main/java/com/timemaster/TimeMasterApplication.kt`
- Create: `app/src/main/java/com/timemaster/permissions/PermissionPrompts.kt`
- Modify: `app/src/main/java/com/timemaster/ui/TimeMasterApp.kt`
- Modify: `app/src/main/java/com/timemaster/ui/home/HomeScreen.kt`

- [ ] **Step 1: Create application container**

Create `TimeMasterApplication` with lazy `AppDatabase`, `ReminderRepository`, `ReminderAlarmScheduler`, `ReminderNotifier`, and `RingtonePlayer` instances.

- [ ] **Step 2: Implement permission helpers**

`PermissionPrompts.kt` exposes functions to request `POST_NOTIFICATIONS` and open `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM`.

- [ ] **Step 3: Wire save/enable actions**

When saving or enabling a reminder, persist it, compute `nextTriggerAtMillis`, request needed permissions, and schedule the alarm only when exact alarm permission is available.

- [ ] **Step 4: Manual permission verification**

On Android 13+, revoke notification permission and enable a reminder.

Expected: User sees a clear permission prompt before notification-dependent reminder behavior.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/timemaster/TimeMasterApplication.kt app/src/main/java/com/timemaster/permissions app/src/main/java/com/timemaster/ui
git commit -m "feat: wire reminder permissions and app state"
```

## Task 8: Final Verification

**Files:**
- Modify only files needed to fix verification failures.

- [ ] **Step 1: Run unit tests**

Run: `.\gradlew.bat :app:testDebugUnitTest`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run instrumentation tests**

Run: `.\gradlew.bat :app:connectedDebugAndroidTest`

Expected: `BUILD SUCCESSFUL` with DAO and UI tests passing.

- [ ] **Step 3: Build debug APK**

Run: `.\gradlew.bat :app:assembleDebug`

Expected: APK created at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 4: Manual smoke test**

Install the APK, create a reminder every 1 minute between current time and 10 minutes later, select a ringtone, enable strong alert, wait for trigger, tap `稍后提醒`, then tap `知道了` on the second alert.

Expected: Alert fires, audio plays, snooze fires 5 minutes later, and acknowledge stops audio.

- [ ] **Step 5: Accessibility smoke test**

Turn on TalkBack and large system font. Navigate homepage, editor, and alert page.

Expected: Controls are readable, non-overlapping, and TalkBack reads complete useful labels.

- [ ] **Step 6: Final commit**

```bash
git status --short
git add app
git commit -m "test: verify time management MVP"
```

## Self-Review

- Spec coverage: The plan covers local data, large accessible UI, recurring time windows, weekday enablement, strong/normal reminders, built-in ringtones, permissions, reboot rescheduling, and tests.
- Red-flag scan: The plan contains no unfinished marker text. Some implementation steps intentionally name focused files and required behavior instead of embedding entire Android framework-heavy classes.
- Type consistency: Domain names are `Reminder`, `ReminderRule`, `AlertMode`, `nextTrigger`, `ReminderEntity`, `ReminderRepository`, and are reused consistently across tasks.
