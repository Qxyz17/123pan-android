# 123panNextGen Android

[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](LICENSE)

123云盘第三方 Android 客户端，使用 Kotlin + Jetpack Compose 构建，WinUI 风格界面。

## 功能

- 账户登录/切换
- 文件浏览（文件夹树 + 文件列表）
- 新建文件夹
- 文件重命名
- 删除文件/文件夹
- 文件分享（生成分享链接）
- 复制下载链接
- 上传/下载管理
- 多线程下载
- 速度限制
- 代理设置
- 深色/浅色主题

## 技术栈

- **语言**: Kotlin 100%
- **UI**: Jetpack Compose + Material 3 (WinUI 主题)
- **HTTP**: OkHttp
- **JSON**: Gson
- **异步**: Kotlin Coroutines
- **最低 SDK**: Android 5.0 (API 21)
- **目标 SDK**: Android 16 (API 36)
- **包名**: `com.pan123nextgen.android`

## 编译

### 方式一：Android Studio（推荐）

1. 用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. APK 文件位于 `android/app/build/outputs/apk/debug/`

### 方式二：命令行

```bash
# 确保已设置 ANDROID_HOME 环境变量
cd android
./gradlew assembleDebug
```

## 安装

编译后 APK 位于 `android/app/build/outputs/apk/debug/`，直接安装到 Android 设备即可。

## 上游仓库

原始 PC 客户端（Python/PyQt6）：https://github.com/123panNextGen/123pan

## License

Apache License 2.0