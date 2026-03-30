# Blokus – Board Game

Blokus is a digital implementation of the classic abstract strategy board game, developed as part of the Software Practical (SoPra) at TU Dortmund University. The goal is to place as many of your pieces as possible on the board while blocking your opponents.

## About the Game

Blokus is played on a 20×20 grid (or 14×14 in the 2-player variant) with 2 to 4 players. Each player has 21 pieces in their color. The key rule: every new piece must touch at least one corner of your own pieces, but never a side. The player who places the most squares on the board wins.

## Features

- 2, 3 and 4 player game modes
- Hotseat mode (local multiplayer on one screen)
- Network multiplayer using the BGW-Net framework
- AI opponents (Easy Bot and Strong Bot)
- Bot-only simulation with adjustable speed
- Undo and Redo functionality
- Save and load game state
- Basic and Advanced scoring systems
- Piece rotation and flipping before placement

## Tech Stack

- **Language:** Kotlin
- **Framework:** BoardGameWork (BGW)
- **Network:** BGW-Net
- **Build Tool:** Gradle (Kotlin DSL)
- **Database:** File-based save system
- **Testing:** JUnit 5 / Kotlin Test

## Project Structure
```
src/
├── main/kotlin/
│   ├── entity/        # Game entities (BlokusGame, GameState, Participant, Piece, ...)
│   ├── service/       # Business logic (GameService, PlayerActionService, ...)
│   └── gui/           # User interface scenes
└── test/kotlin/
    └── service/       # Unit tests for all services
```

## Game Modes

| Mode | Board Size | Players |
|------|-----------|---------|
| Standard | 20×20 | 4 players |
| 3-Player | 20×20 | 3 players + shared color |
| 2-Player | 20×20 | 2 players, 2 colors each |
| 2-Player Small | 14×14 | 2 players, 1 corner each |

## Scoring

**Basic Scoring:** The player with the fewest remaining squares wins.

**Advanced Scoring:** Each remaining square counts as -1 point. Placing all pieces earns +15 points, with an additional +5 bonus if the last piece placed was the 1×1 piece.

## Authors

Developed by Abdullah Al-Qazzaz and a team of 8 students as part of the Software Practical (SoPra) at TU Dortmund University.

## License

This project was developed for educational purposes at TU Dortmund University.
