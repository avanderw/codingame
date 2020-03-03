package net.avdw.codingame.code4life.movement;

import net.avdw.behaviourtree.NodeStatus;
import net.avdw.behaviourtree.leaf.ActionNode;
import net.avdw.codingame.code4life.ModuleType;

public class GoToModuleAction implements ActionNode {
    private final ModuleType moduleType;

    public GoToModuleAction(final ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    @Override
    public NodeStatus tick() {
        System.out.println(String.format("GOTO %s", moduleType));
        return NodeStatus.SUCCESS;
    }
}
