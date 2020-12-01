package net.avdw;

public class Zombie {
    public final int id;
    public final int x;
    public final int y;
    public final int xNext;
    public final int yNext;

    public Zombie(final int id, final int x, final int y, final int xNext, final int yNext) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.xNext = xNext;
        this.yNext = yNext;
    }
}
