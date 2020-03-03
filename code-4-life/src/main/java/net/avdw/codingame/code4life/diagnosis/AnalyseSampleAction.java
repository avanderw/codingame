package net.avdw.codingame.code4life.diagnosis;

import net.avdw.behaviourtree.NodeStatus;
import net.avdw.behaviourtree.leaf.ActionNode;

public class AnalyseSampleAction implements ActionNode {
    @Override
    public NodeStatus tick() {
        return NodeStatus.SUCCESS;
    }
}
