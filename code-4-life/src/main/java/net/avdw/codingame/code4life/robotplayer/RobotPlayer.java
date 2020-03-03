package net.avdw.codingame.code4life.robotplayer;

import net.avdw.codingame.code4life.ModuleType;
import net.avdw.codingame.code4life.molecule.MoleculeType;
import net.avdw.codingame.code4life.sample.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RobotPlayer {
    public final ModuleType target;
    public final int eta;
    public final int score;
    public final Map<MoleculeType, Integer> storedMoleculeMap = new HashMap<MoleculeType, Integer>();
    public final Map<MoleculeType, Integer> expertiseMoleculeMap = new HashMap<MoleculeType, Integer>();
    public final List<Sample> storedSampleList = new ArrayList<>();

    public RobotPlayer(final ModuleType target, final int eta, final int score, final int storageA, final int storageB, final int storageC, final int storageD, final int storageE, final int expertiseA, final int expertiseB, final int expertiseC, final int expertiseD, final int expertiseE) {
        this.target = target;
        this.eta = eta;
        this.score = score;

        storedMoleculeMap.put(MoleculeType.A, storageA);
        storedMoleculeMap.put(MoleculeType.B, storageB);
        storedMoleculeMap.put(MoleculeType.C, storageC);
        storedMoleculeMap.put(MoleculeType.D, storageD);
        storedMoleculeMap.put(MoleculeType.E, storageE);

        expertiseMoleculeMap.put(MoleculeType.A, expertiseA);
        expertiseMoleculeMap.put(MoleculeType.B, expertiseB);
        expertiseMoleculeMap.put(MoleculeType.C, expertiseC);
        expertiseMoleculeMap.put(MoleculeType.D, expertiseD);
        expertiseMoleculeMap.put(MoleculeType.E, expertiseE);
    }

    @Override
    public String toString() {
        return "RobotPlayer{" +
                "target=" + target +
                ", eta=" + eta +
                ", score=" + score +
                ", storedMoleculeMap=" + storedMoleculeMap +
                ", expertiseMoleculeMap=" + expertiseMoleculeMap +
                '}';
    }

    public void addSample(final Sample sample) {
        storedSampleList.add(sample);
    }
}
