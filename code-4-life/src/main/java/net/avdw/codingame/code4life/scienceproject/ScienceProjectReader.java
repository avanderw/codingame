package net.avdw.codingame.code4life.scienceproject;

import java.util.Scanner;

public class ScienceProjectReader {
    private final Scanner scanner;

    public ScienceProjectReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public ScienceProject nextScienceProject() {
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        int c = scanner.nextInt();
        int d = scanner.nextInt();
        int e = scanner.nextInt();
        return new ScienceProject(a, b, c, d, e);
    }
}
