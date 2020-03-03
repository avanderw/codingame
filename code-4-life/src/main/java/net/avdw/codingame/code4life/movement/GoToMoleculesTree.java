package net.avdw.codingame.code4life.movement;

import net.avdw.behaviourtree.composite.SequenceNode;
import net.avdw.codingame.code4life.ModuleType;

public class GoToMoleculesTree extends SequenceNode {
    public GoToMoleculesTree() {
        addChild(new GoToModuleAction(ModuleType.MOLECULES));
    }
}
