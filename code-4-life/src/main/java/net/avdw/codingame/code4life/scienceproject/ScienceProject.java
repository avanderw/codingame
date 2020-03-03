package net.avdw.codingame.code4life.scienceproject;

import net.avdw.codingame.code4life.molecule.MoleculeType;

import java.util.HashMap;
import java.util.Map;

public class ScienceProject {
    public final Map<MoleculeType, Integer> moleculeTypeCost = new HashMap<MoleculeType, Integer>();

    public ScienceProject(final int a, final int b, final int c, final int d, final int e) {
        moleculeTypeCost.put(MoleculeType.A, a);
        moleculeTypeCost.put(MoleculeType.B, b);
        moleculeTypeCost.put(MoleculeType.C, c);
        moleculeTypeCost.put(MoleculeType.D, d);
        moleculeTypeCost.put(MoleculeType.E, e);
    }

    @Override
    public String toString() {
        return "ScienceProject{" +
                "moleculeTypeCost=" + moleculeTypeCost +
                '}';
    }
}
