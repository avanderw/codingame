package net.avdw.codingame.code4life.movement;

import net.avdw.behaviourtree.composite.SequenceNode;
import net.avdw.codingame.code4life.ModuleType;

public class GoToDiagnosisTree extends SequenceNode {
    public GoToDiagnosisTree() {
        addChild(new GoToModuleAction(ModuleType.DIAGNOSIS));
    }
}
