# Forbidden Island (Java Swing Version)

This project is a Java digital version of the classic cooperative board game **"Forbidden Island"**. In the game, players (supporting 2-4 players) act as a team of adventurers who must work together on a rapidly sinking island to collect four mysterious ancient treasures and finally escape together by helicopter before the island sinks.

This project was developed as a course project for "COMP2008J Software Engineering Project 1" and strictly follows the **MVC (Model-View-Controller)** architectural pattern for its design and development.

## ✨ Key Features

* **Complete Game Setup:**
    * **Player Count:** Supports 2-4 players.
    * **Map Selection:** Offers three different map layouts (Classic, Advanced, Expert).
    * **Difficulty Selection:** Provides four difficulty levels (Novice, Normal, Elite, Legendary), which affect the game's initial water level.
* **Core Gameplay Mechanics:**
    * **Dynamic Island Sinking:** Map tiles dynamically become Flooded and Sunk as Flood cards are drawn.
    * **Treasure Collection:** Players must collect 4 matching Treasure cards and be on the corresponding tile to claim a treasure (4 types total: Earth, Wind, Fire, Water).
    * **Card System:** Includes a Treasure Deck and a Flood Deck. The Treasure Deck also contains special cards (Sandbag, Helicopter, and Water Rise).
    * **Water Level Management:** The water level rises throughout the game, increasing the number of Flood cards drawn each turn and raising the difficulty.
* **Role System:**
    * **6 Unique Roles:** At the start of the game, players are randomly assigned one of 6 roles, each with a unique special ability.
    * Roles include: Diver, Engineer, Pilot, Messenger, Explorer, and Navigator.
* **Rich User Experience:**
    * **In-Game Rule Viewer:** A `RuleView` component was added, allowing players to consult the detailed game rulebook from within the application at any time.
    * **Audio Manager:** An `AudioManager` module was implemented to provide background music and sound effects for the game, including a toggle switch.

## 📸 Project Screenshots

**Main Game Interface:**

**Game Setup:**

## 🛠️ Tech Stack & Architecture

* **Primary Language:** **Java**
* **GUI Framework:** **Java Swing**
* **Version Control:** Git & GitHub

### MVC Architecture

This project strictly follows the **MVC (Model-View-Controller)** pattern:

* **Model:** Responsible for managing all game data and business logic. Includes data structures like `Player`, `Tile`, `WaterLevel`, and packages for `Cards`, `Deck`, and `Role`.
* **View:** Responsible for the user interface and visual presentation. Includes all Swing components like `MainView`, `SetupView`, `BoardView`, `MapView`, and `PlayerInfoView`.
* **Controller:** Acts as the coordinator between the Model and View. It handles user input, updates the model, and refreshes the view. Main components include `GameController`, `CardController`, `MapController`, and `RoleManager`.

### Key Design Patterns

Multiple object-oriented design patterns were applied in this project to enhance code maintainability and scalability:

* **Singleton Pattern:** Used to manage globally unique game states, such as in `GameController` and `WaterLevel`.
* **Observer Pattern:** Used to automatically notify the View to update when Model data (like a player's `HandCard`) changes.
* **Factory Pattern:** Used for centralized object creation, such as `RoleManager` which creates and randomly assigns all role instances.
* **Strategy Pattern:** Used to encapsulate the unique abilities of different `Role` classes (e.g., logic for movement or shoring up tiles).
* **Facade Pattern:** `GameController` acts as a unified entry point for the entire game logic, providing a simplified interface for the View layer and hiding the complex interactions of internal subsystems (like card, map, and player management).

## 📁 Project Structure

The project source code is organized according to the MVC architectural pattern:

src/  
├── Controller/                # Controller Layer  
│   ├── GameController.java  
│   ├── CardController.java  
│   ├── MapController.java  
│   ├── RoleManager.java  
│   └── AudioManager.java  
├── Model/                     # Model Layer  
│   ├── Player.java  
│   ├── Tile.java  
│   ├── WaterLevel.java  
│   ├── Cards/  
│   ├── Deck/  
│   ├── Enumeration/  
│   └── Role/  
├── View/                      # View Layer  
│   ├── MainView.java  
│   ├── SetupView.java  
│   ├── BoardView.java  
│   ├── MapView.java  
│   └── PlayerInfoView.java  
└── resources/                 # Resource Files (images, audio, etc.)

## 👥 Team Members (Group 8)

* **Zhixiao Li (23219669):** Model / Foundation Framework / Testing
* **Jiuzhou Zhu (23219655):** View / Testing
* **Haoyang You (23219612):** Controller / Testing





