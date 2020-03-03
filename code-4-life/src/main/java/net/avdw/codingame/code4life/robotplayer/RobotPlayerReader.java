package net.avdw.codingame.code4life.robotplayer;

import net.avdw.codingame.code4life.ModuleType;

import java.util.Scanner;

public class RobotPlayerReader {
    private final Scanner scanner;

    public RobotPlayerReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public RobotPlayer nextRobotPlayer() {
        String target = scanner.next();
        int eta = scanner.nextInt();
        int score = scanner.nextInt();
        int storageA = scanner.nextInt();
        int storageB = scanner.nextInt();
        int storageC = scanner.nextInt();
        int storageD = scanner.nextInt();
        int storageE = scanner.nextInt();
        int expertiseA = scanner.nextInt();
        int expertiseB = scanner.nextInt();
        int expertiseC = scanner.nextInt();
        int expertiseD = scanner.nextInt();
        int expertiseE = scanner.nextInt();

        return new RobotPlayer(ModuleType.valueOf(target), eta, score, storageA, storageB, storageC, storageD, storageE, expertiseA, expertiseB, expertiseC, expertiseD, expertiseE);
    }
}
