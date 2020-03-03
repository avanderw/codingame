package net.avdw.codingame.code4life.movement;

import net.avdw.codingame.code4life.ModuleType;

import java.util.HashMap;
import java.util.Map;

public class RobotMovementMatrix {
    private static Map<ModuleType, Map<ModuleType, Integer>> matrix = new HashMap<ModuleType, Map<ModuleType, Integer>>();

    static {
        Map<ModuleType, Integer> startAreaMovementCost = new HashMap<ModuleType, Integer>();
        startAreaMovementCost.put(ModuleType.SAMPLES, 2);
        startAreaMovementCost.put(ModuleType.DIAGNOSIS, 2);
        startAreaMovementCost.put(ModuleType.MOLECULES, 2);
        startAreaMovementCost.put(ModuleType.LABORATORY, 2);
        matrix.put(ModuleType.START_POS, startAreaMovementCost);

        Map<ModuleType, Integer> sampleAreaMovementCost = new HashMap<ModuleType, Integer>();
        sampleAreaMovementCost.put(ModuleType.SAMPLES, 0);
        sampleAreaMovementCost.put(ModuleType.DIAGNOSIS, 3);
        sampleAreaMovementCost.put(ModuleType.MOLECULES, 3);
        sampleAreaMovementCost.put(ModuleType.LABORATORY, 3);
        matrix.put(ModuleType.START_POS, sampleAreaMovementCost);

        Map<ModuleType, Integer> diagnosisAreaMovementCost = new HashMap<ModuleType, Integer>();
        diagnosisAreaMovementCost.put(ModuleType.SAMPLES, 3);
        diagnosisAreaMovementCost.put(ModuleType.DIAGNOSIS, 0);
        diagnosisAreaMovementCost.put(ModuleType.MOLECULES, 3);
        diagnosisAreaMovementCost.put(ModuleType.LABORATORY, 4);
        matrix.put(ModuleType.START_POS, diagnosisAreaMovementCost);

        Map<ModuleType, Integer> moleculeAreaMovementCost = new HashMap<ModuleType, Integer>();
        moleculeAreaMovementCost.put(ModuleType.SAMPLES, 3);
        moleculeAreaMovementCost.put(ModuleType.DIAGNOSIS, 3);
        moleculeAreaMovementCost.put(ModuleType.MOLECULES, 0);
        moleculeAreaMovementCost.put(ModuleType.LABORATORY, 3);
        matrix.put(ModuleType.START_POS, moleculeAreaMovementCost);

        Map<ModuleType, Integer> laboratoryAreaMovementCost = new HashMap<ModuleType, Integer>();
        laboratoryAreaMovementCost.put(ModuleType.SAMPLES, 3);
        laboratoryAreaMovementCost.put(ModuleType.DIAGNOSIS, 4);
        laboratoryAreaMovementCost.put(ModuleType.MOLECULES, 3);
        laboratoryAreaMovementCost.put(ModuleType.LABORATORY, 0);
        matrix.put(ModuleType.START_POS, laboratoryAreaMovementCost);
    }

    public Integer getMovementCost(final ModuleType from, final ModuleType to) {
        return matrix.get(from).get(to);
    }
}
