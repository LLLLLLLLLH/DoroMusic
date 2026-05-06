# DoroMusic 项目架构优化分析报告（第二轮）

> 基于第一轮优化后的最新代码状态重新排查

## 一、已完成的优化

### 第一轮

| 编号 | 优化项 | 状态 |
|------|--------|------|
| #5 | PlayerManager 依赖倒置 → 引入 PlayQueueStore 接口 | ✅ 已完成 |
| #6 | ArtistDao 返回领域模型 → 创建 ArtistEntity | ✅ 已完成 |
| #8 | MainScreen Tab 耦合 → 提取 MainTab 枚举 | ✅ 已完成 |
| #10 | PlayQueue 双重数据源 → PlayerManager 统一管理 | ✅ 已完成 |
| #11 | 统一错误处理 → BaseViewModel.safeCall | ✅ 已完成 |
| #12 | 命名不一致 → keyWords → keyword | ✅ 已完成 |
| #14 | positionUpdateJob 轮询优化 | ✅ 已完成 |
| #17 | 死代码清理 | ✅ 已完成 |

### 第二轮

| 编号 | 优化项 | 状态 |
|------|--------|------|
| A | SettingsDataStore 提取 decodeSettings 消除重复反序列化 | ✅ 已完成 |
| C | SearchDao sortMode 参数统一为 SortMode 枚举 | ✅ 已完成 |
| F | SongEntity.id 取消 autoGenerate，使用 MediaStore ID 作为主键 + Migration 8→9 | ✅ 已完成 |
| H | FoldersPage 嵌套 NavDisplay 路由提升到 MainRoute 层 | ❌ 已回退 |
| I | 提取 PlaySongsUseCase，消除 4 个 VM 重复 play 模式 | ✅ 已完成 |
| K | PlayerManager 包路径不一致 → 确认实际在 player 包下，import 路径正确，无需修改 | ✅ 已确认 |
| N | ScanSettingsScreen 死代码清理（移除未使用的 mutableStateListOf import） | ✅ 已完成 |

---

## 二、剩余优化点

### 2.1 【SOLID - 单一职责原则 (SRP)】

#### 问题 B：DisplayList 内嵌 Scaffold 导致嵌套 Scaffold

**位置**：[DisplayList.kt](file:///d:/AndroidTool/Project/DoroMusic/app/src/main/java/com/doro/music/ui/component/DisplayList.kt#L29-L33)

`DisplayList` 内部包含 `Scaffold` + `SnackbarHost`，当在已有 Scaffold 的页面（如 MainScreen）中使用时，会形成嵌套 Scaffold。

**优化方案**：将 `DisplayList` 拆分为纯布局组件，Scaffold/Snackbar 职责上移到调用方。DisplayList 只负责列表渲染。

---

### 2.2 【SOLID - 依赖倒置原则 (DIP)】

#### 问题 D：FoldersViewModel 和 PlayerViewModel 未继承 BaseViewModel

**位置**：
- [FoldersViewModel.kt](file:///d:/AndroidTool/Project/DoroMusic/app/src/main/java/com/doro/music/vm/FoldersViewModel.kt)：`class FoldersViewModel(...) : ViewModel()`
- [PlayerViewModel.kt](file:///d:/AndroidTool/Project/DoroMusic/app/src/main/java/com/doro/music/vm/PlayerViewModel.kt)：`class PlayerViewModel(...) : ViewModel()`
- [MainViewModel.kt](file:///d:/AndroidTool/Project/DoroMusic/app/src/main/java/com/doro/music/vm/MainViewModel.kt)：`class MainViewModel(...) : ViewModel()`

项目中 6 个 VM 继承 `BaseViewModel`，但 FoldersVM、PlayerVM、MainVM 仍直接继承 `ViewModel`。

**优化方案**：将 BaseViewModel 拆分为两个层次：

```kotlin
abstract class BaseViewModel : ViewModel() {
    val uiEvent: SharedFlow<UiEvent>
    protected fun emitEvent(event: UiEvent) { ... }
    protected suspend fun <T> safeCall(...): Result<T> { ... }
}

abstract class ListViewModel : BaseViewModel() {
    val sortMode: StateFlow<SortMode>
    val displayMode: StateFlow<DisplayMode>
    fun setSortBy(sort: SortMode) { ... }
    fun setDisplayMode(mode: DisplayMode) { ... }
}
```

- `ListViewModel`：SongsVM、SongListVM、SearchVM、PlaylistsVM、ArtistsVM、SettingsVM
- `BaseViewModel`：MainVM（使用 emitEvent）、PlayerVM（使用 safeCall）
- `FoldersVM`：继承 `ListViewModel`（未来可扩展排序）

---

### 2.3 【SOLID - 接口隔离原则 (ISP)】

#### 问题 E：SongListContent 参数过多（15 个）

**位置**：[SongListContent.kt](file:///d:/AndroidTool/Project/DoroMusic/app/src/main/java/com/doro/music/ui/component/SongListContent.kt#L22-L41)

**优化方案**：引入 `SongListCallbacks` 数据类，将回调参数分组。

---

### 2.4 【数据一致性】

#### 问题 G：MainViewModel 的 scanState 和 scanEvent 双通道语义重叠

**位置**：[MainViewModel.kt](file:///d:/AndroidTool/Project/DoroMusic/app/src/main/java/com/doro/music/vm/MainViewModel.kt#L17-L35)

`ScanState` 同时承担"持续状态"和"一次性事件"两种语义。UI 需要同时订阅两个流。

**优化方案**：统一为单一 `StateFlow<ScanUiState>`，UI 消费事件后调用 `clearScanResult()` 重置。

---

### 2.5 【架构设计】

#### 问题 H：FoldersPage 内嵌 NavDisplay 导致导航状态丢失

**位置**：[FoldersPage.kt](file:///d:/AndroidTool/Project/DoroMusic/app/src/main/java/com/doro/music/ui/screen/main/FoldersPage.kt#L50-L78)

`FoldersPage` 在 `HorizontalPager` 内部维护了独立的 `NavDisplay` + `rememberNavBackStack`。当用户从 FoldersPage 进入 FolderSongs 后切换 Tab，FoldersPage 的 BackStack 会被销毁（HorizontalPager 超出视口时移除 Composable），再次切回时丢失导航状态。

**优化方案**：将 FolderSongs 路由提升到 MainRoute 层统一管理，或使用 `rememberSaveable` 保持嵌套 BackStack 状态。

---

#### 问题 J：PagingConfig 在多个 Repo 中重复创建

**位置**：SongRepo、SongListRepo、PlaylistRepo、ArtistRepo、SearchRepo、FolderRepo

6 个 Repo 都有 `private val pagingConfig = PagingConfig(pageSize = 15, enablePlaceholders = false)`。

**优化方案**：定义 `PagingDefaults.CONFIG` 常量或 AppModule 单例。

---

### 2.6 【命名与代码规范】

#### 问题 L：FolderDao.syncFolders 方法名误导

**位置**：[FolderDao.kt](file:///d:/AndroidTool/Project/DoroMusic/app/src/main/java/com/doro/music/data/db/dao/FolderDao.kt#L27-L28)

方法名为 `syncFolders` 但实际只是 `@Insert` + `IGNORE`，不做删除/更新操作。

**优化方案**：重命名为 `insertFolders` 或 `upsertFolders`。

---

### 2.7 【性能相关】

#### 问题 M：SearchRepo.getAllSongsByKeyWords 全量加载

**位置**：[SearchRepo.kt](file:///d:/AndroidTool/Project/DoroMusic/app/src/main/java/com/doro/music/data/repo/SearchRepo.kt#L20)

当搜索结果很多时，一次性加载所有歌曲到内存。

**优化方案**：限制最大加载数量（`LIMIT :limit`）。

---

## 三、优化优先级建议

| 优先级 | 问题编号 | 问题简述 | 影响面 | 改动量 |
|--------|---------|---------|--------|--------|
| 🟡 P1 | D | ViewModel 继承不一致 | 架构一致性 | 中 |
| 🟡 P1 | G | scanState/scanEvent 双通道 | 代码可维护性 | 小 |
| 🟢 P2 | B | DisplayList 嵌套 Scaffold | 性能/架构 | 中 |
| 🟢 P2 | E | SongListContent 参数过多 | 可维护性 | 中 |
| 🟢 P2 | H | FoldersPage 嵌套 NavDisplay 状态丢失 | 导航状态 | 中 |
| 🟢 P2 | J | PagingConfig 重复创建 | 代码规范 | 小 |
| ⚪ P3 | L | FolderDao.syncFolders 命名误导 | 代码规范 | 小 |
| ⚪ P3 | M | getAllSongsByKeyWords 全量加载 | 大数据场景性能 | 小 |

---

## 四、总结

经过两轮优化，项目在以下方面已有显著改善：

- ✅ PlayerManager 依赖方向正确（依赖 PlayQueueStore 接口而非 UseCase）
- ✅ ArtistDao 分层隔离（返回 ArtistEntity 而非领域模型）
- ✅ PlayQueue 单一数据源（PlayerManager 统一管理队列状态）
- ✅ 错误处理统一（BaseViewModel.safeCall）
- ✅ 命名规范化（keyword、ArtistEntity）
- ✅ 死代码清理（两轮）
- ✅ SettingsDataStore 消除重复反序列化（decodeSettings）
- ✅ SearchDao sortMode 参数类型安全（SortMode 枚举替代 String）
- ✅ SongEntity.id 数据一致性（取消 autoGenerate）
- ✅ 播放逻辑去重（PlaySongsUseCase）

当前剩余优化主要集中在：

1. **架构一致性** — ViewModel 继承层次拆分（P1），scanState/scanEvent 双通道统一（P1）
2. **UI 组件解耦** — DisplayList 嵌套 Scaffold（P2），SongListContent 参数过多（P2），FoldersPage 嵌套 NavDisplay（P2）
3. **代码规范** — PagingConfig 重复创建、方法命名、全量加载限制
