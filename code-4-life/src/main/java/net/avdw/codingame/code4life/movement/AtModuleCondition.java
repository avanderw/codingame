package net.avdw.codingame.code4life.movement;

import net.avdw.behaviourtree.Node;
import net.avdw.behaviourtree.NodeStatus;
import net.avdw.codingame.code4life.ModuleType;
import net.avdw.codingame.code4life.robotplayer.RobotPlayer;

public class AtModuleCondition implements Node {
    private final RobotPlayer robotPlayer;
    private final ModuleType moduleType;

    public AtModuleCondition(final RobotPlayer robotPlayer, final ModuleType moduleType) {
        this.robotPlayer = robotPlayer;
        this.moduleType = moduleType;
    }

    @Override
    public NodeStatus tick() {
        if (moduleType.equals(robotPlayer.target) && robotPlayer.eta == 0) {
            return NodeStatus.SUCCESS;
        } else {
            return NodeStatus.FAILURE;
        }
    }
}
