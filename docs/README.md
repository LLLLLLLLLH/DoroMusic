# DoroMusic

一个基于 Jetpack Compose 和 Media3 的现代 Android 音乐播放器。

## 功能特性

- **轻巧稳定**: 单 Activity 架构，代码简洁，运行流畅稳定
- **简约设计**: 无冗余功能，专注于本地音乐播放核心体验
- **Material 3 主题**: 采用最新的 Material 3 设计语言，支持动态配色（Android 12+）
- **莫奈色彩**: 动态播放器 UI 自适应莫奈色彩系统
- **莫奈主题图标**: 支持 Android 12+ 主题图标适配
- **音乐库管理**: 完整的本地音乐库浏览和管理功能
- **快速搜索**: 快速搜索并浏览您最喜欢的音乐
- **MediaStore 集成**: 使用 MediaStore 快速访问设备上的音乐
- **多种视图**: 支持列表视图和网格视图自由切换
- **文件夹浏览**: 按文件夹和文件系统浏览歌曲
- **智能排序**: 自然排序和其他各种排序选项
- **播放列表**: 支持只读播放列表和自定义播放列表管理
- **播放队列**: 支持顺序、随机、单曲循环播放模式
- **后台播放**: 使用 MediaSessionService 实现后台播放

## 技术栈

| 组件 | 技术 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| 依赖注入 | Koin |
| 数据库 | Room |
| 播放器 | Media3 / ExoPlayer |
| 导航 | Navigation 3 |
| 图片加载 | Coil 3 |
| 数据序列化 | kotlinx.serialization |

## 项目结构

项目采用 Clean Architecture 架构：

```
app/src/main/java/com/doro/music/
├── data/          # 数据层 (Room, DataStore, Repository)
├── domain/        # 领域层 (Use Cases)
├── player/        # 播放器核心 (ExoPlayer, MediaSession)
├── ui/            # 界面层 (Compose Screens, Components)
├── vm/            # ViewModels
├── di/            # 依赖注入模块
└── ext/           # 扩展函数
```

## 构建要求

- **minSdk**: 24
- **targetSdk**: 36
- **Java**: 11
- **Kotlin**: 2.3.10

## 快速开始

```bash
# 构建调试版本
./gradlew assembleDebug

# 运行单元测试
./gradlew testDebugUnitTest

# 安装到设备
./gradlew installDebug
```

## 设计参考

本项目在界面设计和交互逻辑上参考了 [Gramophone](https://github.com/FoedusProgramme/Gramophone) —— 一款优秀的开源 Material 3 音乐播放器。感谢 Gramophone 团队提供的优秀设计灵感。

## 开源协议

本项目采用 Apache 2.0 协议开源。

---

**注意**: 本项目仅供学习和参考使用。
