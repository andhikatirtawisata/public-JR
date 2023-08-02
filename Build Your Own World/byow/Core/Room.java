package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;


public class Room {
    int width;
    int height;
    Position referencePoint;
    TETile[][] board;

    public Room(Position limit1, Position limit2, TETile[][] board) {
        this.board = board;
        for (int x = limit1.getX(); x < limit2.getX(); x += 1) {
            for (int y = limit1.getY(); y < limit2.getY(); y += 1) {
                if ((x == limit1.getX() || x == limit2.getX() - 1 || y == limit1.getY() || y == limit2.getY() - 1)
                        && board[x][y] == Tileset.NOTHING) {
                    board[x][y] = Tileset.WALL;
                } else {
                    board[x][y] = Tileset.FLOOR;
                }
            }
        }
        this.referencePoint = new Position((limit1.getX() + limit2.getX()) / 2,
                (limit1.getY() + limit2.getY()) / 2, board);
        width = limit2.getX() - limit1.getX();
        height = limit2.getY() - limit1.getY();

    }

    public Position getReferencePoint() {
        return this.referencePoint;
    }

}
