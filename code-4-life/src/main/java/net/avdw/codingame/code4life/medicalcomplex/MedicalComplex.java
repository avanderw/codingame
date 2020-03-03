package net.avdw.codingame.code4life.medicalcomplex;

import net.avdw.codingame.code4life.molecule.MoleculeType;
import net.avdw.codingame.code4life.sample.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicalComplex {
    public final Map<MoleculeType, Integer> storedMoleculeList = new HashMap<MoleculeType, Integer>();
    public final List<Sample> storedSampleList = new ArrayList<>();

    public MedicalComplex(final int availableA, final int availableB, final int availableC, final int availableD, final int availableE) {
        storedMoleculeList.put(MoleculeType.A, availableA);
        storedMoleculeList.put(MoleculeType.B, availableB);
        storedMoleculeList.put(MoleculeType.C, availableC);
        storedMoleculeList.put(MoleculeType.D, availableD);
        storedMoleculeList.put(MoleculeType.E, availableE);
    }

    @Override
    public String toString() {
        return "MedicalComplex{" +
                "storedMoleculeList=" + storedMoleculeList +
                '}';
    }

    public void addSample(final Sample sample) {
        storedSampleList.add(sample);
    }
}
