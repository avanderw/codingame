package net.avdw;

import java.util.LinkedList;
import java.util.List;

public class Strategy {
    public final List<Target> targetList = new LinkedList<>();
    public long score;
    public Target nextTarget;

    public Target nextTarget() {
        return nextTarget; // = targetList.remove(0);
    }

    public void add(final Target target) {
        targetList.add(target);
    }
}
