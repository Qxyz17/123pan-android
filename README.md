# 123pan-android
独立维护的 123Pan 安卓客户端，上游：123panNextGen

[![Release](https://img.shields.io/github/v/release/Qxyz17/123pan-android)]
[![Sync Upstream](https://img.shields.io/badge/上游仓库-123panNextGen-green)]
[![License](https://img.shields.io/badge/license-MIT-blue)]

## 仓库关系说明（必看，解决重复仓库疑问）
现有两个相关上游仓库，分工完全不同：
1. https://github.com/123pan/123pan
    仅包含 PC 电脑端程序，**无任何安卓客户端代码**
2. https://github.com/123panNextGen/123pan
    完整整合项目，内置安卓客户端源码，也是本项目代码上游

### 为什么单独新建本仓库？
1. 原版 NextGen 仓库打包、拉取代码体积过大，仅安卓开发/打包需要下载大量无关多端代码
2. 分离安卓模块，单独发布 APK、单独管理安卓端 Issues、独立迭代 UI/适配/安卓专属功能
3. 普通用户只想下载安卓安装包，无需克隆整套多端源码，降低使用门槛
4. 上游 NextGen 同步更新，本仓库会定期合并上游安卓最新代码，无独立分支魔改底层逻辑

## 功能特性
- 完整123网盘文件浏览、批量上传/下载、离线缓存
- 相册自动备份、文件分享、在线视频播放
- 适配 Android 8.0 及以上，深色模式、后台传输优化
- 无内置广告，纯本地开源客户端

## 快速编译
```bash
# 克隆本仓库（仅安卓代码，体积小）
git clone https://github.com/Qxyz17/123pan-android.git
cd 123pan-android
# 使用 Android Studio 直接打开项目编译打包
