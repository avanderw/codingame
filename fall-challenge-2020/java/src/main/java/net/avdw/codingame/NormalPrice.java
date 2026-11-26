package net.avdw.codingame;

public class NormalPrice {
    public static double calculate(final Action brew) {
        if (brew.pathFound) {
            return brew.price * 1. / brew.path.size();
        } else {
            return 0.0;
        }
    }
}
