package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
/* import org.mockito.internal.util.io.IOUtil;
import spark.utils.IOUtils;
import java.io.File;
import java.io.OutputStream;*/
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class World implements Serializable {
    public static final int WIDTH = 95;
    public static final int HEIGHT = 50;

    //minimum gap between the edge to the world

    public static final int BORDER_SPACE = 2;
    public static final int THREE = 3;

    static Random random = new Random();

    public static final long INIT_SEED = 9379;
    long seed;
    int width;
    int height;
    
    int[] doorloc;
    int[] youloc;
    private TETile[][] board;
    ArrayList<Room> rooms;
    public static final int ROOM_MAX_HEIGHT = 12;
    public static final int ROOM_MIN_HEIGHT = 7;
    public static final int ROOM_MAX_WIDTH = 20;
    public static final int ROOM_MIN_WIDTH = 7;
    public static final int MAX_NUMBER_OF_ROOMS = 22;
    public static final int MIN_NUMBER_OF_ROOMS = 14;
    int numberOfStars;

    private static final float EXTRA_CONN_PROB_THRESH = (float) 0.5;


    public World(TETile[][] bd, long inseed) {
        //add extra space to make sure it's empty
        this.board = bd; //new TETile[width + 1][height + 1];
        this.width = bd.length - 1;
        this.height = bd[0].length - 2;
        this.seed = inseed;
        random.setSeed(inseed);

        for (int x = 0; x < width + 1; x += 1) {
            for (int y = 0; y < height + 2; y += 1) {
                board[x][y] = Tileset.NOTHING;
            }
        }
        this.rooms = new ArrayList<>();
        for (int i = 0; i < (int) (Math.floor(World.random() * (MAX_NUMBER_OF_ROOMS - MIN_NUMBER_OF_ROOMS)
                + MIN_NUMBER_OF_ROOMS));
             i += 1) {
            createRandomRoom();
        }
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                if (new Position(x, y, board).isBorder()) {
                    board[x][y] = Tileset.WALL;
                }
            }
        }

        //remove unnecessary walls
        this.removeWastedWalls();


        doorloc = addDoors(Tileset.LOCKED_DOOR);

        //add Avatar
        while (true) {
            //ensure the game is not too easy
            youloc = addInsideFeatures(1, Tileset.AVATAR);
            if (Math.abs(youloc[0] - doorloc[0]) >= width / THREE) {
                break;
            } else { //clear existig one
                board[youloc[0]][youloc[1]] = Tileset.FLOOR;
            }
        }

        //add light sources
        addRoomLightSwitch(rooms.size(), Tileset.STAR);

        //add grass
        for (int x = 0; x < width + 1; x += 1) {
            for (int y = 0; y < height + 1; y += 1) {
                if (board[x][y] == Tileset.NOTHING) {
                    board[x][y] = Tileset.GRASS;
                }
            }
        }

        // Add flowers
        this.numberOfStars = random.nextInt(rooms.size() / 2, rooms.size() * 2);
        addRandomFlowers(numberOfStars);
    }

    /**
     * generate brandom int: >= min, < bound + min
     * @param bound
     * @param min
     * @return
     */
    public static int brandom(int bound, int min) {
        return random.nextInt(bound) + min;
    }

    public static double random() {
        return random.nextDouble();
    }


    public TETile[][] getWorld() {
        return board;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void addGrass(int x, int y) {
        board[x][y] = Tileset.GRASS;
    }

    public void createRandomRoom() {
        int proposedWidth = (int) Math.floor(World.random() * (ROOM_MAX_WIDTH - ROOM_MIN_WIDTH) + ROOM_MIN_WIDTH);
        //one example to use psedudo-brandom function
        //int proposedWidth = brandom(roomMaxWidth - roomMinWidth, roomMinWidth);

        int proposedHeight = (int) Math.floor(World.random() * (ROOM_MAX_HEIGHT - ROOM_MIN_HEIGHT) + ROOM_MIN_HEIGHT);
        int startPointX = (int) Math.floor(World.random() * (width - 2 - proposedWidth) + 1);
        int startPointY = (int) Math.floor(World.random() * (height - 2 - proposedHeight) + 1);
        while (!validRoom(startPointX, startPointY, proposedWidth, proposedHeight)) {
            proposedWidth = (int) Math.floor(World.random() * ROOM_MAX_WIDTH);
            proposedHeight = (int) Math.floor(World.random() * ROOM_MAX_HEIGHT);
            startPointX = (int) Math.floor(World.random() * (width - 2 - proposedWidth) + 1);
            startPointY = (int) Math.floor(World.random() * (height - 2 - proposedHeight) + 1);
        }
        Position limit1 = new Position(startPointX, startPointY, board);
        Position limit2 = new Position(startPointX + proposedWidth, startPointY + proposedHeight, board);
        Room newRoom = new Room(limit1, limit2, board);
        Room neighbor = findNearestRoom(newRoom);
        rooms.add(newRoom);
        if (neighbor != null) {
            createHallway(newRoom.getReferencePoint(), neighbor.getReferencePoint());
            //make the world more connected, 40% of times
            if (neighbor != rooms.get(0) && World.random() > EXTRA_CONN_PROB_THRESH) {
                createHallway(rooms.get(0).getReferencePoint(), newRoom.getReferencePoint());
            }
        }
    }

    public Room findNearestRoom(Room room) {
        if (rooms.isEmpty()) {
            return null;
        }
        Room nearestRoom = rooms.get(0);
        double nearestDistance = distanceBetweenRooms(room, rooms.get(0));
        for (int i = 0; i < rooms.size(); i += 1) {
            if (distanceBetweenRooms(room, rooms.get(i)) < nearestDistance) {
                nearestRoom = rooms.get(i);
                nearestDistance = distanceBetweenRooms(room, rooms.get(i));
            }
        }
        return nearestRoom;
    }

    public static double distanceBetweenRooms(Room room1, Room room2) {
        return room1.referencePoint.distanceTo(room2.referencePoint);
    }

    public Hallway createHallway(Position startPoint, Position endPoint) {
        return new Hallway(startPoint, endPoint, board);
    }

    public boolean validRoom(int startPointX, int startPointY, int proposedWidth, int proposedHeight) {
        for (int x = startPointX; x < startPointX + proposedWidth; x += 1) { // Add case when position is beyond board
            for (int y = startPointY; y < startPointY + proposedHeight; y += 1) {
                if (board[x][y] != Tileset.NOTHING) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[] addDoors(TETile door) {
        int[] loc = new int[2];
        int x = 0, y = 0;
        int dcount = 0;
        while (dcount < 1) {
            x = brandom(width - BORDER_SPACE, BORDER_SPACE);
            y = brandom(height - BORDER_SPACE, BORDER_SPACE);

            //door needs to be place in the middle of three wall tiles
            // and one of the side is NOTHING
            if (board[x][y] == Tileset.WALL) {
                if (board[x - 1][y] == Tileset.WALL && board[x + 1][y] == Tileset.WALL
                        && ((board[x][y - 1] == Tileset.FLOOR  && board[x][y + 1] == Tileset.NOTHING)
                        || (board[x][y + 1] == Tileset.FLOOR && board[x][y - 1] == Tileset.NOTHING)
                        || board[x][y - 1] == Tileset.WALL && board[x][y] == Tileset.WALL
                        && ((board[x - 1][y] == Tileset.FLOOR && board[x - 1][y] == Tileset.NOTHING)
                        || board[x + 1][y] == Tileset.FLOOR && board[x + 1][y] == Tileset.NOTHING))) {
                    //door needs to be accessible, next to floor, exit to nothing
                    board[x][y] = door;
                    dcount++;
                }
            }
        }

        loc[0] = x;
        loc[1] = y;
        return loc;
    }

    private void addRoomFeatureShining(Room r, TETile feature, TETile shining) {
        Position center = r.getReferencePoint();
        int cx = center.getX();
        int cy = center.getY();
        //shine the room
        for (int x = center.getX() - 2; x <= cx + 2; x++) {
            for (int y = cy - 2; y <= cy + 2; y++) {
                if (x == cx && y == cy) {
                    board[x][y] = feature;
                } else if (board[x][y] == Tileset.FLOOR) {
                    board[x][y] = shining;
                }
            }
        }
    }

    public void addRoomFeature(Room r, TETile feature) {
        Position center = r.getReferencePoint();
        int x = center.getX();
        int y = center.getY();

        board[x][y] = feature;
    }


    public void addRoomLightSwitch(int num, TETile feature) {

        int count = 0;
        Room r;
        int x, y;
        boolean lighted = false;
        while (count < num && count < rooms.size()) {
            r = rooms.get(count);
            x = r.getReferencePoint().getX();
            y = r.getReferencePoint().getY();
            if (r.height > 2 && r.width > 2
                    && (board[x + 1][y] == Tileset.FLOOR && board[x - 1][y] == Tileset.FLOOR
                    && board[x][y + 1] == Tileset.FLOOR && board[x][y - 1] == Tileset.FLOOR
                    && board[x + 1][y - 1] == Tileset.FLOOR && board[x - 1][y - 1] == Tileset.FLOOR
                    && board[x + 1][y + 1] == Tileset.FLOOR && board[x - 1][y + 1] == Tileset.FLOOR)) {
                if (!lighted) {
                    addRoomFeatureShining(r, Tileset.SHINING_STAR, Tileset.FLOOR_LIGHT);
                    lighted = true;
                } else {
                    addRoomFeature(r, feature);
                }
            }
            count++;
        }
    }

    public int[] addInsideFeatures(int num, TETile feature) {
        int[] loc = new int[2];
        int x = 0, y = 0;
        int dcount = 0;
        while (dcount < num) {
            x = brandom(width - BORDER_SPACE, BORDER_SPACE);
            y = brandom(height - BORDER_SPACE, BORDER_SPACE);

            //door needs to be place in the middle of three wall tiles
            // and one of the side is NOTHING
            if (board[x][y] == Tileset.FLOOR) {
                board[x][y] = feature;
                dcount++;
            }
        }

        loc[0] = x;
        loc[1] = y;
        return loc;
    }

    public void removeWastedWalls() {
        int count = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (board[x][y] == Tileset.WALL) {
                    try {
                        if (board[x + 1][y] == Tileset.FLOOR || board[x - 1][y] == Tileset.FLOOR
                               || board[x][y + 1] == Tileset.FLOOR || board[x][y - 1] == Tileset.FLOOR
                               || board[x + 1][y - 1] == Tileset.FLOOR || board[x - 1][y - 1] == Tileset.FLOOR
                               || board[x + 1][y + 1] == Tileset.FLOOR || board[x - 1][y + 1] == Tileset.FLOOR) {
                            continue;
                        }

                        board[x][y] = Tileset.NOTHING;
                        count++;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        continue;
                    }
                }
            }
        }
        System.out.println(" Removed unnecessary walls: " + count);
    }

    /**
    public static void main(String[] args) {
        long seed = INIT_SEED;
        if (args.length > 2) {
            System.out.println("Can only have two arguments - the flag and input string");
            System.exit(0);
        } else if (args.length == 2 && args[0].equals("-s")) {
            seed = Long.parseLong(args[1]);
            System.out.println("random seed: " + seed);
        }

        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH + 1, HEIGHT);

        TETile[][] bd = new TETile[WIDTH + 1][HEIGHT + 1];

        World world = new World(bd, seed);

        ter.renderFrame(world.getWorld());
    } */

    public void saveWorld(String fileName) {
        String boardString = Arrays.toString(board);
        Out boardOutput = new Out(fileName);
        boardOutput.println(boardString);
    }

    public TETile[][] loadWorld(String fileName) {
        In boardInput = new In(fileName);
        String boardString = boardInput.readAll();
        return null;
    }

    public void addRandomFlowers(int n) {
        for (int i = 0; i < n; i += 1) {
            int proposedX = random.nextInt(0, width - 1);
            int proposedY = random.nextInt(0, height - 1);
            while (board[proposedX][proposedY] != Tileset.FLOOR) {
                proposedX = random.nextInt(0, width - 1);
                proposedY = random.nextInt(0, height - 1);
            }
            board[proposedX][proposedY] = Tileset.FLOWER;
        }
    }

}
