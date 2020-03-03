package net.avdw.codingame.code4life.laboratory;

import net.avdw.behaviourtree.NodeStatus;
import net.avdw.behaviourtree.leaf.ActionNode;

public class ProduceMedicineAction implements ActionNode {
    @Override
    public NodeStatus tick() {
        return NodeStatus.SUCCESS;
    }
}
