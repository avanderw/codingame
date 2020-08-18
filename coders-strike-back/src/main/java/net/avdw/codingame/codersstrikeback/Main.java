package net.avdw.codingame.codersstrikeback;

import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void main(String[] args) {
        int boostCount = 1;
        Scanner in = new Scanner(System.in);

        int prevX = -1, prevY = -1;
        // game loop
        while (true) {
            int x = in.nextInt();
            int y = in.nextInt();
            int nextCheckpointX = in.nextInt(); // x position of the next check point
            int nextCheckpointY = in.nextInt(); // y position of the next check point
            int nextCheckpointDist = in.nextInt(); // distance to the next checkpoint
            int nextCheckpointAngle = in.nextInt(); // angle between your pod orientation and the direction of the next checkpoint
            int opponentX = in.nextInt();
            int opponentY = in.nextInt();

            int k = 3;
            double maxThrust = 100;
            double checkpointRadius = 600d;

            if (prevX == -1) {
                prevX = x;
                prevY = y;
            }

            // thrust
            double angleModifier = clamp(1 - (Math.abs(nextCheckpointAngle) / 180d), 0, 1);
            double distanceModifier = clamp(nextCheckpointDist / (k * checkpointRadius), 0, 1);
            double thrust = maxThrust * angleModifier * distanceModifier;

            // boost
            boolean boost;
            if (nextCheckpointDist < 1200) {
                boost = false;
            } else {
                boost = nextCheckpointDist > 3600 && thrust > 90 && boostCount != 0;
            }

            // target (counteract inertia)
            int myDiffX = x - prevX;
            int myDiffY = y - prevY;
            int targetX = nextCheckpointX - myDiffX;
            int targetY = nextCheckpointY - myDiffY;

            // You have to output the target position
            // followed by the power (0 <= thrust <= 100)
            // i.e.: "x y thrust"

            if (boost) {
                boostCount--;
                System.out.println(nextCheckpointX + " " + nextCheckpointY + " BOOST");
            } else {
                System.out.println(String.format("%s %s %.0f", targetX, targetY, thrust));
            }
            prevX = x;
            prevY = y;
        }
    }
}