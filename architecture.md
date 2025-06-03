# 项目架构文档

## 概述
本项目采用 **MVC (Model-View-Controller)** 架构模式，将应用程序分为三个主要层次：
- **Model 层**：负责数据管理和业务逻辑
- **View 层**：负责用户界面显示
- **Controller 层**：负责协调 Model 和 View，处理用户交互

---

## Model 层

### 1. 卡牌系统

#### Card.java (抽象基类)
**作用**：所有卡牌的基础类，定义卡牌的基本属性和行为。

**主要变量**：
- `name: String` - 卡牌名称
- `description: String` - 卡牌描述

**主要方法**：
- `Card(String name, String description)` - 构造函数，初始化卡牌基本信息
- `getName(): String` - 获取卡牌名称
- `getDescription(): String` - 获取卡牌描述

**使用方式**：作为所有具体卡牌类的父类，不能直接实例化。

#### TreasureCard.java
**作用**：宝藏卡牌，用于收集宝藏。

**主要变量**：
- `treasureType: TreasureType` - 宝藏类型（Earth, Fire, Wind, Water）

**主要方法**：
- `TreasureCard(TreasureType treasureType)` - 构造函数
- `getTreasureType(): TreasureType` - 获取宝藏类型

**使用方式**：玩家收集4张相同类型的宝藏卡可以获得对应宝藏。

#### SandbagCard.java
**作用**：沙袋卡牌，用于修复被淹没的地块。

**主要方法**：
- `SandbagCard()` - 构造函数
- `canUse(Player player, Tile tile): boolean` - 检查是否可以使用
- `use(Player player, Tile tile): void` - 使用沙袋卡修复地块

**使用方式**：玩家可以使用沙袋卡修复任意被淹没的地块，无需消耗行动点。

#### HelicopterCard.java
**作用**：直升机卡牌，用于移动玩家或胜利逃脱。

**主要变量**：
- `selectedPlayers: List<Player>` - 选中的玩家列表

**主要方法**：
- `HelicopterCard()` - 构造函数
- `canUseForMovement(List<Player> players): boolean` - 检查是否可用于移动
- `canUseForVictory(List<Player> players): boolean` - 检查是否可用于胜利
- `use(List<Player> players, Tile targetTile): void` - 使用直升机卡

**使用方式**：可以将任意数量的玩家移动到任意地块，或在满足胜利条件时用于逃脱。

#### WaterRiseCard.java
**作用**：水位上升卡牌，触发水位上升事件。

**主要变量**：
- `waterLevel: WaterLevel` - 水位实例引用

**主要方法**：
- `WaterRiseCard()` - 构造函数
- `execute(): void` - 执行水位上升效果
- `getWaterLevel(): WaterLevel` - 获取当前水位

**使用方式**：当抽到此卡时自动执行，提升水位并重新洗牌洪水牌组。

#### FloodCard.java
**作用**：洪水卡牌，用于淹没特定地块。

**主要变量**：
- `tileName: TileName` - 对应的地块名称

**主要方法**：
- `FloodCard(TileName tileName)` - 构造函数
- `getTileName(): TileName` - 获取地块名称
- `execute(Tile tile): void` - 执行洪水效果

**使用方式**：根据水位抽取相应数量的洪水卡，淹没对应地块。

### 2. 牌组系统

#### Deck.java (抽象基类)
**作用**：所有牌组的基础类。

**主要变量**：
- `cards: List<Card>` - 卡牌列表

**主要方法**：
- `Deck()` - 构造函数，初始化空牌组
- `shuffle(): void` - 洗牌
- `draw(): Card` - 抽取一张卡
- `isEmpty(): boolean` - 检查牌组是否为空

#### TreasureDeck.java
**作用**：宝藏牌组，管理宝藏卡和特殊卡。

**主要变量**：
- `helicopterTile: Tile` - 直升机着陆点
- `collectedTreasures: Map<TreasureType, Boolean>` - 已收集宝藏状态

**主要方法**：
- `TreasureDeck(Tile helicopterTile)` - 构造函数
- `moveCardToBottom(Card card): void` - 将卡牌移到牌组底部
- `isTreasureCollected(TreasureType type): boolean` - 检查宝藏是否已收集
- `collectTreasure(TreasureType type): void` - 收集宝藏

**使用方式**：每回合结束时玩家从此牌组抽取2张卡。

#### FloodDeck.java
**作用**：洪水牌组，管理洪水卡。

**主要变量**：
- `activeDeck: List<FloodCard>` - 活跃牌组
- `discardPile: List<FloodCard>` - 弃牌堆

**主要方法**：
- `FloodDeck()` - 构造函数
- `resetFloodCards(): void` - 重置洪水卡
- `getFloodCardForTile(TileName tileName): FloodCard` - 获取特定地块的洪水卡
- `discard(FloodCard card): void` - 丢弃洪水卡
- `refillActiveDeck(): void` - 重新填充活跃牌组
- `getUnfloodedTiles(): List<TileName>` - 获取未被淹没的地块

**使用方式**：根据水位抽取洪水卡，淹没对应地块。

#### HandCard.java
**作用**：手牌管理，每个玩家的手牌容器。

**主要变量**：
- `cards: List<Card>` - 手牌列表
- `maxSize: int` - 最大手牌数量（通常为7）

**主要方法**：
- `HandCard()` - 构造函数
- `addCard(Card card): boolean` - 添加卡牌到手牌
- `removeCard(Card card): boolean` - 从手牌移除卡牌
- `getCards(): List<Card>` - 获取所有手牌
- `size(): int` - 获取手牌数量
- `isFull(): boolean` - 检查手牌是否已满

**使用方式**：玩家通过此类管理自己的手牌，进行卡牌操作。

### 3. 枚举类型

#### TileState.java
**作用**：定义地块状态。

**枚举值**：
- `NORMAL` - 正常状态
- `FLOODED` - 被淹没状态
- `SUNK` - 沉没状态

**使用方式**：用于表示游戏地图中每个地块的当前状态。

#### TileName.java
**作用**：定义所有地块的名称。

**主要变量**：
- `displayName: String` - 显示名称

**主要方法**：
- `TileName(String displayName)` - 构造函数
- `getDisplayName(): String` - 获取显示名称

**使用方式**：标识游戏中的24个不同地块。

#### TileType.java
**作用**：定义地块类型。

**枚举值**：
- `TREASURE` - 宝藏地块
- `NORMAL` - 普通地块
- `HELICOPTER` - 直升机着陆点

#### TreasureType.java
**作用**：定义四种宝藏类型。

**枚举值**：
- `EARTH` - 地之宝藏
- `FIRE` - 火之宝藏
- `WIND` - 风之宝藏
- `WATER` - 水之宝藏

### 4. 玩家系统

#### Player.java
**作用**：玩家实体类，管理玩家状态和行为。

**主要变量**：
- `role: Role` - 玩家角色
- `currentTile: Tile` - 当前所在地块
- `handCard: HandCard` - 手牌
- `actionPoints: int` - 行动点数

**主要方法**：
- `Player()` - 构造函数
- `setRole(Role role): void` - 设置角色
- `getRole(): Role` - 获取角色
- `setCurrentTile(Tile tile): void` - 设置当前位置
- `getCurrentTile(): Tile` - 获取当前位置
- `getHandCard(): HandCard` - 获取手牌
- `setActionPoints(int points): void` - 设置行动点
- `getActionPoints(): int` - 获取行动点

**使用方式**：游戏中每个玩家的数据载体，存储玩家的所有状态信息。

### 5. 角色系统

#### Role.java (抽象基类)
**作用**：所有角色的基础类。

**主要变量**：
- `player: Player` - 关联的玩家
- `name: String` - 角色名称
- `description: String` - 角色描述

**主要方法**：
- `Role(String name, String description)` - 构造函数
- `setPlayer(Player player): void` - 设置关联玩家
- `getPlayer(): Player` - 获取关联玩家
- `canUseAbility(): boolean` - 检查是否可以使用特殊能力
- `useSpecialAbility(): void` - 使用特殊能力
- `getMovableTiles(Tile currentTile): List<Tile>` - 获取可移动的地块

#### Pilot.java (飞行员)
**作用**：可以飞行到任意地块的角色。

**主要方法**：
- `Pilot()` - 构造函数
- `canUseAbility(): boolean` - 总是返回true
- `useSpecialAbility(): void` - 激活飞行模式
- `getMovableTiles(Tile currentTile): List<Tile>` - 返回所有未沉没的地块

**使用方式**：消耗1个行动点可以移动到任意未沉没的地块。

#### Engineer.java (工程师)
**作用**：可以用1个行动点修复2个地块的角色。

**主要方法**：
- `Engineer()` - 构造函数
- `canUseAbility(): boolean` - 检查是否有可修复的地块
- `useSpecialAbility(): void` - 激活双重修复模式

**使用方式**：消耗1个行动点可以修复2个相邻的被淹没地块。

#### Explorer.java (探险家)
**作用**：可以对角移动和修复的角色。

**主要方法**：
- `Explorer()` - 构造函数
- `getMovableTiles(Tile currentTile): List<Tile>` - 包含对角相邻的地块

**使用方式**：移动和修复时可以选择对角相邻的地块。

#### Messenger.java (信使)
**作用**：可以远程给予卡牌的角色。

**主要方法**：
- `Messenger()` - 构造函数
- `canUseAbility(): boolean` - 检查是否可以给予卡牌
- `useSpecialAbility(): void` - 激活远程给卡模式

**使用方式**：可以给任意玩家卡牌，无需在同一地块。

#### Navigator.java (导航员)
**作用**：可以移动其他玩家的角色。

**主要方法**：
- `Navigator()` - 构造函数
- `canUseAbility(): boolean` - 检查是否有其他玩家可移动
- `useSpecialAbility(): void` - 激活移动他人模式

**使用方式**：消耗1个行动点可以移动其他玩家最多2格。

#### Diver.java (潜水员)
**作用**：可以穿过被淹没地块移动的角色。

**主要方法**：
- `Diver()` - 构造函数
- `canUseAbility(): boolean` - 检查是否可以使用潜水能力
- `useSpecialAbility(): void` - 激活潜水模式
- `getMovableTiles(Tile currentTile): List<Tile>` - 包含可穿过被淹没地块的路径

**使用方式**：可以穿过被淹没的地块移动到更远的位置。

### 6. 其他核心模型

#### Tile.java
**作用**：地块实体类，表示游戏地图上的一个位置。

**主要变量**：
- `name: TileName` - 地块名称
- `type: TileType` - 地块类型
- `state: TileState` - 地块状态
- `row: int` - 行坐标
- `col: int` - 列坐标
- `treasureType: TreasureType` - 宝藏类型（如果是宝藏地块）

**主要方法**：
- `Tile(TileName name, TileType type, int row, int col)` - 构造函数
- `getName(): TileName` - 获取地块名称
- `getType(): TileType` - 获取地块类型
- `getState(): TileState` - 获取地块状态
- `setState(TileState state): void` - 设置地块状态
- `getRow(): int` - 获取行坐标
- `getCol(): int` - 获取列坐标
- `setTreasureType(TreasureType type): void` - 设置宝藏类型
- `getTreasureType(): TreasureType` - 获取宝藏类型

**使用方式**：构成游戏地图的基本单元，存储位置和状态信息。

#### TilePosition.java
**作用**：管理所有地块的位置映射。

**主要变量**：
- `tilePositions: Map<String, int[]>` - 地块名称到坐标的映射

**主要方法**：
- `TilePosition()` - 构造函数，初始化所有地块位置
- `getPosition(String tileName): int[]` - 获取指定地块的坐标
- `getAllTilePositions(): Map<String, int[]>` - 获取所有地块位置

**使用方式**：提供地块名称和坐标之间的映射关系。

#### WaterLevel.java
**作用**：水位管理单例类。

**主要变量**：
- `instance: WaterLevel` - 单例实例
- `currentLevel: int` - 当前水位
- `maxLevel: int` - 最大水位

**主要方法**：
- `getInstance(): WaterLevel` - 获取单例实例
- `getCurrentLevel(): int` - 获取当前水位
- `setCurrentLevel(int level): void` - 设置当前水位
- `increaseLevel(): void` - 提升水位
- `getFloodCardsCount(): int` - 获取当前水位对应的洪水卡数量

**使用方式**：全局管理游戏水位，影响每回合抽取的洪水卡数量。

---

## View 层

### 1. MainView.java
**作用**：主应用程序窗口和导航中心，管理不同视图之间的转换。

**主要变量**：
- `instance: MainView` - 单例实例
- `currentView: JPanel` - 当前显示的视图
- `audioManager: AudioManager` - 音频管理器
- `isFullScreen: boolean` - 全屏状态

**主要方法**：
- `getInstance(): MainView` - 获取单例实例
- `showMainMenu(): void` - 显示主菜单
- `showSetupView(): void` - 显示游戏设置界面
- `showGameView(int playerCount, String mapType, int difficulty): void` - 显示游戏界面
- `showRuleView(): void` - 显示规则界面
- `toggleFullScreen(): void` - 切换全屏模式
- `setBackgroundMusic(boolean enabled): void` - 设置背景音乐

**使用方式**：作为应用程序的主窗口，协调各个视图的显示和切换。

### 2. BoardView.java
**作用**：游戏主板界面，管理整个游戏界面的布局。

**主要变量**：
- `mapView: MapView` - 地图视图
- `treasureView: TreasureView` - 宝藏视图
- `waterLevelView: WaterLevelView` - 水位视图
- `playerInfoPanels: List<PlayerInfoView>` - 玩家信息面板列表
- `exitButton: JButton` - 退出按钮

**主要方法**：
- `BoardView(int playerCount)` - 构造函数
- `initializeUI(): void` - 初始化用户界面
- `setupResponsiveLayout(): void` - 设置响应式布局
- `addPlayerInfoPanel(PlayerInfoView panel): void` - 添加玩家信息面板
- `updateLayout(): void` - 更新布局

**使用方式**：作为游戏的主要界面容器，组织和显示所有游戏组件。

### 3. MapView.java
**作用**：游戏地图视图，显示6x6的游戏地图。

**主要变量**：
- `buttons: JButton[][]` - 6x6按钮数组
- `tiles: Tile[][]` - 6x6地块数组
- `playerPositions: Map<Integer, Point>` - 玩家位置映射
- `mapType: String` - 地图类型（Classic/Advanced/Expert）
- `isHelicopterMode: boolean` - 直升机模式状态

**主要方法**：
- `MapView(String mapType)` - 构造函数
- `initializeMap(): void` - 初始化地图
- `setupClassicMap(): void` - 设置经典地图
- `setupAdvancedMap(): void` - 设置高级地图
- `setupExpertMap(): void` - 设置专家地图
- `updateTileState(int row, int col, TileState state): void` - 更新地块状态
- `updatePlayerPosition(int playerIndex, int row, int col): void` - 更新玩家位置
- `highlightTiles(List<Tile> tiles): void` - 高亮显示地块
- `clearHighlights(): void` - 清除高亮
- `getButton(int row, int col): JButton` - 获取指定位置的按钮
- `getTile(int row, int col): Tile` - 获取指定位置的地块
- `getAllTiles(): List<Tile>` - 获取所有地块

**使用方式**：显示游戏地图，处理地块点击事件，更新地块状态和玩家位置。

### 4. CardView.java
**作用**：卡牌的视觉表示，继承自JButton。

**主要变量**：
- `card: Card` - 关联的卡牌对象
- `playerCount: int` - 玩家数量（用于调整大小）
- `isHovered: boolean` - 悬停状态

**主要方法**：
- `CardView(Card card, int playerCount)` - 构造函数
- `setupCardAppearance(): void` - 设置卡牌外观
- `setupHoverEffects(): void` - 设置悬停效果
- `getCard(): Card` - 获取关联的卡牌
- `updateSize(int playerCount): void` - 根据玩家数量更新大小

**使用方式**：在玩家手牌区域显示卡牌，支持点击交互和悬停效果。

### 5. PlayerInfoView.java
**作用**：显示单个玩家信息的面板。

**主要变量**：
- `gameController: GameController` - 游戏控制器引用
- `playerNameLabel: JLabel` - 玩家姓名标签
- `roleLabel: JLabel` - 角色标签
- `actionPointsLabel: JLabel` - 行动点标签
- `cardsPanel: JPanel` - 卡牌面板
- `actionButtons: List<JButton>` - 行动按钮列表
- `playerCount: int` - 玩家总数

**主要方法**：
- `PlayerInfoView(GameController gameController)` - 构造函数
- `setPlayerName(String name): void` - 设置玩家姓名
- `setRole(Role role): void` - 设置玩家角色
- `setActionPoints(int points): void` - 设置行动点数
- `getActionPointsLabel(): JLabel` - 获取行动点标签
- `getCardsPanel(): JPanel` - 获取卡牌面板
- `updateButtonStates(): void` - 更新按钮状态
- `adjustSizeForPlayerCount(int playerCount): void` - 根据玩家数量调整大小

**使用方式**：显示玩家的基本信息、手牌和可用行动，处理玩家操作。

### 6. TreasureView.java
**作用**：显示四种宝藏的收集状态。

**主要变量**：
- `treasureButtons: JButton[]` - 宝藏按钮数组（4个）
- `treasureStates: boolean[]` - 宝藏收集状态数组

**主要方法**：
- `TreasureView()` - 构造函数
- `initializeTreasures(): void` - 初始化宝藏显示
- `updateTreasureStatus(TreasureType type, boolean collected): void` - 更新宝藏状态
- `getTreasureButton(int index): JButton` - 获取指定宝藏按钮

**使用方式**：实时显示四种宝藏的收集进度，通过图片变化反映状态。

### 7. WaterLevelView.java
**作用**：显示当前水位状态。

**主要变量**：
- `waterLevelImage: JLabel` - 水位图片标签
- `waterLevelText: JLabel` - 水位文字标签
- `currentLevel: int` - 当前水位

**主要方法**：
- `WaterLevelView()` - 构造函数
- `updateWaterLevel(int level): void` - 更新水位显示
- `getCurrentLevel(): int` - 获取当前水位

**使用方式**：显示当前水位，随着游戏进行动态更新。

### 8. SetupView.java
**作用**：游戏设置界面，配置游戏参数。

**主要变量**：
- `playerCountSlider: JSlider` - 玩家数量滑块
- `mapTypeComboBox: JComboBox<String>` - 地图类型选择框
- `difficultySlider: JSlider` - 难度滑块
- `musicCheckBox: JCheckBox` - 音乐开关
- `mapPreviewLabel: JLabel` - 地图预览标签
- `startButton: JButton` - 开始游戏按钮

**主要方法**：
- `SetupView()` - 构造函数
- `initializeComponents(): void` - 初始化组件
- `setupEventListeners(): void` - 设置事件监听器
- `updateMapPreview(): void` - 更新地图预览
- `getSelectedPlayerCount(): int` - 获取选择的玩家数量
- `getSelectedMapType(): String` - 获取选择的地图类型
- `getSelectedDifficulty(): int` - 获取选择的难度

**使用方式**：在游戏开始前配置游戏参数，提供直观的设置界面。

### 9. RuleView.java
**作用**：游戏规则显示窗口。

**主要变量**：
- `rulePages: List<ImageIcon>` - 规则页面图片列表
- `currentPageIndex: int` - 当前页面索引
- `imageLabel: JLabel` - 图片显示标签
- `prevButton: JButton` - 上一页按钮
- `nextButton: JButton` - 下一页按钮

**主要方法**：
- `RuleView()` - 构造函数
- `loadRulePages(): void` - 加载规则页面
- `showPage(int index): void` - 显示指定页面
- `nextPage(): void` - 下一页
- `prevPage(): void` - 上一页
- `setupKeyboardNavigation(): void` - 设置键盘导航

**使用方式**：以图片形式展示游戏规则，支持翻页浏览和键盘导航。

---

## Controller 层

### 1. GameController.java
**作用**：游戏主控制器，协调整个游戏流程。

**主要变量**：
- `players: List<Player>` - 玩家列表
- `playerInfoViews: List<PlayerInfoView>` - 玩家信息视图列表
- `cardController: CardController` - 卡牌控制器
- `treasureDeck: TreasureDeck` - 宝藏牌组
- `currentPlayerIndex: int` - 当前玩家索引
- `MAX_ACTIONS_PER_TURN: int` - 每回合最大行动数（3）
- `helicopterTile: Tile` - 直升机着陆点
- `waterLevelView: WaterLevelView` - 水位视图
- `currentWaterLevel: int` - 当前水位
- `mapController: MapController` - 地图控制器
- `floodDeck: FloodDeck` - 洪水牌组
- `boardView: BoardView` - 游戏板视图
- `engineerShoreUpCount: int` - 工程师修复计数
- `emergencyMoveQueue: List<Integer>` - 紧急移动队列

**主要方法**：
- `GameController(int playerCount, Tile helicopterTile, WaterLevelView waterLevelView, int initialWaterLevel)` - 构造函数
- `initializeGame(): void` - 初始化游戏
- `startNewTurn(): void` - 开始新回合
- `endTurn(): void` - 结束当前回合
- `movePlayer(int playerIndex, int row, int col): void` - 移动玩家
- `handleShoreUp(int playerIndex): boolean` - 处理修复行动
- `handleMove(int playerIndex): void` - 处理移动行动
- `handleGiveCard(int playerIndex): void` - 处理给卡行动
- `handleCollectTreasure(int playerIndex): void` - 处理收集宝藏
- `canUseSpecialSkill(int playerIndex): boolean` - 检查是否可使用特殊技能
- `useSpecialSkill(int playerIndex): void` - 使用特殊技能
- `checkGameOver(): void` - 检查游戏结束条件
- `checkVictoryConditions(): boolean` - 检查胜利条件
- `drawTreasureCards(): void` - 抽取宝藏卡
- `drawFloodCards(): void` - 抽取洪水卡
- `processWaterRiseCard(): void` - 处理水位上升卡
- `performEmergencyMove(int playerIndex, Tile targetTile): boolean` - 执行紧急移动

**使用方式**：作为游戏的核心控制器，管理游戏状态、回合流程和规则执行。

### 2. MapController.java
**作用**：地图交互控制器，处理地图上的点击事件。

**主要变量**：
- `gameController: GameController` - 游戏控制器引用
- `mapView: MapView` - 地图视图引用
- `isMoveMode: boolean` - 移动模式状态
- `isInShoreUpMode: boolean` - 修复模式状态
- `isNavigatorMoveMode: boolean` - 导航员移动模式
- `currentPlayerIndex: int` - 当前操作玩家索引
- `targetPlayerIndex: int` - 目标玩家索引
- `isSandbagMode: boolean` - 沙袋模式状态
- `isHelicopterMode: boolean` - 直升机模式状态
- `isInEmergencyMoveMode: boolean` - 紧急移动模式
- `emergencyMoveAvailableTiles: List<Tile>` - 紧急移动可用地块

**主要方法**：
- `MapController(GameController gameController, MapView mapView)` - 构造函数
- `initializeListeners(): void` - 初始化监听器
- `actionPerformed(ActionEvent e): void` - 处理按钮点击事件
- `enterMoveMode(int playerIndex): void` - 进入移动模式
- `exitMoveMode(): void` - 退出移动模式
- `enterShoreUpMode(int playerIndex): void` - 进入修复模式
- `exitShoreUpMode(): void` - 退出修复模式
- `enterSandbagMode(int playerIndex): void` - 进入沙袋模式
- `enterHelicopterMode(int playerIndex, List<Player> selectedPlayers, HelicopterCard card): void` - 进入直升机模式
- `enterEmergencyMoveMode(int playerIndex, List<Tile> availableTiles): void` - 进入紧急移动模式
- `handleTileClick(int row, int col): void` - 处理地块点击
- `getMapView(): MapView` - 获取地图视图

**使用方式**：监听地图上的用户交互，根据当前模式执行相应的游戏逻辑。

### 3. CardController.java
**作用**：卡牌交互控制器，处理卡牌相关操作。

**主要变量**：
- `MAX_CARDS: int` - 最大手牌数（7）
- `gameController: GameController` - 游戏控制器引用
- `isDiscardMode: boolean` - 弃牌模式状态
- `cardsToDiscard: int` - 需要弃牌数量
- `cardsDiscarded: int` - 已弃牌数量
- `currentDiscardingPlayer: PlayerInfoView` - 当前弃牌玩家
- `pendingGiveCardPlayerIndex: Integer` - 待给卡玩家索引

**主要方法**：
- `CardController(GameController gameController)` - 构造函数
- `actionPerformed(ActionEvent e): void` - 处理卡牌点击事件
- `handleCardClick(Card card): void` - 处理卡牌点击
- `handleSandbagCard(SandbagCard card): void` - 处理沙袋卡
- `handleHelicopterCard(HelicopterCard card): void` - 处理直升机卡
- `addCard(PlayerInfoView playerInfoView, Card card): void` - 添加卡牌到视图
- `removeCard(PlayerInfoView playerInfoView, Card card): void` - 从视图移除卡牌
- `startDiscardMode(PlayerInfoView player, int cardsToDiscard): void` - 开始弃牌模式
- `handleDiscardCard(Card card): void` - 处理弃牌
- `endDiscardMode(): void` - 结束弃牌模式
- `handleGiveCard(Card card): void` - 处理给卡操作

**使用方式**：管理玩家手牌的显示和交互，处理特殊卡牌的使用逻辑。

### 4. AudioManager.java
**作用**：音频管理器，处理背景音乐播放。

**主要变量**：
- `instance: AudioManager` - 单例实例
- `backgroundMusicClip: Clip` - 背景音乐剪辑
- `isMusicEnabled: boolean` - 音乐开启状态

**主要方法**：
- `getInstance(): AudioManager` - 获取单例实例
- `playBackgroundMusic(): void` - 播放背景音乐
- `stopBackgroundMusic(): void` - 停止背景音乐
- `pauseBackgroundMusic(): void` - 暂停背景音乐
- `resumeBackgroundMusic(): void` - 恢复背景音乐
- `setMusicEnabled(boolean enabled): void` - 设置音乐开关
- `isMusicEnabled(): boolean` - 获取音乐开关状态
- `setVolume(float volume): void` - 设置音量

**使用方式**：全局管理游戏音频，支持播放、暂停、停止和音量控制。

### 5. RoleManager.java
**作用**：角色管理器，负责角色分配和管理。

**主要变量**：
- `availableRoles: List<Role>` - 可用角色列表（静态）

**主要方法**：
- `getRandomRoles(int playerCount): List<Role>` - 获取随机角色列表
- `assignRolesToPlayers(List<Player> players): void` - 为玩家分配角色

**使用方式**：在游戏开始时随机分配角色给玩家，确保角色的唯一性和平衡性。

---

## 架构特点

### 1. MVC 模式优势
- **分离关注点**：Model 专注数据和业务逻辑，View 专注界面显示，Controller 专注交互控制
- **可维护性**：各层职责明确，修改一层不会影响其他层
- **可扩展性**：可以轻松添加新的视图或控制逻辑
- **可测试性**：业务逻辑与界面分离，便于单元测试

### 2. 设计模式应用
- **单例模式**：MainView、AudioManager、WaterLevel 确保全局唯一实例
- **观察者模式**：Controller 监听 View 的事件，实现松耦合
- **策略模式**：不同角色有不同的行为策略
- **工厂模式**：RoleManager 负责创建和分配角色

### 3. 数据流向
1. **用户交互** → View 层捕获事件
2. **事件传递** → Controller 层处理逻辑
3. **数据更新** → Model 层修改状态
4. **状态同步** → View 层更新显示

### 4. 关键交互流程

#### 玩家移动流程
1. 玩家点击"移动"按钮 → PlayerInfoView
2. 触发 actionPerformed → GameController.handleMove()
3. 进入移动模式 → MapController.enterMoveMode()
4. 玩家点击目标地块 → MapController.actionPerformed()
5. 执行移动逻辑 → GameController.movePlayer()
6. 更新玩家位置 → Player.setCurrentTile()
7. 更新视图显示 → MapView.updatePlayerPosition()

#### 卡牌使用流程
1. 玩家点击卡牌 → CardView
2. 触发 actionPerformed → CardController.actionPerformed()
3. 识别卡牌类型 → CardController.handleCardClick()
4. 执行卡牌效果 → 相应的 handle 方法
5. 更新游戏状态 → Model 层相关类
6. 更新界面显示 → View 层相关组件

#### 回合结束流程
1. 行动点耗尽或玩家选择结束 → GameController.endTurn()
2. 抽取宝藏卡 → GameController.drawTreasureCards()
3. 处理手牌上限 → CardController.startDiscardMode()
4. 抽取洪水卡 → GameController.drawFloodCards()
5. 处理水位上升 → GameController.processWaterRiseCard()
6. 检查游戏结束 → GameController.checkGameOver()
7. 开始下一回合 → GameController.startNewTurn()

这个架构设计确保了代码的清晰性、可维护性和可扩展性，为游戏的稳定运行和后续开发提供了良好的基础。