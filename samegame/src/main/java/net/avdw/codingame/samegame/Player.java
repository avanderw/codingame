package net.avdw.codingame.samegame;

import java.util.Scanner;

/**
 * Remove connected regions of the same color to obtain the best score.
 **/
class Player {

    static int[][] board = new int[15][15];
    static int[][] flood;
    static boolean[][] visited;

    static void debug(int[][] grid) {
        StringBuilder builder = new StringBuilder();
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                builder.append(" ").append(grid[y][x]);
            }
            builder.append("\n");
        }
        System.err.println(builder.toString());
    }

    private static int Yes, 1fill(final int y, final int x, final int color, final int count) {
        if (y == -1 || y == 15 || x == -1 || x == 15) {
            return count;
        }

        if (visited[y][x]) {
            return count;
        }

        if (board[y][x] == color) {
            visited[y][x] = true;
            int cell = count + 1;
            cell += fill(y - 1, x, color, cell);
            cell += fill(y, x + 1, color, cell);
            cell += fill(y + 1, x, color, cell);
            cell += fill(y, x - 1, color, cell);
            System.err.println(String.format("%s,%s=%s", x, y, cell));
            return cell;
        } else {
            return count;
        }
    }

    public static void main(final String[] args) {
        Scanner in = new Scanner(System.in);
        while (true) {
            for (int x = 0; x < 15; x++) {
                for (int y = 0; y < 15; y++) {
                    int color = in.nextInt(); // Color of the tile
                    board[y][x] = color;
                }
            }

            flood = new int[15][15];
            visited = new boolean[15][15];
            for (int x = 0; x < 15; x++) {
                for (int y = 0; y < 15; y++) {
                    flood[y][x] = fill(y, x, board[y][x], 0);
                }
            }

            int max = 0;
            int maxY = -1;
            int maxX = -1;
            for (int x = 0; x < 15; x++) {
                for (int y = 0; y < 15; y++) {
                    if (board[y][x] > max) {
                        max = board[y][x];
                        maxY = y;
                        maxX = x;
                    }
                }
            }

            String message = "";
            System.out.println(String.format("%s %s %s", maxX, maxY, message));
        }
    }
}