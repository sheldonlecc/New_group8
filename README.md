# Forbidden Island (Java Swing Version)

This project is a Java digital version of the classic cooperative board game **"Forbidden Island"**. In the game, players (supporting 2-4 players) act as a team of adventurers who must work together on a rapidly sinking island to collect four mysterious ancient treasures and finally escape together by helicopter before the island sinks.

[cite_start]This project was developed as a course project for "COMP2008J Software Engineering Project 1" and strictly follows the **MVC (Model-View-Controller)** architectural pattern for its design and development[cite: 28, 1095].

## ✨ Key Features

* **Complete Game Setup:**
    * **Player Count:** Supports 2-4 players[cite: 1209, 1512, 1515].
    * **Map Selection:** Offers three different map layouts (Classic, Advanced, Expert)[cite: 1211, 1212, 1213].
    * **Difficulty Selection:** Provides four difficulty levels (Novice, Normal, Elite, Legendary), which affect the game's initial water level[cite: 1214, 1215, 1216, 1217, 1218, 1454, 1455].
* **Core Gameplay Mechanics:**
    * **Dynamic Island Sinking:** Map tiles dynamically become Flooded and Sunk as Flood cards are drawn[cite: 53, 353, 358, 1233, 1921].
    * **Treasure Collection:** Players must collect 4 matching Treasure cards and be on the corresponding tile to claim a treasure (4 types total: Earth, Wind, Fire, Water)[cite: 55, 572, 573, 576, 577, 578, 1248, 1249, 1250, 1251, 1252, 1599, 1949].
    * **Card System:** Includes a Treasure Deck and a Flood Deck[cite: 52, 175, 176, 508, 513, 515, 516]. [cite_start]The Treasure Deck also contains special cards (Sandbag, Helicopter, and Water Rise)[cite: 457, 458, 459, 1254, 1255, 1256, 1257].
    * [cite_start]**Water Level Management:** The water level rises throughout the game, increasing the number of Flood cards drawn each turn and raising the difficulty[cite: 54, 174, 380, 381, 385, 386, 1241, 1243, 1455].
* **Role System:**
    * [cite_start]**6 Unique Roles:** At the start of the game, players are randomly assigned one of 6 roles, each with a unique special ability[cite: 281, 1936].
    * [cite_start]Roles include: Diver [cite: 272, 1940][cite_start], Engineer [cite: 273, 1941][cite_start], Pilot [cite: 274, 1938][cite_start], Messenger [cite: 275, 1942][cite_start], Explorer [cite: 276, 1937][cite_start], and Navigator[cite: 277, 1939].
* **Rich User Experience:**
    * [cite_start]**In-Game Rule Viewer:** A `RuleView` component was added, allowing players to consult the detailed game rulebook from within the application at any time[cite: 1001, 1341].
    * [cite_start]**Audio Manager:** An `AudioManager` module was implemented to provide background music and sound effects for the game, including a toggle switch[cite: 89, 96, 644, 1329].

## 🛠️ Tech Stack & Architecture

* [cite_start]**Primary Language:** **Java** [cite: 38]
* [cite_start]**GUI Framework:** **Java Swing** [cite: 39]
* [cite_start]**Version Control:** Git & GitHub [cite: 40, 41]

### MVC Architecture

[cite_start]This project strictly follows the **MVC (Model-View-Controller)** pattern[cite: 28, 43, 1095]:

* [cite_start]**Model:** Responsible for managing all game data and business logic[cite: 50, 308, 309, 1098]. [cite_start]Includes data structures like `Player` [cite: 311][cite_start], `Tile` [cite: 350][cite_start], `WaterLevel` [cite: 379][cite_start], and packages for `Cards` [cite: 450][cite_start], `Deck` [cite: 508][cite_start], and `Role`[cite: 588].
* [cite_start]**View:** Responsible for the user interface and visual presentation[cite: 56, 635, 1099]. [cite_start]Includes all Swing components like `MainView` [cite: 638][cite_start], `SetupView` [cite: 668][cite_start], `BoardView` [cite: 709][cite_start], `MapView` [cite: 752][cite_start], and `PlayerInfoView`[cite: 831].
* [cite_start]**Controller:** Acts as the coordinator between the Model and View[cite: 63, 88, 1100]. It handles user input, updates the model, and refreshes the view. [cite_start]Main components include `GameController` [cite: 169][cite_start], `CardController` [cite: 123][cite_start], `MapController` [cite: 222][cite_start], and `RoleManager`[cite: 269].

### Key Design Patterns

Multiple object-oriented design patterns were applied in this project to enhance code maintainability and scalability:

* [cite_start]**Singleton Pattern:** Used to manage globally unique game states, such as in `GameController` and `WaterLevel`[cite: 44, 97, 653, 1053, 1055].
* [cite_start]**Observer Pattern:** Used to automatically notify the View to update when Model data (like a player's `HandCard`) changes[cite: 45, 1071, 1087].
* [cite_start]**Factory Pattern:** Used for centralized object creation, such as `RoleManager` which creates and randomly assigns all role instances[cite: 47, 761, 1122, 1154, 1155, 1156].
* [cite_start]**Strategy Pattern:** Used to encapsulate the unique abilities of different `Role` classes (e.g., logic for movement or shoring up tiles)[cite: 46].
* [cite_start]**Facade Pattern:** `GameController` acts as a unified entry point for the entire game logic, providing a simplified interface for the View layer and hiding the complex interactions of internal subsystems (like card, map, and player management)[cite: 1161, 1162, 1189, 1190, 1191].

* ## 👥 Team Members (Group 8)

* [cite_start]**Zhixiao Li (23219669):** Model / Foundation Framework / Testing [cite: 11, 1314]
* [cite_start]**Jiuzhou Zhu (23219655):** View / Testing [cite: 11, 1314]
* [cite_start]**Haoyang You (23219612):** Controller / Testing [cite: 11, 1315]


