package net.avdw.codingame.code4life.movement;

import net.avdw.behaviourtree.composite.PriorityNode;
import net.avdw.codingame.code4life.robotplayer.RobotPlayer;

public class MovementTree extends PriorityNode {
    public MovementTree(final RobotPlayer robotPlayer) {
        addChild(new GoToSamplesTree(robotPlayer));
        addChild(new GoToDiagnosisTree());
        addChild(new GoToMoleculesTree());
        addChild(new GoToLaboratoryTree());
    }
}
