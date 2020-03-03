package net.avdw.codingame.code4life;

import net.avdw.behaviourtree.composite.SequenceNode;
import net.avdw.codingame.code4life.diagnosis.AnalyseSamplesTree;
import net.avdw.codingame.code4life.laboratory.ProduceMedicineTree;
import net.avdw.codingame.code4life.molecule.GatherMoleculesTree;
import net.avdw.codingame.code4life.movement.GoToModuleAction;
import net.avdw.codingame.code4life.movement.MovementTree;
import net.avdw.codingame.code4life.robotplayer.RobotPlayer;
import net.avdw.codingame.code4life.sample.GatherSamplesTree;

public class BehaviourTree extends SequenceNode {
    public BehaviourTree(final RobotPlayer robotPlayer) {
        addChild(new GatherSamplesTree(robotPlayer));
        addChild(new AnalyseSamplesTree());
        addChild(new GatherMoleculesTree());
        addChild(new ProduceMedicineTree());
        addChild(new MovementTree(robotPlayer));
        addChild(new GoToModuleAction(ModuleType.SAMPLES));
    }
}
