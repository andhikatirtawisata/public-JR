package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Hallway {
    private Position startPoint;
    private Position endPoint;

    public Hallway(Position startPoint, Position endPoint, TETile[][] board) {
        if (startPoint.getX() > endPoint.getX()) {
            int y = startPoint.getY();
            for (int x = startPoint.getX(); x >= endPoint.getX(); x -= 1) {
                board[x][y] = Tileset.FLOOR;
            }
        } else {
            int y = startPoint.getY();
            for (int x = startPoint.getX(); x < endPoint.getX(); x += 1) {
                board[x][y] = Tileset.FLOOR;
            }
        }
        if (startPoint.getY() > endPoint.getY()) {
            int x = endPoint.getX();
            for (int y = startPoint.getY(); y >= endPoint.getY(); y -= 1) {
                board[x][y] = Tileset.FLOOR;
            }
        } else {
            int x = endPoint.getX();
            for (int y = startPoint.getY(); y < endPoint.getY(); y += 1) {
                board[x][y] = Tileset.FLOOR;
            }
        }
    }
}
