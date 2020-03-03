package net.avdw.behaviourtree.composite;

import net.avdw.behaviourtree.Node;
import net.avdw.behaviourtree.NodeStatus;
//import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ticks its children sequentially until one of them returns FAILURE, RUNNING or ERROR.
 * If all children return the success state, the sequence also returns SUCCESS
 */
public class SequenceNode extends CompositeNode {
    public SequenceNode() {
    }

    @Override
    public NodeStatus tick() {
        System.err.println(String.format("%s -> [%s]", this.getClass().getSimpleName(), childrenNames()));
        for (Node child : getChildren()) {
            System.err.println(String.format("%s -> %s", this.getClass().getSimpleName(), child.getClass().getSimpleName()));
            NodeStatus status = child.tick();
            System.err.println(String.format("%s <- %s <- %s", this.getClass().getSimpleName(), child.getClass().getSimpleName(), status));
            switch (status) {
                case FAILURE:
                case RUNNING:
                    System.err.println(String.format("%s <- %s", this.getClass().getSimpleName(), status));
                    return status;
                case SUCCESS:
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
        System.err.println(String.format("%s <- %s", this.getClass().getSimpleName(), NodeStatus.SUCCESS));
        return NodeStatus.SUCCESS;
    }

    private String childrenNames() {
        return getChildren().stream().map(child->child.getClass().getSimpleName()).collect(Collectors.joining(", "));
    }
}
