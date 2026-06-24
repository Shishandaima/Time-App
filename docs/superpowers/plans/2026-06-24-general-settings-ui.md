# 通用设置项 UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在设置页“通用”卡片内新增字体大小、响铃时长和震动开关 UI，同时保持主题功能不变。

**Architecture:** 仅修改 `SettingsScreen`，字体大小和响铃时长复用现有值行组件，震动开关使用页面内 `remember` 临时状态和 Material `Switch`。通过现有 Compose UI 测试验证文本、顺序、默认状态和切换行为，不接入存储或业务层。

**Tech Stack:** Kotlin、Jetpack Compose Material 3、Compose UI Test、Gradle

---

### Task 1: 用 Compose 测试定义新增 UI

**Files:**
- Modify: `app/src/androidTest/java/com/timemaster/ui/SettingsScreenTest.kt`

- [ ] **Step 1: 添加新增设置项测试**

在现有测试中断言“字体大小”“标准”“响铃时长”“10秒”“震动开关”存在。新增独立测试，通过 `onAllNodesWithText(...).fetchSemanticsNodes()` 的屏幕纵坐标确认字体大小、响铃时长、震动开关依次排列；通过 `onNodeWithText("震动开关").performClick()` 和 `onNode(isToggleable())` 验证开关默认开启且可切换。

- [ ] **Step 2: 编译测试并确认失败**

Run:

```powershell
$env:JAVA_HOME='D:\Programs\Android Studio\jbr'
$env:ANDROID_HOME='C:\Users\YANG\AppData\Local\Android\Sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\.gradle-local\gradle-8.10.2\bin\gradle.bat :app:compileDebugAndroidTestKotlin
```

Expected: 测试源码可编译；运行设置页仪器测试时，新增文本和开关断言在实现前失败。

### Task 2: 实现三个设置项

**Files:**
- Modify: `app/src/main/java/com/timemaster/ui/settings/SettingsScreen.kt`

- [ ] **Step 1: 添加临时震动状态**

```kotlin
var vibrationEnabled by remember { mutableStateOf(true) }
```

- [ ] **Step 2: 在主题选项后添加两个值行**

```kotlin
SettingsValueRow(label = "字体大小", value = "标准")
SettingsValueRow(label = "响铃时长", value = "10秒")
```

- [ ] **Step 3: 添加震动开关行**

新增 `SettingsSwitchRow`，复用 `SettingsValueRow` 的整行垂直内边距、`bodyLarge` 字体、左右布局和居中对齐。整行可点击，右侧 `Switch` 反映并切换临时状态。

- [ ] **Step 4: 编译 Android 测试源码**

Run Task 1 Step 2 的命令。

Expected: `BUILD SUCCESSFUL`。

- [ ] **Step 5: 提交 UI 实现**

```powershell
git add app/src/main/java/com/timemaster/ui/settings/SettingsScreen.kt app/src/androidTest/java/com/timemaster/ui/SettingsScreenTest.kt
git commit -m "新增字体响铃和震动设置界面"
```

### Task 3: 完整验证和 APK

**Files:**
- Verify: `app/src/main/java/com/timemaster/ui/settings/SettingsScreen.kt`
- Verify: `app/src/androidTest/java/com/timemaster/ui/SettingsScreenTest.kt`

- [ ] **Step 1: 运行完整单元测试**

```powershell
$env:JAVA_HOME='D:\Programs\Android Studio\jbr'
$env:ANDROID_HOME='C:\Users\YANG\AppData\Local\Android\Sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\.gradle-local\gradle-8.10.2\bin\gradle.bat :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`。

- [ ] **Step 2: 编译 Android 测试**

```powershell
.\.gradle-local\gradle-8.10.2\bin\gradle.bat :app:compileDebugAndroidTestKotlin
```

Expected: `BUILD SUCCESSFUL`。

- [ ] **Step 3: 构建 APK**

```powershell
.\.gradle-local\gradle-8.10.2\bin\gradle.bat :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`，APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

- [ ] **Step 4: 静态检查**

确认 Git 变更仅包含设置页、设置页测试和计划文档；确认未新增存储、API 或设置模型代码。
