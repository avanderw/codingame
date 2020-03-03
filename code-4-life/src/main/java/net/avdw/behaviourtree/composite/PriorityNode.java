package net.avdw.behaviourtree.composite;

import net.avdw.behaviourtree.Node;
import net.avdw.behaviourtree.NodeStatus;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ticks its children sequentially until one of them returns SUCCESS, RUNNING or ERROR.
 * If all children return the failure state, the priority also returns FAILURE
 */
public class PriorityNode extends CompositeNode {
    public PriorityNode() {
        super();
    }

    @Override
    public NodeStatus tick() {
        System.err.println(String.format("%s -> [%s]", this.getClass().getSimpleName(), childrenNames()));
        for (Node child : getChildren()) {
            System.err.println(String.format("%s -> %s", this.getClass().getSimpleName(), child.getClass().getSimpleName()));
            NodeStatus status = child.tick();
            System.err.println(String.format("%s <- %s <- %s", this.getClass().getSimpleName(), child.getClass().getSimpleName(), status));
            switch (status) {
                case SUCCESS:
                case RUNNING:
                    System.err.println(String.format("%s <- %s", this.getClass().getSimpleName(), status));
                    return status;
                case FAILURE:
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
        System.err.println(String.format("%s <- %s", this.getClass().getSimpleName(), NodeStatus.FAILURE));
        return NodeStatus.FAILURE;
    }

    private String childrenNames() {
        return getChildren().stream().map(child->child.getClass().getSimpleName()).collect(Collectors.joining(", "));
    }
}
