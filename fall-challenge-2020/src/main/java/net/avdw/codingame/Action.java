package net.avdw.codingame;

import java.util.ArrayList;
import java.util.List;

public class Action {
    public final boolean castable;
    public final int delta0;
    public final int delta1;
    public final int delta2;
    public final int delta3;
    public final int id;
    public final List<Node> path = new ArrayList<>();
    public final int price;
    public final boolean repeatable;
    public final int taxCount;
    public final int tomeIndex;
    public final ActionType type;
    public boolean pathFound;

    public Action(final Action that, final boolean castable) {
        this.castable = castable;
        this.delta0 = that.delta0;
        this.delta1 = that.delta1;
        this.delta2 = that.delta2;
        this.delta3 = that.delta3;
        this.id = that.id;
        this.price = that.price;
        this.repeatable = that.repeatable;
        this.taxCount = that.taxCount;
        this.tomeIndex = that.tomeIndex;
        this.type = that.type;
    }

    public Action(final int id, final ActionType type, final int delta0, final int delta1, final int delta2, final int delta3, final int price, final int tomeIndex, final int taxCount, final boolean castable, final boolean repeatable) {
        this.id = id;
        this.type = type;
        this.delta0 = delta0;
        this.delta1 = delta1;
        this.delta2 = delta2;
        this.delta3 = delta3;
        this.price = price;
        this.tomeIndex = tomeIndex;
        this.taxCount = taxCount;
        this.castable = castable;
        this.repeatable = repeatable;
    }

    @Override
    public String toString() {
        return String.format("[%2s] %4s-%-2s{%2s,%2s,%2s,%2s,castable=%-5s}", price, type, id, delta0, delta1, delta2, delta3, castable);
    }
}
