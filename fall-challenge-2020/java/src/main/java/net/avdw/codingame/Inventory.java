package net.avdw.codingame;

public class Inventory {
    public final int inv0;
    public final int inv1;
    public final int inv2;
    public final int inv3;
    public final int score;

    public Inventory(final Inventory inventory) {
        this.inv0 = inventory.inv0;
        this.inv1 = inventory.inv1;
        this.inv2 = inventory.inv2;
        this.inv3 = inventory.inv3;
        this.score = inventory.score;
    }

    public Inventory(final int inv0, final int inv1, final int inv2, final int inv3, final int score) {
        this.inv0 = inv0;
        this.inv1 = inv1;
        this.inv2 = inv2;
        this.inv3 = inv3;
        this.score = score;
    }

    @Override
    public String toString() {
        return String.format("[%2s]<%s,%s,%s,%s>", score,inv0,inv1,inv2,inv3);
    }

    public boolean valid() {
        boolean inv0valid = inv0 >= 0;
        boolean inv1valid = inv1 >= 0;
        boolean inv2valid = inv2 >= 0;
        boolean inv3valid = inv3 >= 0;
        return inv0valid && inv1valid && inv2valid && inv3valid;
    }
}
