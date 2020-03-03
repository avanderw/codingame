package net.avdw.behaviourtree.decorator;

import net.avdw.behaviourtree.Node;
import net.avdw.behaviourtree.NodeStatus;

public class InverterNode extends DecoratorNode {
    public InverterNode(final Node child) {
        super(child);
    }

    @Override
    public NodeStatus tick() {
        NodeStatus nodeStatus = getChild().tick();
        switch (nodeStatus) {
            case SUCCESS:
                return NodeStatus.FAILURE;
            case FAILURE:
                return NodeStatus.SUCCESS;
            case RUNNING:
                return NodeStatus.RUNNING;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
