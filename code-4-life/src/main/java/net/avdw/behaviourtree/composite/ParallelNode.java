package net.avdw.behaviourtree.composite;

import net.avdw.behaviourtree.NodeStatus;

import java.util.ArrayList;

/**  ticks all children at the same time, allowing them to work in parallel. */
public class ParallelNode extends CompositeNode {
    public ParallelNode() {
    }

    @Override
    public NodeStatus tick() {
        throw new UnsupportedOperationException();
    }
}
