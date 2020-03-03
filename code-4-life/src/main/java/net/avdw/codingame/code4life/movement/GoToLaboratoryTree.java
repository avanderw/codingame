package net.avdw.codingame.code4life.movement;

import net.avdw.behaviourtree.composite.SequenceNode;
import net.avdw.codingame.code4life.ModuleType;

public class GoToLaboratoryTree extends SequenceNode {
    public GoToLaboratoryTree() {
        addChild(new GoToModuleAction(ModuleType.LABORATORY));
    }
}
