package net.avdw.codingame.code4life.molecule;

import net.avdw.behaviourtree.composite.PriorityNode;

public class GatherMoleculesTree extends PriorityNode {
    public GatherMoleculesTree() {
        addChild(new GatherMoleculeAction());
    }
}
