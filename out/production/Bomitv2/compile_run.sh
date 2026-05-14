#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
#  Bom It! V2  —  compile and launch helper
#
#  Requirements: Java 11+ JDK (javac must be on PATH).
#  Usage       : bash compile_run.sh
# ─────────────────────────────────────────────────────────────────────────────
set -e

SRC="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT="$SRC/out"

echo ">> Compiling to $OUT ..."
mkdir -p "$OUT"

# Compile all .java files; -sourcepath lets the compiler resolve inter-package
# dependencies automatically without listing every file manually.
javac -sourcepath "$SRC" \
      -d "$OUT" \
      "$SRC"/Game_2D/Main.java \
      "$SRC"/Game_2D/GamePanel.java \
      "$SRC"/Game_2D/KeyHandler.java \
      "$SRC"/Game_2D/CollisionChecker.java \
      "$SRC"/Game_2D/Renderable.java \
      "$SRC"/Game_2D/Updatable.java \
      "$SRC"/bomb/Bomb.java \
      "$SRC"/bomb/BombAlgorithm.java \
      "$SRC"/bomb/BombAppearance.java \
      "$SRC"/bomb/Flame.java \
      "$SRC"/bomb/FlameAppearance.java \
      "$SRC"/bomb/FlameDirection.java \
      "$SRC"/entity/Entity.java \
      "$SRC"/entity/Player.java \
      "$SRC"/entity/Bot.java \
      "$SRC"/entity/Pathfinder.java \
      "$SRC"/entity/Direction.java \
      "$SRC"/entity/Destructible.java \
      "$SRC"/tile/Tile.java \
      "$SRC"/tile/TileManager.java

echo ">> Launching Bom It! ..."
java -cp "$OUT" Game_2D.Main
