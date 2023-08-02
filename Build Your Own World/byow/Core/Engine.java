package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

//import java.io.FileNotFoundException;
//import edu.princeton.cs.algs4.In;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */

    // 95,45 is the maximum to fit on 14.2" laptop
    public static final int WIDTH = 95; //105;
    public static final int HEIGHT = 45; //50;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        Interact it = new Interact(WIDTH, HEIGHT);
        it.showMenu();
        it.handleMenu();
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, running both of these:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        TETile[][] frame = null;
        long seed = 1;
        int index = 1;
        StringBuffer seedbf = new StringBuffer();
        System.out.println(("input: ") + input);
        String upip = input.toUpperCase();
        if (upip.charAt(0) == 'N') {
            while (index < upip.length() && upip.charAt(index) != 'S') {
                seedbf.append(upip.charAt(index));
                index++;
            }
            seed = Long.parseLong(seedbf.toString());
            System.out.println("cmdline seed: " + seed);
            index++; //skip S
            Interact it = new Interact(WIDTH, HEIGHT, seed);
            it.buildWorld();
            String c = "";
            if (index < upip.length()) {
                it.handleActionFromString(upip.substring(index));
                c = upip.substring(upip.length() - 1);
            }

            frame = it.frame;
            //ter.renderFrame(it.frame);
            //it.showHUD(c);
        } else if (upip.charAt(0) == 'L') {
            Interact it = new Interact(WIDTH, HEIGHT);

            if (!it.load()) {
                return frame;
            }

            index++; //skip L
            String c = "";
            if (index < upip.length()) {
                int qi = upip.indexOf(":Q");
                if (qi > 0) {
                    //if there is ":Q" command, ignore action afterwards
                    it.handleActionFromString(upip.substring(index, qi + 2));
                } else {
                    it.handleActionFromString(upip.substring(index));
                }
                c = upip.substring(upip.length() - 1);
            }
            frame = it.frame;
            //ter.renderFrame(it.frame);
            //it.showHUD(c);
        } else if (upip.charAt(0) == 'Q') {
            System.out.println("command: Q");
        }
        return frame;
    }


}
