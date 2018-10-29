import javafx.scene.layout.Priority;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Bring dataMap on patient samples from the diagnosis machine to the laboratory with enough moleculeBag to produce medicine!
 **/
class Player {

    public static void main(String args[]) {
        Integer MOLECULE_QUICK_WIN = 2;

        GameState gameState = new GameState();
        Scanner in = new Scanner(System.in);

        List<Project> projects = new ArrayList<>();
        int projectCount = in.nextInt();
        for (int i = 0; i < projectCount; i++) {
            int a = in.nextInt();
            int b = in.nextInt();
            int c = in.nextInt();
            int d = in.nextInt();
            int e = in.nextInt();

            Project project = new Project();
            project.requiredA = a;
            project.requiredB = b;
            project.requiredC = c;
            project.requiredD = d;
            project.requiredE = e;
            project.isComplete = Boolean.FALSE;
            project.completedBy = "N";
            projects.add(project);
        }
        gameState.projects = projects;

        ABehaviourTree<GameState> behaviourTree = new ABehaviourTree.Sequence<GameState>("Code4Life",
                new UpdateBlackboard(in),
                new ABehaviourTree.Selector("TakeAction",
                        new ABehaviourTree.Sequence("Moving",
                                new ABehaviourTree.Guard("isMoving", bb -> bb.player.eta > 0),
                                new ABehaviourTree.Execute("ExecuteWait", bb -> System.out.println("WAIT"))
                        ),
                        new ABehaviourTree.Sequence("StartPosition",
                                new ABehaviourTree.Guard("isAtStart", bb -> bb.player.target.equals("START_POS")),
                                new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO SAMPLES"))
                        ),
                        new ABehaviourTree.Sequence("AtSamples",
                                new ABehaviourTree.Guard("isAtSamples", bb -> bb.player.target.equals("SAMPLES")),
                                new ABehaviourTree.Selector("SelectSamplesAction",
                                        new ABehaviourTree.Sequence("CollectToMaxSamples",
                                                new ABehaviourTree.Guard("ifNotCarryingMaxSamples", bb -> bb.player.samples.size() < 3),
                                                new ABehaviourTree.Selector<>("IdentifySample",
                                                        new ABehaviourTree.Sequence("Rank3",
                                                                new ABehaviourTree.Selector("Rank3Conditions",
                                                                        new ABehaviourTree.Guard("hasCompleted2Projects", bb -> bb.projects.stream()
                                                                                .filter(p -> p.isComplete)
                                                                                .filter(p -> p.completedBy.equals("P"))
                                                                                .count() > 1
                                                                        ),
                                                                        new ABehaviourTree.Guard("enoughExpertiseForRank3", bb -> 2 <=
                                                                                (bb.player.expertiseA >= 3 ? 1 : 0) +
                                                                                        (bb.player.expertiseB >= 3 ? 1 : 0) +
                                                                                        (bb.player.expertiseC >= 3 ? 1 : 0) +
                                                                                        (bb.player.expertiseD >= 3 ? 1 : 0) +
                                                                                        (bb.player.expertiseE >= 3 ? 1 : 0)
                                                                        ),
                                                                        new ABehaviourTree.Guard("has2Rank2Samples", bb -> bb.player.samples.stream().filter(s -> s.rank == 2).count() == 2)
                                                                ),
                                                                new ABehaviourTree.Guard("hasRank2Sample", bb -> bb.player.samples.stream().anyMatch(s -> s.rank == 2)),
                                                                new ABehaviourTree.Store("<rank-id>", bb -> Optional.of("3"))
                                                        ),
                                                        new ABehaviourTree.Sequence("Rank2",
                                                                new ABehaviourTree.Selector("Rank2Conditions",
                                                                        new ABehaviourTree.Guard("hasCompletedAProject", bb -> bb.projects.stream()
                                                                                .filter(p -> p.isComplete).anyMatch(p -> p.completedBy.equals("P"))
                                                                        ),
                                                                        new ABehaviourTree.Guard("enoughExpertiseForRank2", bb -> 2 <=
                                                                                (bb.player.expertiseA >= 2 ? 1 : 0) +
                                                                                        (bb.player.expertiseB >= 2 ? 1 : 0) +
                                                                                        (bb.player.expertiseC >= 2 ? 1 : 0) +
                                                                                        (bb.player.expertiseD >= 2 ? 1 : 0) +
                                                                                        (bb.player.expertiseE >= 2 ? 1 : 0)
                                                                        ),
                                                                        new ABehaviourTree.Guard("has2Rank1Samples", bb -> bb.player.samples.stream().filter(s -> s.rank == 1).count() == 2)
                                                                ),
                                                                new ABehaviourTree.Store("<rank-id>", bb -> Optional.of("2"))
                                                        ),
                                                        new ABehaviourTree.Sequence("Rank1",
                                                                new ABehaviourTree.Store("<rank-id>", bb -> Optional.of("1"))
                                                        )
                                                ),
                                                new ABehaviourTree.Guard("isSampleIdentified", bb -> bb.dataMap.containsKey("<rank-id>") && bb.dataMap.get("<rank-id>") != null),
                                                new ABehaviourTree.Execute("CollectSample", bb -> System.out.println(String.format("CONNECT %s", bb.dataMap.get("<rank-id>"))))
                                        ),
                                        new ABehaviourTree.Selector("LeaveSamples",
                                                new ABehaviourTree.Sequence("GoToDiagnosis",
                                                        new ABehaviourTree.Guard("isCarryingUndiagnosedSamples", bb -> bb.player.samples.stream().anyMatch(sample -> sample.cost() < 0)),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO DIAGNOSIS"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToMolecules",
                                                        new ABehaviourTree.Guard("doesAnySampleNeedMolecules", bb -> bb.player.samples.stream().anyMatch(s -> !s.isComplete())),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO MOLECULES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToLaboratory",
                                                        new ABehaviourTree.Guard("ifThereAreCompleteSamples", bb -> bb.player.samples.stream().anyMatch(Sample::isComplete)),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO LABORATORY"))
                                                )
                                        )
                                )
                        ),
                        new ABehaviourTree.Sequence("AtDiagnosis",
                                new ABehaviourTree.Guard("isAtDiagnosis", bb -> bb.player.target.equals("DIAGNOSIS")),
                                new ABehaviourTree.Selector("SelectDiagnosisAction",
                                        new ABehaviourTree.Sequence("DiagnoseSamples",
                                                new ABehaviourTree.Guard("isCarryingUndiagnosedSamples", bb -> bb.player.samples.stream().anyMatch(sample -> sample.cost() < 0)),
                                                new ABehaviourTree.Store("<sample-id>", bb -> bb.player.samples.stream().filter(s -> s.cost() < 0).map(s -> s.id).findAny()),
                                                new ABehaviourTree.Guard("isSampleIdentified", bb -> bb.dataMap.containsKey("<sample-id>") && bb.dataMap.get("<sample-id>") != null),
                                                new ABehaviourTree.Execute("UploadSample", bb -> System.out.println(String.format("CONNECT %s", bb.dataMap.get("<sample-id>"))))
                                        ),
                                        new ABehaviourTree.Sequence("UploadImpossibleSamples", /// TODO account for moleculeBag in hand
                                                new ABehaviourTree.Selector("SelectSamples",
                                                        new ABehaviourTree.Store("<sample-id>", bb -> bb.player.samples.stream()
                                                                .filter(s -> !s.isComplete())
                                                                .filter(s -> !s.canAfford())
                                                                .map(s -> s.id).findAny()
                                                        )
                                                ),
                                                new ABehaviourTree.Guard("isSampleIdentified", bb -> bb.dataMap.containsKey("<sample-id>") && bb.dataMap.get("<sample-id>") != null),
                                                new ABehaviourTree.Execute("UploadSample", bb -> System.out.println(String.format("CONNECT %s", bb.dataMap.get("<sample-id>"))))
                                        ),
                                        new ABehaviourTree.Sequence("UploadSamplesNotContributingToProjects",
                                                new ABehaviourTree.Store("<sample-id>", bb -> bb.player.samples.stream()
                                                        .filter(s -> {
                                                            if (s.remaining() >= MOLECULE_QUICK_WIN && bb.projects.stream().filter(p -> p.isComplete).count() < 1) {
                                                                switch (s.expertiseGain) {
                                                                    case "A":
                                                                        return s.remainingA() > 0 && bb.projects.stream()
                                                                                .filter(p -> !p.isComplete).noneMatch(p -> p.requiredA > bb.player.expertiseA);
                                                                    case "B":
                                                                        return s.remainingB() > 0 && bb.projects.stream()
                                                                                .filter(p -> !p.isComplete).noneMatch(p -> p.requiredB > bb.player.expertiseB);
                                                                    case "C":
                                                                        return s.remainingC() > 0 && bb.projects.stream()
                                                                                .filter(p -> !p.isComplete).noneMatch(p -> p.requiredC > bb.player.expertiseC);
                                                                    case "D":
                                                                        return s.remainingD() > 0 && bb.projects.stream()
                                                                                .filter(p -> !p.isComplete).noneMatch(p -> p.requiredD > bb.player.expertiseD);
                                                                    case "E":
                                                                        return s.remainingE() > 0 && bb.projects.stream()
                                                                                .filter(p -> !p.isComplete).noneMatch(p -> p.requiredE > bb.player.expertiseE);
                                                                }
                                                            }
                                                            return Boolean.FALSE;
                                                        })
                                                        .map(s -> s.id).findAny()),
                                                new ABehaviourTree.Guard("isSampleIdentified", bb -> bb.dataMap.containsKey("<sample-id>") && bb.dataMap.get("<sample-id>") != null),
                                                new ABehaviourTree.Execute("UploadSample", bb -> System.out.println(String.format("CONNECT %s", bb.dataMap.get("<sample-id>"))))
                                        ),
                                        new ABehaviourTree.Sequence("UploadAllSamplesIfCannotReleaseMolecules",
                                                new ABehaviourTree.Guard("isMoleculeStorageFull", bb -> bb.player.storage == 10),
                                                new ABehaviourTree.Guard("canSamplesBeCompletedWithStoredMolecules", bb -> bb.player.samples.stream().noneMatch(Sample::isComplete)),
                                                new ABehaviourTree.Store("<sample-id>", bb -> bb.player.samples.stream().findAny()),
                                                new ABehaviourTree.Guard("isSampleIdentified", bb -> bb.dataMap.containsKey("<sample-id>") && bb.dataMap.get("<sample-id>") != null),
                                                new ABehaviourTree.Execute("UploadSample", bb -> System.out.println(String.format("CONNECT %s", ((Sample) bb.dataMap.get("<sample-id>")).id)))
                                        ),
                                        new ABehaviourTree.Sequence("DownloadQuickWinSamples",
                                                new ABehaviourTree.Guard("isSpaceForSamples", bb -> bb.player.samples.size() < 3),
                                                new ABehaviourTree.Store("<sample-id>", bb -> bb.cloud.stream()
                                                        .sorted(Comparator.<Sample>comparingInt(s -> s.rank).reversed())
                                                        .filter(s -> s.remaining() < MOLECULE_QUICK_WIN)
                                                        .map(s -> s.id).findAny()
                                                ),
                                                new ABehaviourTree.Guard("isSampleIdentified", bb -> bb.dataMap.containsKey("<sample-id>") && bb.dataMap.get("<sample-id>") != null),
                                                new ABehaviourTree.Execute("UploadSample", bb -> System.out.println(String.format("CONNECT %s", bb.dataMap.get("<sample-id>"))))
                                        ),
                                        new ABehaviourTree.Selector("LeaveDiagnosis",
                                                new ABehaviourTree.Sequence("GoToSamples",
                                                        new ABehaviourTree.Guard("notCarryingSamples", bb -> bb.player.samples.isEmpty()),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO SAMPLES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToLaboratory",
                                                        new ABehaviourTree.Guard("ifThereAreCompleteSamples", bb -> bb.player.samples.stream().anyMatch(Sample::isComplete)),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO LABORATORY"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToMolecules",
                                                        new ABehaviourTree.Guard("doesAnySampleNeedMolecules", bb -> bb.player.samples.stream().anyMatch(s -> !s.isComplete())),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO MOLECULES"))
                                                )
                                        )
                                )
                        ),

                        new ABehaviourTree.Sequence("AtMolecules",
                                new ABehaviourTree.Guard("isAtMolecules", bb -> bb.player.target.equals("MOLECULES")),
                                new ABehaviourTree.Selector("SelectMoleculesAction",
                                        new ABehaviourTree.Sequence("CollectMolecules",
                                                new ABehaviourTree.Guard("isThereSpaceForMolecules", bb -> bb.player.storage < 10),
                                                new ABehaviourTree.Guard("doesAnySampleNeedMolecules", bb -> bb.player.samples.stream().anyMatch(s -> s.remaining() > 0)),
                                                new ABehaviourTree.UntilFail<>("EmptyStack", new ABehaviourTree.Pop()),
                                                new ABehaviourTree.UntilFail<>("AddPlayerSamplesNeedingMoleculesToStack",
                                                        new ABehaviourTree.Push(bb -> bb.player.samples.stream()
                                                                .filter(sample -> sample.remaining() > 0)
                                                                .filter(sample -> !bb.stack.contains(sample))
                                                                .max(Comparator.comparingInt(Sample::remaining))
                                                        )
                                                ),
                                                new ABehaviourTree.UntilFail<>("PopSamplesUntilFail",
                                                        new ABehaviourTree.Invert<>("InvertIdentify",
                                                                new ABehaviourTree.Selector<>("IdentifyMolecules",
                                                                        new ABehaviourTree.Guard("IsSampleStackEmpty", bb -> bb.stack.isEmpty()),
                                                                        new ABehaviourTree.Sequence("CheckA",
                                                                                new ABehaviourTree.Guard("doesSampleRequireA", bb -> ((Sample) bb.stack.peek()).remainingA() > 0),
                                                                                new ABehaviourTree.Guard("isAAvailable", bb -> bb.availableA > 0),
                                                                                new ABehaviourTree.Store("<molecule-id>", (bb) -> Optional.of("A"))
                                                                        ),
                                                                        new ABehaviourTree.Sequence("CheckB",
                                                                                new ABehaviourTree.Guard("doesSampleRequireB", bb -> ((Sample) bb.stack.peek()).remainingB() > 0),
                                                                                new ABehaviourTree.Guard("isBAvailable", bb -> bb.availableB > 0),
                                                                                new ABehaviourTree.Store("<molecule-id>", (bb) -> Optional.of("B"))
                                                                        ),
                                                                        new ABehaviourTree.Sequence("CheckC",
                                                                                new ABehaviourTree.Guard("doesSampleRequireC", bb -> ((Sample) bb.stack.peek()).remainingC() > 0),
                                                                                new ABehaviourTree.Guard("isCAvailable", bb -> bb.availableC > 0),
                                                                                new ABehaviourTree.Store("<molecule-id>", (bb) -> Optional.of("C"))
                                                                        ),
                                                                        new ABehaviourTree.Sequence("CheckD",
                                                                                new ABehaviourTree.Guard("doesSampleRequireD", bb -> ((Sample) bb.stack.peek()).remainingD() > 0),
                                                                                new ABehaviourTree.Guard("isDAvailable", bb -> bb.availableD > 0),
                                                                                new ABehaviourTree.Store("<molecule-id>", (bb) -> Optional.of("D"))
                                                                        ),
                                                                        new ABehaviourTree.Sequence("CheckE",
                                                                                new ABehaviourTree.Guard("doesSampleRequireE", bb -> ((Sample) bb.stack.peek()).remainingE() > 0),
                                                                                new ABehaviourTree.Guard("isEAvailable", bb -> bb.availableE > 0),
                                                                                new ABehaviourTree.Store("<molecule-id>", (bb) -> Optional.of("E"))
                                                                        ),
                                                                        new ABehaviourTree.Invert<>("InvertPop", new ABehaviourTree.Pop())
                                                                )
                                                        )
                                                ),
                                                new ABehaviourTree.Guard("isMoleculeIdentified", bb -> bb.dataMap.containsKey("<molecule-id>") && bb.dataMap.get("<molecule-id>") != null),
                                                new ABehaviourTree.Execute("CollectMolecule", bb -> System.out.println(String.format("CONNECT %s", bb.dataMap.get("<molecule-id>"))))
                                        ),
                                        new ABehaviourTree.Sequence("WaitForMolecules",
                                                new ABehaviourTree.Guard("isThereSpaceForMolecules", bb -> bb.player.storage < 10),
                                                new ABehaviourTree.Guard("ifThereAreNoCompleteSamples", bb -> bb.player.samples.stream().noneMatch(Sample::isComplete)),
                                                new ABehaviourTree.Guard("ifEnemyAtLaboratory", bb -> bb.enemy.target.equals("LABORATORY")),
                                                new ABehaviourTree.Execute("WaitForMolecules", bb -> System.out.println("WAIT"))
                                        ),
                                        new ABehaviourTree.Selector("LeaveMolecules",
                                                new ABehaviourTree.Sequence("GoToLaboratory",
                                                        new ABehaviourTree.Guard("ifThereAreCompleteSamples", bb -> bb.player.samples.stream().anyMatch(Sample::isComplete)),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO LABORATORY"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToSamples",
                                                        new ABehaviourTree.Guard("ifThereAreNoCompleteSamples", bb -> bb.player.samples.stream().noneMatch(Sample::isComplete)),
                                                        new ABehaviourTree.Guard("ifThereIsSpaceForSamples", bb -> bb.player.samples.size() < 3),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO SAMPLES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToDiagnosis",
                                                        new ABehaviourTree.Guard("isCarryingUndiagnosedSamples", bb -> bb.player.samples.stream().anyMatch(sample -> sample.cost() < 0)),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO DIAGNOSIS"))
                                                ),
                                                new ABehaviourTree.Execute("ExecuteWait", bb -> System.out.println("WAIT"))
                                        )
                                )
                        ),
                        new ABehaviourTree.Sequence("AtLaboratory",
                                new ABehaviourTree.Guard("isAtLaboratory", bb -> bb.player.target.equals("LABORATORY")),
                                new ABehaviourTree.Selector("SelectLaboratoryAction",
                                        new ABehaviourTree.Sequence("CompleteSamples",
                                                new ABehaviourTree.Guard("ifThereAreCompleteSamples", bb -> bb.player.samples.stream().anyMatch(Sample::isComplete)),
                                                new ABehaviourTree.UntilFail<>("EmptyStack", new ABehaviourTree.Pop()),
                                                new ABehaviourTree.UntilFail<>("AddPlayerCompleteSamplesToStack",
                                                        new ABehaviourTree.Push(bb -> bb.player.samples.stream()
                                                                .filter(sample -> !bb.stack.contains(sample))
                                                                .filter(Sample::isComplete)
                                                                .max(Comparator.comparingInt(sample -> sample.health))
                                                        )
                                                ),
                                                new ABehaviourTree.Execute("ExecuteConnect", bb -> System.out.println(String.format("CONNECT %s", ((Sample) bb.stack.peek()).id)))
                                        ),
                                        new ABehaviourTree.Selector("LeaveLaboratory",
                                                new ABehaviourTree.Sequence("GoToSamples",
                                                        new ABehaviourTree.Guard("notCarryingSamples", bb -> bb.player.samples.isEmpty()),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO SAMPLES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToMolecules",
                                                        new ABehaviourTree.Guard("doesAnySampleNeedMolecules", bb -> bb.player.samples.stream().anyMatch(s -> !s.isComplete())),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO MOLECULES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToDiagnosis",
                                                        new ABehaviourTree.Guard("isCarryingUndiagnosedSamples", bb -> bb.player.samples.stream().anyMatch(sample -> sample.cost() < 0)),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO DIAGNOSIS"))
                                                )
                                        )
                                )
                        )
                ),
                new EmptyBlackboard()
        );

        while (true) {
            behaviourTree.process(gameState);
        }
    }


    enum MoleculeType {
        A, B, C, D, E;

        static Map<MoleculeType, Integer> createBag() {
            Map<MoleculeType, Integer> bag = new HashMap<>();
            bag.put(MoleculeType.A, 0);
            bag.put(MoleculeType.B, 0);
            bag.put(MoleculeType.C, 0);
            bag.put(MoleculeType.D, 0);
            bag.put(MoleculeType.E, 0);
            return bag;
        }
    }
    class Agent {
        Map<MoleculeType, Integer> moleculeBag = MoleculeType.createBag();
        Map<MoleculeType, Integer> expertiseBag = MoleculeType.createBag();
        PriorityQueue<NewSample> incompleteSamples = new PriorityQueue<>(3, Comparator.comparing(s -> s.health));
        PriorityQueue<NewSample> completeSamples = new PriorityQueue<>(3, Comparator.comparing(s -> s.health));
    }

    class NewSample {
        Integer health;
        Map<MoleculeType, Integer> moleculeBag = MoleculeType.createBag();
    }

    private static class UpdateBlackboard extends ABehaviourTree<GameState> {
        private Scanner in;

        UpdateBlackboard(Scanner in) {
            super("UpdateBlackboard");
            this.in = in;
        }

        @Override
        public Status process(GameState bb) {
            bb.player.target = in.next();
            System.err.println(bb.player.target);
            bb.player.eta = in.nextInt();
            bb.player.score = in.nextInt();
            bb.player.storageA = in.nextInt();
            bb.player.storageB = in.nextInt();
            bb.player.storageC = in.nextInt();
            bb.player.storageD = in.nextInt();
            bb.player.storageE = in.nextInt();
            bb.player.storage = bb.player.storageA + bb.player.storageB + bb.player.storageC + bb.player.storageD + bb.player.storageE;
            bb.player.expertiseA = in.nextInt();
            bb.player.expertiseB = in.nextInt();
            bb.player.expertiseC = in.nextInt();
            bb.player.expertiseD = in.nextInt();
            bb.player.expertiseE = in.nextInt();

            bb.enemy.target = in.next();
            bb.enemy.eta = in.nextInt();
            bb.enemy.score = in.nextInt();
            bb.enemy.storageA = in.nextInt();
            bb.enemy.storageB = in.nextInt();
            bb.enemy.storageC = in.nextInt();
            bb.enemy.storageD = in.nextInt();
            bb.enemy.storageE = in.nextInt();
            bb.enemy.storage = bb.enemy.storageA + bb.enemy.storageB + bb.enemy.storageC + bb.enemy.storageD + bb.enemy.storageE;
            bb.enemy.expertiseA = in.nextInt();
            bb.enemy.expertiseB = in.nextInt();
            bb.enemy.expertiseC = in.nextInt();
            bb.enemy.expertiseD = in.nextInt();
            bb.enemy.expertiseE = in.nextInt();

            bb.availableA = in.nextInt();
            bb.availableB = in.nextInt();
            bb.availableC = in.nextInt();
            bb.availableD = in.nextInt();
            bb.availableE = in.nextInt();

            if (bb.maxAvailableA == null) {
                bb.maxAvailableA = bb.availableA;
                bb.maxAvailableB = bb.availableB;
                bb.maxAvailableC = bb.availableC;
                bb.maxAvailableD = bb.availableD;
                bb.maxAvailableE = bb.availableE;
            }

            bb.sampleCount = in.nextInt();
            for (int i = 0; i < bb.sampleCount; i++) {
                Sample sample = new Sample(bb);
                sample.id = in.nextInt();
                sample.carriedBy = in.nextInt();
                sample.rank = in.nextInt();
                sample.expertiseGain = in.next();
                sample.health = in.nextInt();
                sample.costA = in.nextInt();
                sample.costB = in.nextInt();
                sample.costC = in.nextInt();
                sample.costD = in.nextInt();
                sample.costE = in.nextInt();

                switch (sample.carriedBy) {
                    case -1:
                        sample.owner = bb.player;
                        bb.cloud.add(sample);
                        break;
                    case 0:
                        sample.owner = bb.player;
                        bb.player.samples.add(sample);
                        break;
                    case 1:
                        sample.owner = bb.enemy;
                        bb.enemy.samples.add(sample);
                        break;
                    default:
                        throw new RuntimeException("unhandled carrier");
                }
            }

            Collections.sort(bb.player.samples, Comparator.comparingInt(Sample::remaining));
            for (Sample sample : bb.player.samples) {
                sample.moleculesA = Math.min(sample.costA, bb.player.storageA);
                sample.moleculesB = Math.min(sample.costB, bb.player.storageB);
                sample.moleculesC = Math.min(sample.costC, bb.player.storageC);
                sample.moleculesD = Math.min(sample.costD, bb.player.storageD);
                sample.moleculesE = Math.min(sample.costE, bb.player.storageE);

                bb.player.storageA -= sample.moleculesA;
                bb.player.storageB -= sample.moleculesB;
                bb.player.storageC -= sample.moleculesC;
                bb.player.storageD -= sample.moleculesD;
                bb.player.storageE -= sample.moleculesE;
            }

            bb.projects.stream().filter(p -> !p.isComplete)
                    .forEach(p -> {
                                p.isComplete = p.requiredA <= bb.player.expertiseA &&
                                        p.requiredB <= bb.player.expertiseB &&
                                        p.requiredC <= bb.player.expertiseC &&
                                        p.requiredD <= bb.player.expertiseD &&
                                        p.requiredE <= bb.player.expertiseE;
                                if (p.isComplete) {
                                    p.completedBy = "P";
                                }
                            }
                    );

            bb.projects.stream().filter(p -> !p.isComplete)
                    .forEach(p -> {
                                p.isComplete = p.requiredA <= bb.enemy.expertiseA &&
                                        p.requiredB <= bb.enemy.expertiseB &&
                                        p.requiredC <= bb.enemy.expertiseC &&
                                        p.requiredD <= bb.enemy.expertiseD &&
                                        p.requiredE <= bb.enemy.expertiseE;
                                if (p.isComplete) {
                                    p.completedBy = "E";
                                }
                            }
                    );

            return Status.Success;
        }
    }

    private static class EmptyBlackboard extends ABehaviourTree<GameState> {
        EmptyBlackboard() {
            super("EmptyBlackboard");
        }

        @Override
        public Status process(GameState gameState) {
            gameState.player.samples.clear();
            gameState.enemy.samples.clear();
            gameState.cloud.clear();
            gameState.dataMap.clear();

            return Status.Success;
        }
    }

}

class Bot {
    /* module where the player is */ String target;
    int eta;
    /* the player's number of health points */ int score;
    /* number of A moleculeBag held by the player */ int storageA;
    /* number of B moleculeBag held by the player */ int storageB;
    /* number of C moleculeBag held by the player */ int storageC;
    /* number of D moleculeBag held by the player */ int storageD;
    /* number of E moleculeBag held by the player */ int storageE;
    int storage;
    int expertiseA;
    int expertiseB;
    int expertiseC;
    int expertiseD;
    int expertiseE;

    List<Sample> samples = new ArrayList<>();
}

class Project {
    int requiredA;
    int requiredB;
    int requiredC;
    int requiredD;
    int requiredE;
    Boolean isComplete;
    String completedBy;

    @Override
    public String toString() {
        return String.format("project(%s) { %s, %s, %s, %s, %s }", completedBy, requiredA, requiredB, requiredC, requiredD, requiredE);
    }
}

class Sample {
    Bot owner;
    GameState gameState;
    /* unique id for the sample */ int id;
    /* 0 if the sample is carried by you, 1 by the other robot, -1 if the sample is in the cloud */ int carriedBy;
    int rank;
    String expertiseGain;
    /* number of health points you gain from this sample */ int health;
    /* number of A moleculeBag needed to research the sample */ int costA;
    /* number of B moleculeBag needed to research the sample */ int costB;
    /* number of C moleculeBag needed to research the sample */ int costC;
    /* number of D moleculeBag needed to research the sample */ int costD;
    /* number of E moleculeBag needed to research the sample */ int costE;

    Sample(GameState gameState) {
        this.gameState = gameState;
    }

    int cost() {
        return costA + costB + costC + costD + costE;
    }

    int trueCostA() {
        return Math.max(costA - owner.expertiseA, 0);
    }

    int trueCostB() {
        return Math.max(costB - owner.expertiseB, 0);
    }

    int trueCostC() {
        return Math.max(costC - owner.expertiseC, 0);
    }

    int trueCostD() {
        return Math.max(costD - owner.expertiseD, 0);
    }

    int trueCostE() {
        return Math.max(costE - owner.expertiseE, 0);
    }

    int trueCost() {
        return trueCostA() + trueCostB() + trueCostC() + trueCostD() + trueCostE();
    }

    boolean canAffordA() {
        return trueCostA() <= gameState.maxAvailableA;
    }

    boolean canAffordB() {
        return trueCostB() <= gameState.maxAvailableB;
    }

    boolean canAffordC() {
        return trueCostC() <= gameState.maxAvailableC;
    }

    boolean canAffordD() {
        return trueCostD() <= gameState.maxAvailableD;
    }

    boolean canAffordE() {
        return trueCostE() <= gameState.maxAvailableE;
    }

    boolean canAfford() {
        return trueCost() < 10 && canAffordA() && canAffordB() && canAffordC() && canAffordD() && canAffordE();
    }

    int moleculesA;
    int moleculesB;
    int moleculesC;
    int moleculesD;
    int moleculesE;

    int molecules() {
        return moleculesA + moleculesB + moleculesC + moleculesD + moleculesE;
    }

    int remainingA() {
        return Math.max(0, costA - owner.expertiseA - moleculesA);
    }

    int remainingB() {
        return Math.max(0, costB - owner.expertiseB - moleculesB);
    }

    int remainingC() {
        return Math.max(0, costC - owner.expertiseC - moleculesC);
    }

    int remainingD() {
        return Math.max(0, costD - owner.expertiseD - moleculesD);
    }

    int remainingE() {
        return Math.max(0, costE - owner.expertiseE - moleculesE);
    }

    int remaining() {
        return remainingA() + remainingB() + remainingC() + remainingD() + remainingE();
    }

    boolean isAComplete() {
        return remainingA() == 0;
    }

    boolean isBComplete() {
        return remainingB() == 0;
    }

    boolean isCComplete() {
        return remainingC() == 0;
    }

    boolean isDComplete() {
        return remainingD() == 0;
    }

    boolean isEComplete() {
        return remainingE() == 0;
    }

    boolean isComplete() {
        return isAComplete() && isBComplete() && isCComplete() && isDComplete() && isEComplete();
    }

    @Override
    public String toString() {
        return String.format("#%s { health=%s, recipe={A=%s, B=%s, C=%s, D=%s, E=%s}, remaining={A=%s, B=%s, C=%s, D=%s, E=%s} }",
                id, health, costA, costB, costC, costD, costE, remainingA(), remainingB(), remainingC(), remainingD(), remainingE());
    }
}

class GameState {
    Bot player = new Bot();
    Bot enemy = new Bot();
    List<Sample> cloud = new ArrayList<>();
    int availableA;
    int availableB;
    int availableC;
    int availableD;
    int availableE;
    int sampleCount;
    Map<String, Object> dataMap = new HashMap<>();
    public Stack stack = new Stack();
    public Integer maxAvailableA;
    public Integer maxAvailableB;
    public Integer maxAvailableC;
    public Integer maxAvailableD;
    public Integer maxAvailableE;
    public List<Project> projects;
}

abstract class ABehaviourTree<S> {
    private ABehaviourTree parent;
    final String name;
    List<ABehaviourTree> children = new ArrayList<>();

    public ABehaviourTree(String name, ABehaviourTree... children) {
        this.name = name;
        add(children);
    }

    public final void add(ABehaviourTree... children) {
        for (ABehaviourTree child : children) {
            if (child == null) {
                throw new NullPointerException("child cannot be null");
            }

            child.parent = this;
            this.children.add(child);
        }
    }

    private Integer depth(Integer current) {
        if (parent != null) {
            return parent.depth(current + 1);
        } else {
            return current;
        }
    }

    protected String indent() {
        Integer depth = depth(0);
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("-");
        }

        return indent.toString();
    }

    final void print() {
        System.err.println(String.format("%s> %s", indent(), name));
    }

    public abstract Status process(S state);

    /**
     * Fallback nodes are used to find and execute the lower child that does not
     * fail. A fallback node will return immediately with a status code of
     * success or running when one of its children returns success or running.
     * The children are ticked in order of importance, from left to right.
     *
     * @author Andrew van der Westhuizen
     */
    public static class Selector<S> extends ABehaviourTree<S> {

        public Selector(String name, ABehaviourTree... children) {
            super(name, children);
        }

        @Override
        public Status process(S state) {
            print();
            for (ABehaviourTree child : children) {
                Status status = child.process(state);
//                Logger.trace(String.format("    %s - %s", new Object[]{child.getClass().getSimpleName(), status}));
                switch (status) {
                    case Running:
                    case Success:
                        return status;
                }
            }

            return Status.Failure;
        }
    }

    /**
     * Sequence nodes are used to find and execute the lower child that has not
     * yet succeeded. A sequence node will return immediately with a status code
     * of failure or running when one of its children returns failure or
     * running. The children are ticked in order, from left to right.
     *
     * @author Andrew van der Westhuizen
     */
    public static class Sequence<S> extends ABehaviourTree<S> {

        public Sequence(String name, ABehaviourTree... children) {
            super(name, children);
        }

        @Override
        public Status process(S state) {
            print();
            for (ABehaviourTree child : children) {
                Status status = child.process(state);
//                Logger.trace(String.format("    %s - %s", new Object[]{child.getClass().getSimpleName(), status}));

                switch (status) {
                    case Running:
                    case Failure:
                        return status;
                }
            }

            return Status.Success;
        }
    }

    /**
     * Will repeat the wrapped task until that task fails.
     *
     * @author Andrew van der Westhuizen
     */
    public static class UntilFail<S> extends ABehaviourTree<S> {

        public UntilFail(String name, ABehaviourTree<S> child) {
            super(name, child);
        }

        @Override
        public Status process(S state) {
            print();
            if (children.size() != 1) {
                throw new AssertionError(children);
            }

            Status status = Status.Success;
            while (status != Status.Failure) {
                status = children.get(0).process(state);
            }

            return Status.Success;
        }
    }

    /**
     * Will always fail the task.
     *
     * @author Andrew van der Westhuizen
     */
    public static class AlwaysFail<S> extends ABehaviourTree<S> {

        public AlwaysFail(String name, ABehaviourTree<S> child) {
            super(name, child);
        }

        @Override
        public Status process(S state) {
            print();
            if (children.size() != 1) {
                throw new AssertionError(children);
            }

            Status status = children.get(0).process(state);
            switch (status) {
                case Running:
                case Success:
                case Failure:
                    return Status.Failure;
                default:
                    throw new AssertionError(status.name());
            }
        }
    }

    /**
     * Will invert the child's response.
     *
     * @author Andrew van der Westhuizen
     */
    public static class Invert<S> extends ABehaviourTree<S> {
        public Invert(String name, ABehaviourTree child) {
            super(name, child);
        }

        @Override
        public Status process(S state) {
            print();
            if (children.size() != 1) {
                throw new AssertionError(children);
            }

            Status status = children.get(0).process(state);
            switch (status) {
                case Running:
                    return Status.Running;
                case Success:
                    return Status.Failure;
                case Failure:
                    return Status.Success;
                default:
                    throw new AssertionError(status.name());
            }
        }
    }

    /**
     * Will execute a boolean lambda function
     */
    static class Guard extends ABehaviourTree<GameState> {
        private Function<GameState, Boolean> lambda;

        Guard(String name, Function<GameState, Boolean> lambda) {
            super(name);
            this.lambda = lambda;
        }

        @Override
        public Status process(GameState gameState) {
            if (lambda.apply(gameState)) {
                System.err.println(String.format("%s> [PASS] %s", indent(), name));
                return Status.Success;
            } else {
                System.err.println(String.format("%s> [FAIL] %s", indent(), name));
                return Status.Failure;
            }
        }
    }

    /**
     * Will push an object onto a stack
     */
    static class Push extends ABehaviourTree<GameState> {
        private Function<GameState, Optional> lambda;

        public Push(Function<GameState, Optional> lambda) {
            super("Push");
            this.lambda = lambda;
        }

        @Override
        public Status process(GameState gameState) {
//            System.err.println(String.format("gameState stack <%s>", gameState.stack));
            Optional item = lambda.apply(gameState);
            if (item.isPresent()) {
                gameState.stack.push(item.get());
                System.err.println(String.format("%s> [%s] <%s>", indent(), name, item.get()));
                return Status.Success;
            } else {
//                System.err.println("nothing to push onto stack");
                return Status.Failure;
            }
        }
    }

    /**
     * Will store an object with a key
     */
    static class Store extends ABehaviourTree<GameState> {
        private final String key;
        private final Function<GameState, Optional> lambda;

        Store(String key, Function<GameState, Optional> lambda) {
            super("Store");
            this.key = key;
            this.lambda = lambda;
        }

        @Override
        public Status process(GameState gameState) {
            Optional value = lambda.apply(gameState);
            if (value.isPresent()) {
                System.err.println(String.format("%s> [PASS] %s %s=%s ", indent(), name, key, value.get()));
                gameState.dataMap.put(key, value.get());
                return Status.Success;
            } else {
                System.err.println(String.format("%s> [FAIL] %s %s ", indent(), name, key));
                return Status.Failure;
            }
        }
    }

    /**
     * Will remove the topmost item from the stack
     */
    static class Pop extends ABehaviourTree<GameState> {
        public Pop() {
            super("Pop");
        }

        @Override
        public Status process(GameState gameState) {
            if (gameState.stack.isEmpty()) {
                return Status.Failure;
            } else {
                Object item = gameState.stack.pop();
                System.err.println(String.format("%s> [%s] <%s>", indent(), name, item));
                return Status.Success;
            }
        }
    }

    /**
     * Will execute a lambda and report success if no exceptions occur
     */
    static class Execute extends ABehaviourTree<GameState> {
        private Consumer<GameState> lambda;

        public Execute(String name, Consumer<GameState> lambda) {
            super(name);
            this.lambda = lambda;
        }

        @Override
        public Status process(GameState gameState) {
            try {
                lambda.accept(gameState);
            } catch (Exception e) {
                System.err.println(String.format("%s> [FAIL] %s", indent(), name));
                return Status.Failure;
            }
            System.err.println(String.format("%s> [PASS] %s", indent(), name));
            return Status.Success;
        }
    }

    public enum Status {
        Running, Failure, Success
    }

}
