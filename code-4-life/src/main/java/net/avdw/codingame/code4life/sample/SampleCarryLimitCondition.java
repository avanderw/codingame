package net.avdw.codingame.code4life.sample;

import net.avdw.behaviourtree.NodeStatus;
import net.avdw.behaviourtree.leaf.ConditionNode;
import net.avdw.codingame.code4life.robotplayer.RobotPlayer;

public class SampleCarryLimitCondition implements ConditionNode {
    public static final int CARRY_LIMIT = 3;
    private final RobotPlayer robotPlayer;

    SampleCarryLimitCondition(final RobotPlayer robotPlayer) {
        this.robotPlayer = robotPlayer;
    }


    @Override
    public NodeStatus tick() {
        if (robotPlayer.storedSampleList.size() == CARRY_LIMIT) {
            return NodeStatus.SUCCESS;
        } else {
            return NodeStatus.FAILURE;
        }

    }
}
