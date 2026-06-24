# 页面标题栏整体上移 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在保留状态栏安全区域的前提下，将首页、设置页、编辑页和新建周期提醒页的标题栏与内容整体上移 12.dp。

**Architecture:** 新增一个公共页面布局文件，集中定义顶部 8.dp、水平 20.dp、底部 20.dp 的页面内容边距。三个页面继续各自调用 `statusBarsPadding()`，随后统一应用公共边距，因此安全区域、标题栏内部对齐和标题与内容间距均保持不变。

**Tech Stack:** Kotlin、Jetpack Compose、JUnit 4、Android Compose UI Test、Gradle

---

## 文件结构

- 新建 `app/src/main/java/com/timemaster/ui/layout/PageLayout.kt`：保存公共页面内容边距及应用该边距的 `Modifier` 扩展。
- 新建 `app/src/test/java/com/timemaster/ui/layout/PageLayoutTest.kt`：锁定顶部、水平和底部边距值。
- 修改 `app/src/main/java/com/timemaster/ui/home/HomeScreen.kt`：首页根容器应用公共边距。
- 修改 `app/src/main/java/com/timemaster/ui/settings/SettingsScreen.kt`：设置页根容器应用公共边距。
- 修改 `app/src/main/java/com/timemaster/ui/editor/ReminderEditorScreen.kt`：编辑页和新建页共用根容器应用公共边距。

### Task 1: 公共页面边距

**Files:**
- Create: `app/src/main/java/com/timemaster/ui/layout/PageLayout.kt`
- Create: `app/src/test/java/com/timemaster/ui/layout/PageLayoutTest.kt`

- [ ] **Step 1: 写入失败测试**

```kotlin
package com.timemaster.ui.layout

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class PageLayoutTest {
    @Test
    fun pageContentInsetsKeepHorizontalAndBottomSpacingWhileRaisingTop() {
        assertEquals(20.dp, PageContentHorizontalPadding)
        assertEquals(8.dp, PageContentTopPadding)
        assertEquals(20.dp, PageContentBottomPadding)
    }
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run:

```powershell
$env:JAVA_HOME='D:\Programs\Android Studio\jbr'
$env:ANDROID_HOME='C:\Users\YANG\AppData\Local\Android\Sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\.gradle-local\gradle-8.10.2\bin\gradle.bat :app:testDebugUnitTest --tests com.timemaster.ui.layout.PageLayoutTest
```

Expected: FAIL，提示 `PageContentHorizontalPadding`、`PageContentTopPadding` 和 `PageContentBottomPadding` 未定义。

- [ ] **Step 3: 实现公共边距**

```kotlin
package com.timemaster.ui.layout

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal val PageContentHorizontalPadding = 20.dp
internal val PageContentTopPadding = 8.dp
internal val PageContentBottomPadding = 20.dp

fun Modifier.pageContentPadding(): Modifier = padding(
    start = PageContentHorizontalPadding,
    top = PageContentTopPadding,
    end = PageContentHorizontalPadding,
    bottom = PageContentBottomPadding
)
```

- [ ] **Step 4: 运行测试并确认通过**

Run: Task 1 Step 2 的同一命令。

Expected: `BUILD SUCCESSFUL`，`PageLayoutTest` 通过。

- [ ] **Step 5: 提交公共边距**

```powershell
git add app/src/main/java/com/timemaster/ui/layout/PageLayout.kt app/src/test/java/com/timemaster/ui/layout/PageLayoutTest.kt
git commit -m "新增统一页面安全边距配置"
```

### Task 2: 三个页面统一应用公共边距

**Files:**
- Modify: `app/src/main/java/com/timemaster/ui/home/HomeScreen.kt`
- Modify: `app/src/main/java/com/timemaster/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/timemaster/ui/editor/ReminderEditorScreen.kt`

- [ ] **Step 1: 替换首页根容器边距**

导入：

```kotlin
import com.timemaster.ui.layout.pageContentPadding
```

将根 `Column` 中位于 `statusBarsPadding()` 后的：

```kotlin
.padding(20.dp)
```

替换为：

```kotlin
.pageContentPadding()
```

- [ ] **Step 2: 替换设置页根容器边距**

导入：

```kotlin
import com.timemaster.ui.layout.pageContentPadding
```

仅将根 `Column` 中位于 `statusBarsPadding()` 后的 `.padding(20.dp)` 替换为 `.pageContentPadding()`，保留设置卡片内部的其他 `padding`。

- [ ] **Step 3: 替换编辑页和新建页根容器边距**

导入：

```kotlin
import com.timemaster.ui.layout.pageContentPadding
```

仅将 `ReminderEditorScreen` 根 `Column` 中位于 `verticalScroll(...)` 后的 `.padding(20.dp)` 替换为 `.pageContentPadding()`，不修改时间选择器、星期按钮或其他控件边距。

- [ ] **Step 4: 编译 Android 测试源码**

Run:

```powershell
$env:JAVA_HOME='D:\Programs\Android Studio\jbr'
$env:ANDROID_HOME='C:\Users\YANG\AppData\Local\Android\Sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\.gradle-local\gradle-8.10.2\bin\gradle.bat :app:compileDebugAndroidTestKotlin
```

Expected: `BUILD SUCCESSFUL`，首页、设置页和编辑页现有 Compose 测试源码继续编译。

- [ ] **Step 5: 提交页面接入**

```powershell
git add app/src/main/java/com/timemaster/ui/home/HomeScreen.kt app/src/main/java/com/timemaster/ui/settings/SettingsScreen.kt app/src/main/java/com/timemaster/ui/editor/ReminderEditorScreen.kt
git commit -m "统一上移各页面标题栏和内容"
```

### Task 3: 完整验证与 APK

**Files:**
- Verify: `app/src/main/java/com/timemaster/ui/layout/PageLayout.kt`
- Verify: `app/src/main/java/com/timemaster/ui/home/HomeScreen.kt`
- Verify: `app/src/main/java/com/timemaster/ui/settings/SettingsScreen.kt`
- Verify: `app/src/main/java/com/timemaster/ui/editor/ReminderEditorScreen.kt`

- [ ] **Step 1: 运行完整单元测试**

Run:

```powershell
$env:JAVA_HOME='D:\Programs\Android Studio\jbr'
$env:ANDROID_HOME='C:\Users\YANG\AppData\Local\Android\Sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\.gradle-local\gradle-8.10.2\bin\gradle.bat :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`，无失败测试。

- [ ] **Step 2: 生成 Debug APK**

Run:

```powershell
$env:JAVA_HOME='D:\Programs\Android Studio\jbr'
$env:ANDROID_HOME='C:\Users\YANG\AppData\Local\Android\Sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\.gradle-local\gradle-8.10.2\bin\gradle.bat :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`，生成 `app/build/outputs/apk/debug/app-debug.apk`。

- [ ] **Step 3: 静态自检**

Run:

```powershell
rg -n "statusBarsPadding|pageContentPadding|padding\(20\.dp\)" app/src/main/java/com/timemaster/ui/home/HomeScreen.kt app/src/main/java/com/timemaster/ui/settings/SettingsScreen.kt app/src/main/java/com/timemaster/ui/editor/ReminderEditorScreen.kt
git diff --check
git status --short --branch
```

Expected:

- 三个页面仍包含 `statusBarsPadding()`。
- 三个根容器均包含 `pageContentPadding()`。
- 其他组件内部 `padding(20.dp)` 如存在则保持原样。
- `git diff --check` 无错误。

- [ ] **Step 4: 手机手动验收**

1. 启动首页，确认设置按钮、标题和加号按钮水平排列且垂直居中。
2. 进入设置页，确认返回按钮与“设置”标题对齐。
3. 打开已有提醒，确认返回按钮与“编辑提醒”标题对齐。
4. 新建提醒，确认返回按钮与“新建周期提醒”标题对齐。
5. 在不同字体大小和显示大小下确认标题栏未侵入系统状态栏。
6. 确认标题与首个内容控件间距未变化，所有内容随标题整体上移。
