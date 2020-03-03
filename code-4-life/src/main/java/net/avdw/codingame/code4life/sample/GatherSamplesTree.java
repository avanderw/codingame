package net.avdw.codingame.code4life.sample;

import net.avdw.behaviourtree.composite.PriorityNode;
import net.avdw.behaviourtree.decorator.InverterNode;
import net.avdw.codingame.code4life.ModuleType;
import net.avdw.codingame.code4life.movement.AtModuleCondition;
import net.avdw.codingame.code4life.robotplayer.RobotPlayer;

public class GatherSamplesTree extends PriorityNode {
    public GatherSamplesTree(final RobotPlayer robotPlayer) {
        addChild(new InverterNode(new AtModuleCondition(robotPlayer, ModuleType.SAMPLES)));
        addChild(new SampleCarryLimitCondition(robotPlayer));
        addChild(new CollectSampleAction(1));
    }
}
