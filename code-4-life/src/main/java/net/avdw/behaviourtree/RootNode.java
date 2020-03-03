package net.avdw.behaviourtree;

/**
 * no parent
 * one child
 * ticks
 */
public class RootNode implements Node {
    private final Node child;

    public RootNode(final Node child) {
        this.child = child;
    }

    @Override
    public NodeStatus tick() {
        return child.tick();
    }
}
