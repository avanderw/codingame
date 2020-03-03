package net.avdw.codingame.code4life.sample;

import net.avdw.codingame.code4life.molecule.MoleculeType;

import java.util.HashMap;
import java.util.Map;

public class Sample {
    public final int sampleId;
    public final SampleOwner carriedBy;
    public final int rank;
    public final MoleculeType expertiseGain;
    public final int health;
    public final Map<MoleculeType, Integer> moleculeCostMap = new HashMap<MoleculeType, Integer>();
    public final SampleState sampleState;

    public Sample(final int sampleId, final SampleOwner carriedBy, final int rank, final MoleculeType expertiseGain, final int health, final int costA, final int costB, final int costC, final int costD, final int costE) {
        this.sampleId = sampleId;
        this.carriedBy = carriedBy;
        this.rank = rank;
        this.expertiseGain = expertiseGain;
        this.health = health;

        moleculeCostMap.put(MoleculeType.A, costA);
        moleculeCostMap.put(MoleculeType.B, costB);
        moleculeCostMap.put(MoleculeType.C, costC);
        moleculeCostMap.put(MoleculeType.D, costD);
        moleculeCostMap.put(MoleculeType.E, costE);

        if (expertiseGain.equals(MoleculeType.UNKNOWN)) {
            sampleState = SampleState.UNDIAGNOSED;
        } else {
            sampleState = SampleState.DIAGNOSED;
        }
    }

    @Override
    public String toString() {
        return "SampleData{" +
                "sampleId=" + sampleId +
                ", carriedBy=" + carriedBy +
                ", rank=" + rank +
                ", expertiseGain=" + expertiseGain +
                ", health=" + health +
                ", sampleDataState=" + sampleState +
                ", moleculeCostMap=" + moleculeCostMap +
                '}';
    }
}
