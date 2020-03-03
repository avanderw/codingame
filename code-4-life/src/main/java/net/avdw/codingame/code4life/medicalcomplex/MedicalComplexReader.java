package net.avdw.codingame.code4life.medicalcomplex;

import java.util.Scanner;

public class MedicalComplexReader {
    private final Scanner scanner;

    public MedicalComplexReader(final Scanner scanner) {
        this.scanner = scanner;
    }

    public MedicalComplex nextMedicalComplex() {
        int availableA = scanner.nextInt();
        int availableB = scanner.nextInt();
        int availableC = scanner.nextInt();
        int availableD = scanner.nextInt();
        int availableE = scanner.nextInt();
        return new MedicalComplex(availableA, availableB, availableC, availableD, availableE);
    }
}
