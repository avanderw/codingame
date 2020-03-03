package net.avdw.codingame.code4life;

import net.avdw.codingame.code4life.medicalcomplex.MedicalComplex;
import net.avdw.codingame.code4life.medicalcomplex.MedicalComplexReader;
import net.avdw.codingame.code4life.robotplayer.RobotPlayer;
import net.avdw.codingame.code4life.robotplayer.RobotPlayerReader;
import net.avdw.codingame.code4life.sample.Sample;
import net.avdw.codingame.code4life.sample.SampleReader;
import net.avdw.codingame.code4life.scienceproject.ScienceProject;
import net.avdw.codingame.code4life.scienceproject.ScienceProjectReader;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int projectCount = scanner.nextInt();
        ScienceProjectReader scienceProjectReader = new ScienceProjectReader(scanner);
        for (int i = 0; i < projectCount; i++) {
            ScienceProject scienceProject = scienceProjectReader.nextScienceProject();
            System.err.println(scienceProject);
        }

        // game loop
        RobotPlayerReader robotPlayerReader = new RobotPlayerReader(scanner);
        MedicalComplexReader medicalComplexReader = new MedicalComplexReader(scanner);
        SampleReader sampleReader = new SampleReader(scanner);
        while (true) {
            RobotPlayer myRobotPlayer = robotPlayerReader.nextRobotPlayer();
            System.err.println(myRobotPlayer);
            RobotPlayer otherRobotPlayer = robotPlayerReader.nextRobotPlayer();
            System.err.println(otherRobotPlayer);

            MedicalComplex medicalComplex = medicalComplexReader.nextMedicalComplex();
            System.err.println(medicalComplex);

            int sampleCount = scanner.nextInt();
            for (int i = 0; i < sampleCount; i++) {
                Sample sample = sampleReader.nextSampleData();
                System.err.println(sample);
                switch (sample.carriedBy) {
                    case ME:
                        myRobotPlayer.addSample(sample);
                        break;
                    case OTHER:
                        otherRobotPlayer.addSample(sample);
                        break;
                    case CLOUD:
                        medicalComplex.addSample(sample);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }

            new BehaviourTree(myRobotPlayer).tick();
        }
    }
}
