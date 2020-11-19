package net.avdw.codingame;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public final Action action;
    public final List<Node> children = new ArrayList<>();
    public final int depth;
    public final Inventory inv;
    public Node parent;

    public Node(final Inventory inv, final Action action, final int depth) {
        this.inv = inv;
        this.action = action;
        this.depth = depth;
    }

    @Override
    public String toString() {
        return String.format("[%2s] %s %s", depth, action, inv);
    }
}
