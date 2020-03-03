package net.avdw.behaviourtree.decorator;

import net.avdw.behaviourtree.Node;
import net.avdw.behaviourtree.NodeStatus;

public class RepeaterNode extends DecoratorNode {
    public RepeaterNode(final Node child) {
        super(child);
    }

    @Override
    public NodeStatus tick() {
        NodeStatus nodeStatus = getChild().tick();
        while (nodeStatus.equals(NodeStatus.SUCCESS) || nodeStatus.equals(NodeStatus.RUNNING)) {
            nodeStatus = getChild().tick();
        }

        return nodeStatus;
    }
}
