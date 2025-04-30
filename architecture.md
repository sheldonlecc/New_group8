# 项目架构说明

## 整体架构

本项目采用MVC（Model-View-Controller）架构模式，清晰地分离了数据、表现层和控制逻辑。

```
项目结构
├── Model/           # 数据模型层
├── View/            # 视图层
└── Controller/      # 控制器层
```

## 模型层（Model）

### 卡牌系统（Cards）
- `Card`：抽象基类，定义了卡牌的基本属性和行为
  - `type`：卡牌类型（宝藏卡、洪水卡、特殊卡）
  - `name`：卡牌名称
  - `description`：卡牌描述
  - `use()`：抽象方法，定义卡牌使用效果

### 牌组系统（Deck）
- `Deck<T extends Card>`：泛型抽象类，管理卡牌的抽取和弃牌
  - FloodDeck：水位牌堆
  - TreasureDeck：宝藏牌堆、

### 枚举类型（Enumeration）

- `CardType`：卡牌类型枚举
- `TileType`：地形块类型枚举
- `TreasureType`：宝藏类型枚举


### 玩家系统（Player）
- `Player`：玩家类
  - `HandCard`：手牌管理
  - `currentTile`：当前位置
  - 主要方法：`addCard()`、`removeCard()`、`moveTo()`

### 其他核心模型
- `Tile`：地形块类
- `WaterLevel`：水位系统

## 视图层（View）

- `MainView`：主视图
- `BoardView`：游戏板视图
- `CardView`：卡牌视图
- `ControlView`：控制面板视图
- `PlayerInfoView`：玩家信息视图
- `StepShowView`：步骤显示视图

## 控制器层（Controller）

- `GameController`：游戏主控制器
  - 负责协调Model和View层
  - 处理游戏逻辑和玩家操作



## 设计特点

1. **高内聚低耦合**
   - 各个组件职责明确，相互独立
   - 通过抽象类和接口实现松耦合

2. **可扩展性**
   - 卡牌系统支持自定义新卡牌类型
   - 牌组系统使用泛型设计，支持不同类型的卡牌管理

3. **封装性**
   - 核心游戏逻辑封装在Model层
   - 界面交互封装在View层
   - 通过Controller层统一协调