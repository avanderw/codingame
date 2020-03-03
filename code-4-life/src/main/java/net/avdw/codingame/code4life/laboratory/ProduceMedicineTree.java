package net.avdw.codingame.code4life.laboratory;

import net.avdw.behaviourtree.composite.PriorityNode;

public class ProduceMedicineTree extends PriorityNode {
    public ProduceMedicineTree() {
        addChild(new ProduceMedicineAction());
    }
}
