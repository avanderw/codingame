package net.avdw.codingame.code4life.movement;

import net.avdw.behaviourtree.composite.PriorityNode;
import net.avdw.codingame.code4life.ModuleType;
import net.avdw.codingame.code4life.robotplayer.RobotPlayer;

public class GoToSamplesTree extends PriorityNode {
    public GoToSamplesTree(final RobotPlayer robotPlayer) {
        addChild(new AtModuleCondition(robotPlayer, ModuleType.SAMPLES));
        addChild(new GoToModuleAction(ModuleType.SAMPLES));
    }
}
