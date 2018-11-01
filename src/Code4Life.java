import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Bring store on patient oldSamples from the diagnosis machine to the laboratory with enough carriedMoleculeBag to produce medicine!
 **/
class Player {

    public static void main(String args[]) {
        Integer MOLECULE_QUICK_WIN = 2;

        Blackboard blackboard = new Blackboard();
        Scanner in = new Scanner(System.in);

        int projectCount = in.nextInt();
        for (int i = 0; i < projectCount; i++) {
            int a = in.nextInt();
            int b = in.nextInt();
            int c = in.nextInt();
            int d = in.nextInt();
            int e = in.nextInt();

            Project project = new Project();
            project.requirementBag.put(MoleculeType.A, new AtomicInteger(a));
            project.requirementBag.put(MoleculeType.B, new AtomicInteger(b));
            project.requirementBag.put(MoleculeType.C, new AtomicInteger(c));
            project.requirementBag.put(MoleculeType.D, new AtomicInteger(d));
            project.requirementBag.put(MoleculeType.E, new AtomicInteger(e));
            blackboard.incompleteProjects.add(project);
        }

        ABehaviourTree<Blackboard> behaviourTree = new ABehaviourTree.Sequence<>("Code4Life",
                new ABehaviourTree.Execute("ReadPlayerState", (bb) -> {
                    bb.player.target = in.next();
                    bb.player.eta = in.nextInt();
                    bb.player.score = in.nextInt();
                    bb.player.storageA = in.nextInt();
                    bb.player.storageB = in.nextInt();
                    bb.player.storageC = in.nextInt();
                    bb.player.storageD = in.nextInt();
                    bb.player.storageE = in.nextInt();
                    bb.player.expertiseA = in.nextInt();
                    bb.player.expertiseB = in.nextInt();
                    bb.player.expertiseC = in.nextInt();
                    bb.player.expertiseD = in.nextInt();
                    bb.player.expertiseE = in.nextInt();

                    bb.playerAgent.target = bb.player.target;
                    bb.playerAgent.eta = bb.player.eta;
                    bb.playerAgent.score = bb.player.score;
                    bb.playerAgent.validateStorageBags(MoleculeType.A, bb.player.storageA);
                    bb.playerAgent.validateStorageBags(MoleculeType.B, bb.player.storageB);
                    bb.playerAgent.validateStorageBags(MoleculeType.C, bb.player.storageC);
                    bb.playerAgent.validateStorageBags(MoleculeType.D, bb.player.storageD);
                    bb.playerAgent.validateStorageBags(MoleculeType.E, bb.player.storageE);
                    bb.playerAgent.validateExpertiseBag(MoleculeType.A, bb.player.expertiseA);
                    bb.playerAgent.validateExpertiseBag(MoleculeType.B, bb.player.expertiseB);
                    bb.playerAgent.validateExpertiseBag(MoleculeType.C, bb.player.expertiseC);
                    bb.playerAgent.validateExpertiseBag(MoleculeType.D, bb.player.expertiseD);
                    bb.playerAgent.validateExpertiseBag(MoleculeType.E, bb.player.expertiseE);
                }),
                new ABehaviourTree.Execute("ReadEnemyState", (bb) -> {
                    bb.enemy.target = in.next();
                    bb.enemy.eta = in.nextInt();
                    bb.enemy.score = in.nextInt();
                    bb.enemy.storageA = in.nextInt();
                    bb.enemy.storageB = in.nextInt();
                    bb.enemy.storageC = in.nextInt();
                    bb.enemy.storageD = in.nextInt();
                    bb.enemy.storageE = in.nextInt();
                    bb.enemy.expertiseA = in.nextInt();
                    bb.enemy.expertiseB = in.nextInt();
                    bb.enemy.expertiseC = in.nextInt();
                    bb.enemy.expertiseD = in.nextInt();
                    bb.enemy.expertiseE = in.nextInt();
                }),
                new ABehaviourTree.Execute("ReadMoleculeState", (bb) -> {
                    Integer availableA = in.nextInt();
                    Integer availableB = in.nextInt();
                    Integer availableC = in.nextInt();
                    Integer availableD = in.nextInt();
                    Integer availableE = in.nextInt();

                    bb.validateMoleculePool(MoleculeType.A, availableA);
                    bb.validateMoleculePool(MoleculeType.B, availableB);
                    bb.validateMoleculePool(MoleculeType.C, availableC);
                    bb.validateMoleculePool(MoleculeType.D, availableD);
                    bb.validateMoleculePool(MoleculeType.E, availableE);

                    bb.moleculePoolMax.computeIfAbsent(MoleculeType.A, k -> new AtomicInteger(availableA));
                    bb.moleculePoolMax.computeIfAbsent(MoleculeType.B, k -> new AtomicInteger(availableB));
                    bb.moleculePoolMax.computeIfAbsent(MoleculeType.C, k -> new AtomicInteger(availableC));
                    bb.moleculePoolMax.computeIfAbsent(MoleculeType.D, k -> new AtomicInteger(availableD));
                    bb.moleculePoolMax.computeIfAbsent(MoleculeType.E, k -> new AtomicInteger(availableE));
                }),
                new ABehaviourTree.Execute("ReadSampleState", (bb) -> {
                    bb.sampleCount = in.nextInt();
                    for (int i = 0; i < bb.sampleCount; i++) {
                        OldSample oldSample = new OldSample(bb);
                        oldSample.id = in.nextInt();
                        oldSample.carriedBy = in.nextInt();
                        oldSample.rank = in.nextInt();
                        oldSample.expertiseGain = in.next();
                        oldSample.health = in.nextInt();
                        oldSample.costA = in.nextInt();
                        oldSample.costB = in.nextInt();
                        oldSample.costC = in.nextInt();
                        oldSample.costD = in.nextInt();
                        oldSample.costE = in.nextInt();

                        switch (oldSample.carriedBy) {
                            case -1:
                                oldSample.owner = bb.player;
                                bb.cloud.add(oldSample);
                                break;
                            case 0:
                                oldSample.owner = bb.player;
                                bb.player.oldSamples.add(oldSample);
                                break;
                            case 1:
                                oldSample.owner = bb.enemy;
                                bb.enemy.oldSamples.add(oldSample);
                                break;
                            default:
                                throw new RuntimeException("unhandled carrier");
                        }
                    }

                    bb.player.oldSamples.sort(Comparator.comparingInt(OldSample::remaining));
                    for (OldSample oldSample : bb.player.oldSamples) {
                        oldSample.moleculesA = Math.min(oldSample.costA, bb.player.storageA);
                        oldSample.moleculesB = Math.min(oldSample.costB, bb.player.storageB);
                        oldSample.moleculesC = Math.min(oldSample.costC, bb.player.storageC);
                        oldSample.moleculesD = Math.min(oldSample.costD, bb.player.storageD);
                        oldSample.moleculesE = Math.min(oldSample.costE, bb.player.storageE);

                        bb.player.storageA -= oldSample.moleculesA;
                        bb.player.storageB -= oldSample.moleculesB;
                        bb.player.storageC -= oldSample.moleculesC;
                        bb.player.storageD -= oldSample.moleculesD;
                        bb.player.storageE -= oldSample.moleculesE;
                    }
                }),
                new ABehaviourTree.Selector("SelectAction",
                        new ABehaviourTree.Sequence("WhilstMoving",
                                new ABehaviourTree.Guard("isMoving", bb -> bb.playerAgent.eta > 0),
                                new ABehaviourTree.Execute("Wait", bb -> System.out.println("WAIT"))
                        ),
                        new ABehaviourTree.Sequence("WhilstAtStart",
                                new ABehaviourTree.Guard("isAtStart", bb -> bb.playerAgent.target.equals("START_POS")),
                                new ABehaviourTree.Execute("gotoSamples", bb -> System.out.println("GOTO SAMPLES"))
                        ),
                        new ABehaviourTree.Sequence("WhilstAtSamples",
                                new ABehaviourTree.Guard("isAtSamples", bb -> bb.player.target.equals("SAMPLES")),
                                new ABehaviourTree.Selector("SelectAction",
                                        new ABehaviourTree.Sequence("CollectSamples",
                                                new ABehaviourTree.Guard("canCarryMoreSamples", bb -> bb.playerAgent.canCarryMoreSamples()),
                                                new ABehaviourTree.Selector<>("SelectSample",
                                                        new ABehaviourTree.Sequence("WhenNoExpertise",
                                                                new ABehaviourTree.Guard("hasNoExpertise", bb -> bb.playerAgent.expertise.values().stream().mapToInt(AtomicInteger::get).sum() == 0),
                                                                new ABehaviourTree.Store("samples:collect-sample-rank", bb -> Optional.of("1"))
                                                        ),
                                                        new ABehaviourTree.Sequence("ConsiderRank3",
                                                                new ABehaviourTree.Guard("hasRank2Sample", bb -> bb.playerAgent.carriedSamples().stream().anyMatch(s -> s.rank == 2)),
                                                                new ABehaviourTree.Store("samples:collect-sample-rank", bb -> Optional.of("3"))
                                                        ),
                                                        new ABehaviourTree.Sequence("ConsiderRank2",
                                                                new ABehaviourTree.Selector("Rank2Conditions",
                                                                        new ABehaviourTree.Guard("enoughExpertiseForRank2", bb -> 2 <=
                                                                                (bb.player.expertiseA >= 2 ? 1 : 0) +
                                                                                        (bb.player.expertiseB >= 2 ? 1 : 0) +
                                                                                        (bb.player.expertiseC >= 2 ? 1 : 0) +
                                                                                        (bb.player.expertiseD >= 2 ? 1 : 0) +
                                                                                        (bb.player.expertiseE >= 2 ? 1 : 0)
                                                                        ),
                                                                        new ABehaviourTree.Guard("has2Rank1Samples", bb -> bb.player.oldSamples.stream().filter(s -> s.rank == 1).count() == 2)
                                                                ),
                                                                new ABehaviourTree.Store("samples:collect-sample-rank", bb -> Optional.of("2"))
                                                        ),
                                                        new ABehaviourTree.Sequence("AlwaysRank1",
                                                                new ABehaviourTree.Store("samples:collect-sample-rank", bb -> Optional.of("1"))
                                                        )
                                                ),
                                                new ABehaviourTree.Guard("isSampleIdentified", bb -> bb.store.containsKey("samples:collect-sample-rank")),
                                                new ABehaviourTree.Execute("collectSample", bb -> System.out.println(String.format("CONNECT %s", bb.store.get("samples:collect-sample-rank"))))
                                        ),
                                        new ABehaviourTree.Selector("LeaveSamples",
                                                new ABehaviourTree.Sequence("GoToDiagnosis",
                                                        new ABehaviourTree.Guard("isCarryingUndiagnosedSamples", bb -> bb.player.oldSamples.stream().anyMatch(oldSample -> oldSample.cost() < 0)),
                                                        new ABehaviourTree.Execute("gotoDiagnosis", bb -> System.out.println("GOTO DIAGNOSIS"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToMolecules",
                                                        new ABehaviourTree.Guard("doesAnySampleNeedMolecules", bb -> bb.player.oldSamples.stream().anyMatch(s -> !s.isComplete())),
                                                        new ABehaviourTree.Execute("gotoMolecules", bb -> System.out.println("GOTO MOLECULES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToLaboratory",
                                                        new ABehaviourTree.Guard("ifThereAreCompleteSamples", bb -> bb.player.oldSamples.stream().anyMatch(OldSample::isComplete)),
                                                        new ABehaviourTree.Execute("gotoLaboratory", bb -> System.out.println("GOTO LABORATORY"))
                                                )
                                        )
                                )
                        ),
                        new ABehaviourTree.Sequence("AtDiagnosis",
                                new ABehaviourTree.Guard("isAtDiagnosis", bb -> bb.player.target.equals("DIAGNOSIS")),
                                new ABehaviourTree.Selector("SelectDiagnosisAction",
                                        new ABehaviourTree.Sequence("DiagnoseUndiagnosedSamples",
                                                new ABehaviourTree.Guard("isCarryingUndiagnosedSamples", bb -> bb.player.oldSamples.stream().anyMatch(oldSample -> oldSample.cost() < 0)),
                                                new ABehaviourTree.Store("diagnosis:undiagnosed-sample", bb -> bb.player.oldSamples.stream().filter(s -> s.cost() < 0).map(s -> s.id).findAny()),
                                                new ABehaviourTree.Guard("isSampleIdentified", bb -> !Objects.isNull(bb.store.get("diagnosis:undiagnosed-sample"))),
                                                new ABehaviourTree.Execute("diagnoseSample", bb -> System.out.println(String.format("CONNECT %s", ((Sample) bb.store.get("diagnosis:undiagnosed-sample")).id)))
                                        ),
                                        new ABehaviourTree.Sequence("UploadAllSamplesIfCannotReleaseMolecules",
                                                new ABehaviourTree.Guard("cannotCarryMoreMolecules", bb -> !bb.playerAgent.canCarryMoreMolecules()),
                                                new ABehaviourTree.Guard("canSamplesBeCompletedWithStoredMolecules", bb -> bb.player.oldSamples.stream().noneMatch(OldSample::isComplete)),
                                                new ABehaviourTree.Store("<sample-id>", bb -> bb.player.oldSamples.stream().findAny()),
                                                new ABehaviourTree.Guard("isSampleIdentified", bb -> bb.store.containsKey("<sample-id>") && bb.store.get("<sample-id>") != null),
                                                new ABehaviourTree.Execute("UploadSample", bb -> System.out.println(String.format("CONNECT %s", ((OldSample) bb.store.get("<sample-id>")).id)))
                                        ),
                                        new ABehaviourTree.Sequence("DownloadQuickWinSamples",
                                                new ABehaviourTree.Guard("isSpaceForSamples", bb -> bb.player.oldSamples.size() < 3),
                                                new ABehaviourTree.Store("<sample-id>", bb -> bb.cloud.stream()
                                                        .sorted(Comparator.<OldSample>comparingInt(s -> s.rank).reversed())
                                                        .filter(s -> s.remaining() < MOLECULE_QUICK_WIN)
                                                        .map(s -> s.id).findAny()
                                                ),
                                                new ABehaviourTree.Guard("isSampleIdentified", bb -> bb.store.containsKey("<sample-id>") && bb.store.get("<sample-id>") != null),
                                                new ABehaviourTree.Execute("UploadSample", bb -> System.out.println(String.format("CONNECT %s", bb.store.get("<sample-id>"))))
                                        ),
                                        new ABehaviourTree.Selector("LeaveDiagnosis",
                                                new ABehaviourTree.Sequence("GoToSamples",
                                                        new ABehaviourTree.Guard("notCarryingSamples", bb -> bb.player.oldSamples.isEmpty()),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO SAMPLES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToLaboratory",
                                                        new ABehaviourTree.Guard("ifThereAreCompleteSamples", bb -> bb.player.oldSamples.stream().anyMatch(OldSample::isComplete)),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO LABORATORY"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToMolecules",
                                                        new ABehaviourTree.Guard("doesAnySampleNeedMolecules", bb -> bb.player.oldSamples.stream().anyMatch(s -> !s.isComplete())),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO MOLECULES"))
                                                )
                                        )
                                )
                        ),

                        new ABehaviourTree.Sequence("AtMolecules",
                                new ABehaviourTree.Guard("isAtMolecules", bb -> bb.player.target.equals("MOLECULES")),
                                new ABehaviourTree.Selector("SelectMoleculesAction",
                                        new ABehaviourTree.Sequence("CollectMolecules",
                                                new ABehaviourTree.Guard("isThereSpaceForMolecules", bb -> bb.playerAgent.canCarryMoreMolecules()),
                                                new ABehaviourTree.Guard("doesAnySampleNeedMolecules", bb -> bb.player.oldSamples.stream().anyMatch(s -> s.remaining() > 0)),
                                                new ABehaviourTree.UntilFail<>("EmptyStack", new ABehaviourTree.Pop()),
                                                new ABehaviourTree.UntilFail<>("AddPlayerSamplesNeedingMoleculesToStack",
                                                        new ABehaviourTree.Push(bb -> bb.player.oldSamples.stream()
                                                                .filter(oldSample -> oldSample.remaining() > 0)
                                                                .filter(oldSample -> !bb.stack.contains(oldSample))
                                                                .max(Comparator.comparingInt(OldSample::remaining))
                                                        )
                                                ),
                                                new ABehaviourTree.UntilFail<>("PopSamplesUntilFail",
                                                        new ABehaviourTree.Invert<>("InvertIdentify",
                                                                new ABehaviourTree.Selector<>("IdentifyMolecules",
                                                                        new ABehaviourTree.Guard("IsSampleStackEmpty", bb -> bb.stack.isEmpty()),
                                                                        new ABehaviourTree.Sequence("CheckA",
                                                                                new ABehaviourTree.Guard("doesSampleRequireA", bb -> ((OldSample) bb.stack.peek()).remainingA() > 0),
                                                                                new ABehaviourTree.Guard("isAAvailable", bb -> bb.moleculePool.get(MoleculeType.A).get() > 0),
                                                                                new ABehaviourTree.Store("<molecule-id>", (bb) -> Optional.of(MoleculeType.A))
                                                                        ),
                                                                        new ABehaviourTree.Sequence("CheckB",
                                                                                new ABehaviourTree.Guard("doesSampleRequireB", bb -> ((OldSample) bb.stack.peek()).remainingB() > 0),
                                                                                new ABehaviourTree.Guard("isBAvailable", bb -> bb.moleculePool.get(MoleculeType.B).get() > 0),
                                                                                new ABehaviourTree.Store("<molecule-id>", (bb) -> Optional.of(MoleculeType.B))
                                                                        ),
                                                                        new ABehaviourTree.Sequence("CheckC",
                                                                                new ABehaviourTree.Guard("doesSampleRequireC", bb -> ((OldSample) bb.stack.peek()).remainingC() > 0),
                                                                                new ABehaviourTree.Guard("isCAvailable", bb -> bb.moleculePool.get(MoleculeType.C).get() > 0),
                                                                                new ABehaviourTree.Store("<molecule-id>", (bb) -> Optional.of(MoleculeType.C))
                                                                        ),
                                                                        new ABehaviourTree.Sequence("CheckD",
                                                                                new ABehaviourTree.Guard("doesSampleRequireD", bb -> ((OldSample) bb.stack.peek()).remainingD() > 0),
                                                                                new ABehaviourTree.Guard("isDAvailable", bb -> bb.moleculePool.get(MoleculeType.D).get() > 0),
                                                                                new ABehaviourTree.Store("<molecule-id>", (bb) -> Optional.of(MoleculeType.D))
                                                                        ),
                                                                        new ABehaviourTree.Sequence("CheckE",
                                                                                new ABehaviourTree.Guard("doesSampleRequireE", bb -> ((OldSample) bb.stack.peek()).remainingE() > 0),
                                                                                new ABehaviourTree.Guard("isEAvailable", bb -> bb.moleculePool.get(MoleculeType.E).get() > 0),
                                                                                new ABehaviourTree.Store("<molecule-id>", (bb) -> Optional.of(MoleculeType.E))
                                                                        ),
                                                                        new ABehaviourTree.Invert<>("InvertPop", new ABehaviourTree.Pop())
                                                                )
                                                        )
                                                ),
                                                new ABehaviourTree.Guard("isMoleculeIdentified", bb -> bb.store.containsKey("<molecule-id>") && bb.store.get("<molecule-id>") != null),
                                                new ABehaviourTree.Execute("CollectMolecule", bb -> {
                                                    System.out.println(String.format("CONNECT %s", bb.store.get("<molecule-id>")));
                                                    bb.playerAgent.collectMolecule((Sample) bb.store.get("molecule:selected-sample"), (MoleculeType) bb.store.get("molecule:selected-molecule"));
                                                })
                                        ),
                                        new ABehaviourTree.Sequence("WaitForMolecules",
                                                new ABehaviourTree.Guard("isThereSpaceForMolecules", bb -> bb.playerAgent.canCarryMoreMolecules()),
                                                new ABehaviourTree.Guard("ifThereAreNoCompleteSamples", bb -> bb.player.oldSamples.stream().noneMatch(OldSample::isComplete)),
                                                new ABehaviourTree.Guard("ifEnemyAtLaboratory", bb -> bb.enemy.target.equals("LABORATORY")),
                                                new ABehaviourTree.Execute("WaitForMolecules", bb -> System.out.println("WAIT"))
                                        ),
                                        new ABehaviourTree.Selector("LeaveMolecules",
                                                new ABehaviourTree.Sequence("GoToLaboratory",
                                                        new ABehaviourTree.Guard("ifThereAreCompleteSamples", bb -> bb.player.oldSamples.stream().anyMatch(OldSample::isComplete)),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO LABORATORY"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToSamples",
                                                        new ABehaviourTree.Guard("ifThereAreNoCompleteSamples", bb -> bb.player.oldSamples.stream().noneMatch(OldSample::isComplete)),
                                                        new ABehaviourTree.Guard("ifThereIsSpaceForSamples", bb -> bb.player.oldSamples.size() < 3),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO SAMPLES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToDiagnosis",
                                                        new ABehaviourTree.Guard("isCarryingUndiagnosedSamples", bb -> bb.player.oldSamples.stream().anyMatch(oldSample -> oldSample.cost() < 0)),
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
                                                new ABehaviourTree.Guard("ifThereAreCompleteSamples", bb -> bb.player.oldSamples.stream().anyMatch(OldSample::isComplete)),
                                                new ABehaviourTree.UntilFail<>("EmptyStack", new ABehaviourTree.Pop()),
                                                new ABehaviourTree.UntilFail<>("AddPlayerCompleteSamplesToStack",
                                                        new ABehaviourTree.Push(bb -> bb.player.oldSamples.stream()
                                                                .filter(oldSample -> !bb.stack.contains(oldSample))
                                                                .filter(OldSample::isComplete)
                                                                .max(Comparator.comparingInt(oldSample -> oldSample.health))
                                                        )
                                                ),
                                                new ABehaviourTree.Execute("ExecuteConnect", bb -> System.out.println(String.format("CONNECT %s", ((OldSample) bb.stack.peek()).id)))
                                        ),
                                        new ABehaviourTree.Selector("LeaveLaboratory",
                                                new ABehaviourTree.Sequence("GoToSamples",
                                                        new ABehaviourTree.Guard("notCarryingSamples", bb -> bb.player.oldSamples.isEmpty()),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO SAMPLES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToMolecules",
                                                        new ABehaviourTree.Guard("doesAnySampleNeedMolecules", bb -> bb.player.oldSamples.stream().anyMatch(s -> !s.isComplete())),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO MOLECULES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToDiagnosis",
                                                        new ABehaviourTree.Guard("isCarryingUndiagnosedSamples", bb -> bb.player.oldSamples.stream().anyMatch(oldSample -> oldSample.cost() < 0)),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO DIAGNOSIS"))
                                                )
                                        )
                                )
                        )
                ),
                new EmptyBlackboard()
        );

        while (true) {
            behaviourTree.process(blackboard);
        }
    }


    enum MoleculeType {
        A, B, C, D, E
    }

    static class PlayerAgent {
        String target;
        Map<MoleculeType, AtomicInteger> expertise = new HashMap<>();
        Map<MoleculeType, AtomicInteger> futureExpertise = new HashMap<>();
        List<Sample> incompleteSamples = new ArrayList<>();
        List<Sample> completeSamples = new ArrayList<>();
        Integer eta;
        Integer score;

        Integer carriedMoleculeCount() {
            return carriedSamples().stream().mapToInt(Sample::carriedMoleculeCount).sum();
        }

        void validateStorageBags(MoleculeType moleculeType, Integer count) {
            if (carriedMolecules().get(moleculeType).get() != count) {
                throw new IllegalStateException("agent state does not equal system state");
            }
        }

        void validateExpertiseBag(MoleculeType moleculeType, Integer count) {
            if (expertise.get(moleculeType).get() != count) {
                throw new IllegalStateException("agent state does not equal system state");
            }
        }

        void collectMolecule(Sample sample, MoleculeType moleculeType) {
            if (Objects.isNull(sample)) {
                throw new NullPointerException("samples is null");
            }
            if (Objects.isNull(moleculeType)) {
                throw new NullPointerException("moleculeType is null");
            }
            if (!incompleteSamples.contains(sample)) {
                throw new IllegalStateException("should only collect molecules for incomplete samples");
            }

            if (sample.isComplete()) {
                throw new IllegalStateException("sample is complete but still listed in incomplete samples");
            }

            if (!canCarryMoreMolecules()) {
                throw new IllegalStateException("cannot collect more molecules as player agent if full");
            }

            sample.addMolecule(moleculeType);

            if (sample.isComplete()) {
                incompleteSamples.remove(sample);
                completeSamples.add(sample);
                futureExpertise.get(sample.expertiseGain).incrementAndGet();
            }
        }

        Boolean canCarryMoreSamples() {
            return carriedSamples().size() < 3;
        }

        Boolean canCarryMoreMolecules() {
            return carriedMoleculeCount() < 10;
        }

        List<Sample> carriedSamples() {
            List<Sample> carriedSamples = new ArrayList<>();
            carriedSamples.addAll(completeSamples);
            carriedSamples.addAll(incompleteSamples);
            return carriedSamples;
        }

        Map<MoleculeType, AtomicInteger> carriedMolecules() {
            Map<MoleculeType, AtomicInteger> carriedMolecules = new HashMap<>();
            carriedSamples().stream().map(sample -> sample.carriedMoleculeBag)
                    .forEach(carriedMolecules::putAll);
            return carriedMolecules;
        }
    }

    class Sample {
        final Integer id;
        final Integer rank;
        final Integer health;
        final MoleculeType expertiseGain;
        final Map<MoleculeType, AtomicInteger> futureExpertise;
        final Map<MoleculeType, AtomicInteger> expertise;
        final Map<MoleculeType, AtomicInteger> requiredMoleculeBag = new HashMap<>();
        final Map<MoleculeType, AtomicInteger> carriedMoleculeBag = new HashMap<>();

        Sample(Integer id, Integer rank, Integer health, MoleculeType expertiseGain, Map<MoleculeType, AtomicInteger> futureExpertise, Map<MoleculeType, AtomicInteger> expertise) {
            this.id = id;
            this.rank = rank;
            this.health = health;
            this.expertiseGain = expertiseGain;
            this.futureExpertise = futureExpertise;
            this.expertise = expertise;
        }

        Integer carriedMoleculeCount() {
            return carriedMoleculeBag.values().stream().mapToInt(AtomicInteger::get).sum();
        }

        Boolean isComplete() {
            return requiredMoleculeBag.entrySet().stream().allMatch(entry -> {
                MoleculeType type = entry.getKey();
                Integer requirement = entry.getValue().get();
                Integer required = requirement - expertise.get(type).get() - futureExpertise.get(type).get() - carriedMoleculeBag.get(type).get();
                if (required < 0) {
                    throw new IllegalStateException("waste detected, too many molecules for sample");
                }

                return required == 0;
            });
        }

        void addMolecule(MoleculeType moleculeType) {
            if (isComplete()) {
                throw new IllegalStateException("waste detected, about to collect too many molecules for sample");
            }

            carriedMoleculeBag.computeIfAbsent(moleculeType, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    static class Project {
        Map<MoleculeType, AtomicInteger> requirementBag = new HashMap<>();
        Class completedBy;
    }


    private static class EmptyBlackboard extends ABehaviourTree<Blackboard> {
        EmptyBlackboard() {
            super("EmptyBlackboard");
        }

        @Override
        public Status process(Blackboard blackboard) {
            blackboard.player.oldSamples.clear();
            blackboard.enemy.oldSamples.clear();
            blackboard.cloud.clear();
            blackboard.store.clear();

            return Status.Success;
        }
    }

}

class Blackboard {
    List<Player.Project> completeProjects = new ArrayList<>();
    List<Player.Project> incompleteProjects = new ArrayList<>();
    Player.PlayerAgent playerAgent = new Player.PlayerAgent();
    Map<Player.MoleculeType, AtomicInteger> moleculePoolMax = new HashMap<>();
    Map<Player.MoleculeType, AtomicInteger> moleculePool = new HashMap<>();

    Bot player = new Bot();
    Bot enemy = new Bot();
    List<OldSample> cloud = new ArrayList<>();
    int sampleCount;
    Map<String, Object> store = new HashMap<>();
    public Stack stack = new Stack();

    void validateMoleculePool(Player.MoleculeType moleculeType, Integer count) {
        if (moleculePool.get(moleculeType).get() != count) {
            throw new IllegalStateException("blackboard state does not equal system state");
        }
    }
}

class Bot {
    /* module where the player is */ String target;
    int eta;
    /* the player's number of health points */ int score;
    /* number of A carriedMoleculeBag held by the player */ int storageA;
    /* number of B carriedMoleculeBag held by the player */ int storageB;
    /* number of C carriedMoleculeBag held by the player */ int storageC;
    /* number of D carriedMoleculeBag held by the player */ int storageD;
    /* number of E carriedMoleculeBag held by the player */ int storageE;
    int expertiseA;
    int expertiseB;
    int expertiseC;
    int expertiseD;
    int expertiseE;

    List<OldSample> oldSamples = new ArrayList<>();
}

class OldSample {
    Bot owner;
    Blackboard blackboard;
    /* unique id for the sample */ int id;
    /* 0 if the sample is carried by you, 1 by the other robot, -1 if the sample is in the cloud */ int carriedBy;
    int rank;
    String expertiseGain;
    /* number of health points you gain from this sample */ int health;
    /* number of A carriedMoleculeBag needed to research the sample */ int costA;
    /* number of B carriedMoleculeBag needed to research the sample */ int costB;
    /* number of C carriedMoleculeBag needed to research the sample */ int costC;
    /* number of D carriedMoleculeBag needed to research the sample */ int costD;
    /* number of E carriedMoleculeBag needed to research the sample */ int costE;

    OldSample(Blackboard blackboard) {
        this.blackboard = blackboard;
    }

    int cost() {
        return costA + costB + costC + costD + costE;
    }

    int moleculesA;
    int moleculesB;
    int moleculesC;
    int moleculesD;
    int moleculesE;

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
    static class Guard extends ABehaviourTree<Blackboard> {
        private Function<Blackboard, Boolean> lambda;

        Guard(String name, Function<Blackboard, Boolean> lambda) {
            super(name);
            this.lambda = lambda;
        }

        @Override
        public Status process(Blackboard blackboard) {
            if (lambda.apply(blackboard)) {
                System.err.println(String.format("%s> [PASS] if %s", indent(), name));
                return Status.Success;
            } else {
                System.err.println(String.format("%s> [FAIL] if %s", indent(), name));
                return Status.Failure;
            }
        }
    }

    /**
     * Will push an object onto a stack
     */
    static class Push extends ABehaviourTree<Blackboard> {
        private Function<Blackboard, Optional> lambda;

        public Push(Function<Blackboard, Optional> lambda) {
            super("Push");
            this.lambda = lambda;
        }

        @Override
        public Status process(Blackboard blackboard) {
//            System.err.println(String.format("blackboard stack <%s>", blackboard.stack));
            Optional item = lambda.apply(blackboard);
            if (item.isPresent()) {
                blackboard.stack.push(item.get());
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
    static class Store extends ABehaviourTree<Blackboard> {
        private final String key;
        private final Function<Blackboard, Optional> lambda;

        Store(String key, Function<Blackboard, Optional> lambda) {
            super("Store");
            this.key = key;
            this.lambda = lambda;
        }

        @Override
        public Status process(Blackboard blackboard) {
            Optional value = lambda.apply(blackboard);
            if (value.isPresent()) {
                System.err.println(String.format("%s> [PASS] %s %s=%s ", indent(), name, key, value.get()));
                blackboard.store.put(key, value.get());
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
    static class Pop extends ABehaviourTree<Blackboard> {
        public Pop() {
            super("Pop");
        }

        @Override
        public Status process(Blackboard blackboard) {
            if (blackboard.stack.isEmpty()) {
                return Status.Failure;
            } else {
                Object item = blackboard.stack.pop();
                System.err.println(String.format("%s> [%s] <%s>", indent(), name, item));
                return Status.Success;
            }
        }
    }

    /**
     * Will execute a lambda and report success if no exceptions occur
     */
    static class Execute extends ABehaviourTree<Blackboard> {
        private Consumer<Blackboard> lambda;

        public Execute(String name, Consumer<Blackboard> lambda) {
            super(name);
            this.lambda = lambda;
        }

        @Override
        public Status process(Blackboard blackboard) {
            try {
                lambda.accept(blackboard);
            } catch (Exception e) {
                System.err.println(String.format("%s> [FAIL] Execute %s", indent(), name));
                e.printStackTrace(System.err);
                return Status.Failure;
            }
            System.err.println(String.format("%s> [PASS] Execute %s", indent(), name));
            return Status.Success;
        }
    }

    public enum Status {
        Running, Failure, Success
    }

}
