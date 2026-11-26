package net.avdw.codingame;

public class Zombie implements Position {
    int id;
    int x1;
    int y1;
    int x2;
    int y2;

    public Zombie(int zombieId, int zombieX, int zombieY, int zombieXNext, int zombieYNext) {
        id = zombieId;
        x1 = zombieX;
        y1 = zombieY;
        x2 = zombieXNext;
        y2 = zombieYNext;
    }

    @Override
    public int getX() {
        return x1;
    }

    @Override
    public int getY() {
        return y1;
    }
}
