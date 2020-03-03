package net.avdw.codingame.code4life.sample;

import net.avdw.behaviourtree.NodeStatus;
import net.avdw.behaviourtree.leaf.ActionNode;

public class CollectSampleAction implements ActionNode {
    private final Integer rank;

    CollectSampleAction(Integer rank) {
        this.rank = rank;
    }

    @Override
    public NodeStatus tick() {
        System.out.println(String.format("CONNECT %s", rank));
        return NodeStatus.SUCCESS;
    }
}
