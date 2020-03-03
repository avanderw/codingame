package net.avdw.codingame.code4life.sample;

import net.avdw.codingame.code4life.molecule.MoleculeType;

import java.util.Scanner;

public class SampleReader {
    private final Scanner scanner;

    public SampleReader(final Scanner scanner) {
        this.scanner = scanner;
    }

    public Sample nextSampleData() {
        int sampleId = scanner.nextInt();
        int carriedBy = scanner.nextInt();
        int rank = scanner.nextInt();
        String expertiseGain = scanner.next();
        int health = scanner.nextInt();
        int costA = scanner.nextInt();
        int costB = scanner.nextInt();
        int costC = scanner.nextInt();
        int costD = scanner.nextInt();
        int costE = scanner.nextInt();

        SampleOwner sampleOwner;
        switch (carriedBy) {
            case 0:
                sampleOwner = SampleOwner.ME;
                break;
            case 1:
                sampleOwner = SampleOwner.OTHER;
                break;
            case -1:
                sampleOwner = SampleOwner.CLOUD;
                break;
            default:
                throw new UnsupportedOperationException();
        }

        Sample sample;
        try {
            sample = new Sample(sampleId, sampleOwner, rank, MoleculeType.valueOf(expertiseGain), health, costA, costB, costC, costD, costE);
        } catch (RuntimeException e) {
            sample = new Sample(sampleId, sampleOwner, rank, MoleculeType.UNKNOWN, health, costA, costB, costC, costD, costE);
        }

        return sample;
    }
}
