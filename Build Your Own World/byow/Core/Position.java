package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Position {
    private int x;
    private int y;
    private TETile[][] board;

    public Position(int x, int y, TETile[][] board) {
        this.x = x;
        this.y = y;
        this.board = board;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public TETile getAbove() {
        if (y == board[0].length - 1) {
            return null;
        }
        return board[x][y + 1];
    }

    public TETile getBelow() {
        if (y == 0) {
            return null;
        }
        return board[x][y - 1];
    }

    public TETile getLeft() {
        if (x == 0) {
            return null;
        }
        return board[x - 1][y];
    }

    public TETile getRight() {
        if (x == board.length - 1) {
            return null;
        }
        return board[x + 1][y];
    }

    public TETile getNE() {
        if (x == board.length - 1 || y == board.length - 1) {
            return null;
        }
        return board[x + 1][y + 1];
    }

    public TETile getSE() {
        if (x == board.length - 1 || y == 0) {
            return null;
        }
        return board[x + 1][y - 1];
    }

    public TETile getNW() {
        if (x == 0 || y == board.length - 1) {
            return null;
        }
        return board[x - 1][y + 1];
    }

    public TETile getSW() {
        if (x == 0 || y == 0) {
            return null;
        }
        return board[x - 1][y - 1];
    }

    public TETile getTile() {
        return board[x][y];
    }

    public double distanceTo(Position pos) {
        return Math.sqrt(Math.pow(this.x - pos.x, 2) + Math.pow(this.y - pos.y, 2));
    }

    public boolean isBorder() {
        if (getTile() == Tileset.NOTHING && (getAbove() == Tileset.FLOOR || getBelow() == Tileset.FLOOR
                || getLeft() == Tileset.FLOOR || getRight() == Tileset.FLOOR
                || getNE() == Tileset.FLOOR || getSE() == Tileset.FLOOR
                || getNW() == Tileset.FLOOR || getSW() == Tileset.FLOOR)) {
            return true;
        }
        return false;
    }
}
