package net.avdw.codingame;

import java.util.*;

/**
 * Save humans, destroy zombies!
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            int x = in.nextInt();
            int y = in.nextInt();
            int humanCount = in.nextInt();
            for (int i = 0; i < humanCount; i++) {
                int humanId = in.nextInt();
                int humanX = in.nextInt();
                int humanY = in.nextInt();
            }
            int zombieCount = in.nextInt();
            for (int i = 0; i < zombieCount; i++) {
                int zombieId = in.nextInt();
                int zombieX = in.nextInt();
                int zombieY = in.nextInt();
                int zombieXNext = in.nextInt();
                int zombieYNext = in.nextInt();
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println("0 0"); // Your destination coordinates
        }
    }
}