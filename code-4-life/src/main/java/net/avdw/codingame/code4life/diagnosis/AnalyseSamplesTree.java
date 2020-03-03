package net.avdw.codingame.code4life.diagnosis;

import net.avdw.behaviourtree.composite.PriorityNode;

public class AnalyseSamplesTree extends PriorityNode {
    public AnalyseSamplesTree() {
        addChild(new AnalyseSampleAction());
    }
}
