package net.avdw.behaviourtree.decorator;

import net.avdw.behaviourtree.Node;

/**
 * One parent
 * One child
 * Operator
 */
public abstract class DecoratorNode implements Node {
    private final Node child;

    protected DecoratorNode(final Node child) {
        this.child = child;
    }

    public Node getChild() {
        return child;
    }
}
