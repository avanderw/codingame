package net.avdw.codingame.code4life.molecule;

import net.avdw.behaviourtree.NodeStatus;
import net.avdw.behaviourtree.leaf.ActionNode;

public class GatherMoleculeAction implements ActionNode {
    @Override
    public NodeStatus tick() {
        return NodeStatus.SUCCESS;
    }
}
