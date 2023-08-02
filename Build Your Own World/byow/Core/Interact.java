package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.WeightedQuickUnionUF;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import java.io.File;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.util.ArrayList;

/**
 * handle interactivity.
 * Include 1 primary + 2 secondary features for Ambition Score:
 *  1) turn on/off light (primary)
 *  2) real date and time in HUD area
 *  3) the ability to create a new world without closing and
 *     reopening the project, when reach a “game over” state
 * reused portion of codes from lab12
 *
 * @author group g205: Audrey Su, Andhika Tirtawisata
 */
public class Interact {
    //constants, required by style checker too
    static final int HUD_FONT_SIZE = 14;
    static final double HUD_MARGIN = 1.5;
    static final int HUD_LEFT_START = 9;
    static final int HUD_RIGHT_START = 9;

    static final int MENU_WIDTH = 45;
    static final int MENU_HEIGHT = 45;
    static final int MENU_SCALE = 16;
    static final int MENU_FONT_SIZE = 30;
    static final int MENU_Y = 30;
    static final int MENU_LINE_H = 3;
    static final int THREE = 3;
    private static final int SHORT_PAUSE = 300;
    private static final int LONG_PAUSE = 1500;
    public static final int BORDER_SPACE = 1;

    public static final int POINTS_THRESH = 5;

    long seed;
    int width; //world width
    int height; //world height
    TETile[][] frame;
    int[] you; //avatar/you loc
    int[] door;
    int[] mouse; //mouseloc
    TETile youtile = Tileset.FLOOR; //keep old tile replaced by avatar
    StringBuffer actHistory = new StringBuffer();
    static File SAVE = Paths.get(System.getProperty("user.dir"), "proj3save.txt").toFile();

    TERenderer ter = new TERenderer();

    //game states
    boolean done = false;
    boolean goal = false; //achieve goal
    boolean colonflag = false; //keep track colon command

    //Points
    int points;


    public Interact(int w, int h) {
        this(w, h, 1);
    }
    public Interact(int w, int h, long seed) {
        this.seed = seed;
        this.width = w;
        this.height = h;
        this.frame = new TETile[width][height];
        this.mouse = new int[2];
        this.you = new int[2];
        this.points = 0;

        clearForMenu();
    }

    private void clearForMenu() {
        //reuse draw initilization from lab12
        StdDraw.setCanvasSize(MENU_WIDTH * MENU_SCALE, MENU_HEIGHT * MENU_SCALE);
        Font font = new Font("Monaco", Font.BOLD, MENU_FONT_SIZE);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, MENU_WIDTH);
        StdDraw.setYscale(0, MENU_HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
    }

    public static boolean accessible(TETile[][] w, int x, int y) {
        return w[x][y] == Tileset.FLOOR || w[x][y] == Tileset.FLOOR_LIGHT
                || w[x][y] == Tileset.DOLLAR || w[x][y] == Tileset.AVATAR
                || w[x][y] == Tileset.FLOWER;
    }

    private int uid(int x, int y) {
        return x * height + y;
    }

    /**
     * check the world has all connections:
     *  1) Avatar can go to exit door
     *  2) Avatar can go to each room
     * @return
     */
    public boolean isWorldConnected(int[] doorloc, ArrayList<Room> rooms) {
        WeightedQuickUnionUF un = new WeightedQuickUnionUF(width * height);

        for (int x = BORDER_SPACE; x < width - BORDER_SPACE; x++) {
            for (int y = BORDER_SPACE; y < height - BORDER_SPACE; y++) {
                if (accessible(frame, x, y)) {
                    int curid = uid(x, y);
                    //4 directions
                    if (accessible(frame, x + 1, y)) {
                        un.union(curid, uid(x + 1, y));
                    }

                    if (accessible(frame, x, y + 1)) {
                        un.union(curid, uid(x, y + 1));
                    }
                }
            }
        }

        int yx = you[0];
        int yy = you[1];
        int youid = uid(yx, yy);
        int rx, ry, rid;
        //System.out.println(String.format("youloc: (%s, %s), uid: %s", yx, yy, youid));
        for (Room r: rooms) {
            rx = r.referencePoint.getX();
            ry = r.referencePoint.getY();
            if (frame[rx][ry] == Tileset.STAR || frame[rx][ry] == Tileset.SHINING_STAR) {
                rx = rx + 1;
            }
            rid = uid(rx, ry);
            if (!un.connected(youid, rid)) {
                System.out.println("!!! avatar cannot go to MyRoom: " + r);
                //System.out.println("tile: " + world[yx][yy].description() + ", " + world[rx][ry].description());
                //System.out.println(String.format("roomloc: (%s, %s), uid: %s", rx, ry, rid));
                //System.out.println("find youid: " + un.find(youid) + ", rid: " + un.find(rid));
                return false;
            }
        }

        //add exit check
        int x = doorloc[0];
        int y = doorloc[1];
        int curid = uid(x, y);
        //door's 4 directions
        if (accessible(frame, x + 1, y)) {
            un.union(curid, uid(x + 1, y));
        }
        if (accessible(frame, x - 1, y)) {
            un.union(curid, uid(x - 1, y));
        }
        if (accessible(frame, x, y + 1)) {
            un.union(curid, uid(x, y + 1));
        }
        if (accessible(frame, x, y - 1)) {
            un.union(curid, uid(x, y - 1));
        }

        if (!un.connected(uid(you[0], you[1]), uid(doorloc[0], doorloc[1]))) {
            System.out.println("!!! avatar cannot go to door !!!");
            return false;
        }

        return true;
    }

    public TETile[][] buildWorld() {
        ter.initialize(width, height);

        World myworld = new World(frame, seed);
        this.you = myworld.youloc;
        this.door = myworld.doorloc;
        //build frame from World, replace test world
        // fillTestWorld(this.frame);
        if (!isWorldConnected(myworld.doorloc, myworld.rooms)) {
            System.out.println("!The world is not fully connected!");
        }


        ter.renderFrame(this.frame);

        return this.frame;
    }

    /**
     * reused from lab12
     * @param s
     */
    public void drawFrame(String s) {
        /* Take the input string S and display it at the center of the screen,
         * with the pen settings given below. */
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);

        Font fontBig = new Font("Monaco", Font.BOLD, MENU_FONT_SIZE);
        StdDraw.setFont(fontBig);
        StdDraw.text(MENU_WIDTH / 2, MENU_HEIGHT / 2, s);

        StdDraw.show();
    }

    public void showMenu(String s) {
        /* Take the input string S and display it at the center of the screen,
         * with the pen settings given below. */
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);

        Font fontBig = new Font("Monaco", Font.BOLD, MENU_FONT_SIZE);
        StdDraw.setFont(fontBig);
        StdDraw.text(MENU_WIDTH / 2, MENU_HEIGHT - MENU_LINE_H, s);

        StdDraw.text(MENU_WIDTH / 2, MENU_Y, "New Game (n/N)");
        StdDraw.text(MENU_WIDTH / 2, MENU_Y - MENU_LINE_H, "Load Game (l/L)");
        StdDraw.text(MENU_WIDTH / 2, MENU_Y - 2 * MENU_LINE_H, "Quit Game (q/Q)");

        StdDraw.text(MENU_WIDTH / 2, 2, "Goal: collect 5 flowers, then exit");

        StdDraw.show();
    }

    public void showMenu() {
        this.showMenu("Welcome to Proj3 (g205)");
    }

    /**
     * reuse from lab 12
     * @param
     * @return
     */
    public String getSeed() {
        //Read n letters of player input
        StringBuffer sb = new StringBuffer();

        drawFrame("Pl input seed, then letter s");
        StdDraw.pause(LONG_PAUSE);

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'S') {
                    drawFrame("Starting new game ... ");
                    StdDraw.pause(LONG_PAUSE);
                    return sb.toString();
                } else if (Character.isDigit(c)) {
                    sb.append(c);
                    drawFrame(sb.toString());
                    StdDraw.pause(SHORT_PAUSE);
                }
            }
        }
    }

    public void handleMenu() {
        char c = ' ';
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'N') {
                    this.seed = Long.parseLong(getSeed());
                    this.buildWorld();
                    break;

                } else if (c == 'L') {
                    drawFrame("Loading game ... ");
                    StdDraw.pause(LONG_PAUSE);
                    if (!this.load()) {
                        drawFrame("No saved game, exit ...");
                        StdDraw.pause(LONG_PAUSE);
                        System.exit(0);
                    }
                    ter.renderFrame(this.frame);
                    String lastact = (actHistory.length() > 0) ? actHistory.substring(actHistory.length() - 1) : "";
                    this.showHUD(lastact, "Game loaded");
                    StdDraw.pause(LONG_PAUSE);
                    break;
                } else if (c == 'Q') {
                    drawFrame("See you next time");
                    StdDraw.pause(LONG_PAUSE);
                    System.exit(0);
                } else {
                    System.out.println("Unrecognized menu command");
                }
            }
        }

        this.handleAction();
    }

    private int[] to(char action) {
        int[] res = new int[2];

        switch (action) {
            case 'A':
                res[0] = you[0] - 1;
                res[1] = you[1];
                break;
            case 'D':
                res[0] = you[0] + 1;
                res[1] = you[1];
                break;
            case 'W':
                res[0] = you[0];
                res[1] = you[1] + 1;
                break;
            case 'S':
                res[0] = you[0];
                res[1] = you[1] - 1;
                break;
            default:
                res[0] = you[0];
                res[1] = you[1];
        }

        return res;
    }

    private boolean validAction(int[] to) {
        int x = to[0];
        int y = to[1];
        return frame[x][y] == Tileset.FLOOR || frame[x][y] == Tileset.FLOOR_LIGHT
                || frame[x][y] == Tileset.UNLOCKED_DOOR || frame[x][y] == Tileset.DOLLAR
                || frame[x][y] == Tileset.SHINING_STAR || frame[x][y] == Tileset.STAR
                || frame[x][y] == Tileset.FLOWER;
    }

    private void switchLight(int cx, int cy) {
        //shine the room
        boolean on = frame[cx][cy] == Tileset.STAR;

        for (int x = cx - 2; x <= cx + 2; x++) {
            for (int y = cy - 2; y <= cy + 2; y++) {
                if (x == cx && y == cy) {
                    frame[x][y] = on ? Tileset.SHINING_STAR : Tileset.STAR;
                } else if (frame[x][y] == (on ? Tileset.FLOOR : Tileset.FLOOR_LIGHT)) {
                    frame[x][y] = on ? Tileset.FLOOR_LIGHT : Tileset.FLOOR;
                }
            }
        }
    }

    private boolean act(int[] to) {
        boolean res = false;
        int x = to[0];
        int y = to[1];

        if (frame[x][y] == Tileset.UNLOCKED_DOOR) {
            res = true;
        } else if (frame[x][y] == Tileset.STAR || frame[x][y] == Tileset.SHINING_STAR) {
            //switch light
            switchLight(x, y);
            if (youtile == Tileset.FLOOR) {
                youtile = Tileset.FLOOR_LIGHT;
            } else {

                youtile = Tileset.FLOOR;
            }
            return false;
        } else if (frame[x][y] == Tileset.FLOWER) {
            collectFlower(x, y);
        }

        frame[you[0]][you[1]] = youtile;
        youtile = frame[x][y];
        frame[x][y] = Tileset.AVATAR;
        you = to;

        if (points >= POINTS_THRESH && frame[door[0]][door[1]] == Tileset.LOCKED_DOOR) {
            frame[door[0]][door[1]] = Tileset.UNLOCKED_DOOR;
        }

        return res;
    }

    private void clearPlayFlags() {
        this.goal = false;
        this.done = false;
        this.colonflag = false;
        this.points = 0;
    }

    /**
     * helper method to ensure (x,y) is inside our world
     * @param x
     * @param y
     * @return
     */
    private boolean insideWorld(int x, int y) {
        return x < frame.length && x >= 0
                && y < frame[0].length && y >= 0;
    }

    /**
     * handle user action
     */
    public void handleAction() {
        char c = ' ';
        int mx, my;
        while (true) {
            //handle mouse hover-over proj requirement (show tile description)
            mx = (int) StdDraw.mouseX();
            my = (int) StdDraw.mouseY();
            if (insideWorld(mx, my)
                && (mx != mouse[0] || my != mouse[1])) {
                mouse[0] = mx;
                mouse[1] = my;
                showHUD(String.valueOf(c), frame[mx][my].description());
                StdDraw.pause(LONG_PAUSE);
            }

            if (StdDraw.hasNextKeyTyped()) {
                c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'A' || c == 'D' || c == 'S' || c == 'W') {
                    actHistory.append(c);
                    colonflag = false;
                    //4 directions action
                    int[] to = to(c);
                    if (!validAction(to)) {
                        continue; //wait for a valid action
                    }

                    if (act(to)) {
                        //return true means goal is achieved
                        goal = true;
                        done = true;
                        ter.renderFrame(frame);
                        showHUD(String.valueOf(c));
                        break;
                    }

                } else if (c == ':') {
                    //handle command Q
                    //for now, just set goal and done
                    colonflag = true;
                } else if (colonflag && c == 'Q') {
                    save();
                    done = true;

                    break;
                } else {
                    colonflag = false;
                }
            }

            ter.renderFrame(frame);
            showHUD(String.valueOf(c));
            //draw HUD
        }

        if (done) {
            //game is over logic
            System.out.println("Game is over, goal: " + goal);
            System.out.println("actHistory: " + actHistory);
            quit(goal);
            this.showMenu("Want to play again?");
            clearPlayFlags();
            actHistory = new StringBuffer();
            youtile = Tileset.FLOOR;
            handleMenu();
        }
    }

    /**
     * handle same actions from a string
     * @param actions upper-case string passed in
     */
    public void handleActionFromString(String actions) {
        char c;
        for (int i = 0; i < actions.length(); i++) {
            c = actions.charAt(i);
            if (c == 'A' || c == 'D' || c == 'S' || c == 'W' || c == 'C') {
                actHistory.append(c);
                colonflag = false;
                //4 directions action
                int[] to = to(c);
                if (!validAction(to)) {
                    continue; //wait for a valid action
                }

                if (act(to)) {
                    //return true means goal is achieved
                    goal = true;
                    done = true;
                    break;
                }
            } else if (c == ':') {
                //handle command Q
                //for now, just set goal and done
                colonflag = true;
            } else if (colonflag && c == 'Q') {
                save();
                done = true;
                break;
            } else {
                colonflag = false;
            }
        }
    }

    /**
     * Head-up Display
     *  include secondary feature: display of real date and time
     * @param act
     */
    public void showHUD(String act) {
        this.showHUD(act, "");
    }
    public void showHUD(String act, String status) {
        //get real date/time
        DateTimeFormatter df = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        Font hfont = new Font("Newman", Font.BOLD, HUD_FONT_SIZE);
        StdDraw.setFont(hfont);
        StdDraw.setPenColor(Color.yellow);
        StdDraw.text(HUD_LEFT_START, height - HUD_MARGIN, "Flowers: " + points + ", Act: " + act);
        StdDraw.text(width - HUD_RIGHT_START, height - HUD_MARGIN, df.format(now));

        StdDraw.setPenColor(Color.red);

        if (colonflag) {
            StdDraw.text(width / 2 - 2, height - HUD_MARGIN, "type Q to save and quit");
        }  else {
            //String encouragement = ENCOURAGEMENT[you[0] % ENCOURAGEMENT.length];
            StdDraw.text(width / 2 - 2, height - HUD_MARGIN, status);
        }
        StdDraw.show();
    }

    /**
     * we only need to save seed and actionHistory
     */
    public void save() {
        try {
            FileOutputStream fos = new FileOutputStream(SAVE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(seed);
            oos.writeObject(actHistory.toString());

        } catch (IOException e) {
            System.out.println("IO exception: " + e);
        }
    }

    public boolean load() {
        Long lseed = (long) 1;
        String acthistory = "";
        try {
            FileInputStream fis = new FileInputStream(SAVE);
            ObjectInputStream ois = new ObjectInputStream(fis);

            lseed = (Long) ois.readObject();
            acthistory = (String) ois.readObject();

        } catch (IOException e) {
            System.out.println("IO exception: " + e);
            return false;
        } catch (ClassNotFoundException e) {
            System.out.println("class not found exception: " + e);
            return false;
        }

        this.seed = lseed;
        buildWorld();
        handleActionFromString(acthistory);
        return true;
    }

    private void quit(boolean replay) {
        //clear display and go away
        clearForMenu();

        String status = "";
        if (goal) {
            status = "You won!";
        } else {
            status = "Game saved! See ya.";
        }

        drawFrame(status);
        StdDraw.pause(LONG_PAUSE);

    }

    public void collectFlower(int x, int y) {
        if (frame[x][y] == Tileset.FLOWER) {
            frame[x][y] = floorType(x, y); //Tileset.FLOOR;

            points += 1;
            //StdDraw.text(HUD_LEFT_START, height - HUD_MARGIN, "Points: " + points);
        }
    }

    private TETile floorType(int lx, int ly) {
        for (int x = lx - 2; x <= lx + 2; x++) {
            for (int y = ly - 2; y <= ly + 2; y++) {
                try {
                    if (frame[x][y] == Tileset.SHINING_STAR) {
                        return Tileset.FLOOR_LIGHT;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
            }
        }

        return Tileset.FLOOR;
    }
}
