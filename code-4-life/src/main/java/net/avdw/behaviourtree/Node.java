package net.avdw.behaviourtree;

@FunctionalInterface
public interface Node {
    NodeStatus tick();
}
