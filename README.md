# ParkourGame

A Java Swing parkour game with four playable characters and eleven procedurally generated levels.

## Requirements

- JDK 8 or newer
- Windows for the included `run.bat` launcher

## Run

From the project root, run:

```bat
run.bat
```

The script compiles the Java sources into `build/` and starts the game. To compile manually:

```powershell
New-Item -ItemType Directory -Force build | Out-Null
$sources = Get-ChildItem src/main/java -Recurse -Filter '*.java' | ForEach-Object FullName
javac -source 8 -target 8 -d build $sources
java -cp build parkour.Main
```

## Controls

- Move: `A` / `D` or arrow keys
- Jump: `W`, `Up`, or `Space`
- Character ability: `Shift`
- Select characters and levels and use the on-screen buttons with the mouse

## Project layout

```text
src/main/
  java/parkour/
    Main.java
    ParkourGame.java
    characters/
      Character.java
      CharacterFactory.java
      runner/Runner.java
      ninja/Ninja.java
      flash/Flash.java
      ghost/Ghost.java
    levels/
      Level.java
      LevelFactory.java
      level01/Level01.java
      ...
      level11/Level11.java
  resources/parkour/characters/runner/MarioStand.png
  resources/parkour/characters/runner/MarioJump.png
```

Each character and level has its own class and folder. Shared character attributes and procedural level generation live in their respective base classes.
