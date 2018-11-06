import java.util.*;
import java.io.*;
import java.math.*;
import java.util.stream.IntStream;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Random random = new Random();
        TicTacToe ticTacToeState = new TicTacToe();

        // game loop
        while (true) {
            int opponentY = in.nextInt();
            int opponentX = in.nextInt();
            ticTacToeState.opponentMove(new Cell(opponentX, opponentY));

            int validActionCount = in.nextInt();
            Set<Cell> validCells = new HashSet<>();
            IntStream.range(0, validActionCount).forEach(i->{
                int y = in.nextInt();
                int x = in.nextInt();
                validCells.add(new Cell(x, y));
            });

            PriorityQueue<Cell> cellPriorityQueue = new PriorityQueue<>();
            validCells.forEach(cell -> {
                cell.probability = ticTacToeState.simulateWinProbability(cell);
                cellPriorityQueue.add(cell);
            });

            Cell myCell = cellPriorityQueue.poll();
            ticTacToeState.myMove(myCell);
            System.out.println(String.format("%s %s", myCell.y, myCell.x));
        }
    }

    static class TicTacToe {
        Set<Cell> opponentCells = new HashSet<>();
        Set<Cell> myCells = new HashSet<>();

        void opponentMove(Cell cell) {
            opponentCells.add(cell);
        }

        void myMove(Cell cell) {
            myCells.add(cell);
        }

        boolean isWin(Set<Cell> cells) {
            if (cells.stream().filter(cell -> cell.y == 0).count() == 3) {
                return true;
            }
            if (cells.stream().filter(cell -> cell.y == 1).count() == 3) {
                return true;
            }
            if (cells.stream().filter(cell -> cell.y == 2).count() == 3) {
                return true;
            }
            if (cells.stream().filter(cell -> cell.x == 0).count() == 3) {
                return true;
            }
            if (cells.stream().filter(cell -> cell.x == 1).count() == 3) {
                return true;
            }
            if (cells.stream().filter(cell -> cell.x == 2).count() == 3) {
                return true;
            }
            if (cells.stream().filter(cell -> cell.x == cell.y).count() == 3) {
                return true;
            }
            return cells.stream().filter(cell -> cell.x == 2 - cell.y).count() == 3;
        }

        boolean isMyWin() {
            return isWin(myCells);
        }

        boolean isOpponentWin() {
            return isWin(opponentCells);
        }

        int simulateWinProbability(Cell cell) {
            IntStream.range(0, 1000).forEach(sim->{
                mycells.
            });
            return 0;
        }
    }

    static class Cell implements Comparable<Cell>{
        int x;
        int y;
        int probability;

        Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Cell that) {
            return this.probability - that.probability;
        }
    }
}