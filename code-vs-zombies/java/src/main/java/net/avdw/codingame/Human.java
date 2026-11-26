package net.avdw.codingame;

public class Human implements Position {
    int id;
    int x;
    int y;

    public Human(int humanId, int humanX, int humanY) {
        id = humanId;
        x = humanX;
        y = humanY;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }
}
