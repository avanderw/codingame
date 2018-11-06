import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

class Player {

    public static void main(String args[]) {
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
                    String target = in.next();
                    Integer eta = in.nextInt();
                    Integer score = in.nextInt();
                    Integer storageA = in.nextInt();
                    Integer storageB = in.nextInt();
                    Integer storageC = in.nextInt();
                    Integer storageD = in.nextInt();
                    Integer storageE = in.nextInt();
                    Integer expertiseA = in.nextInt();
                    Integer expertiseB = in.nextInt();
                    Integer expertiseC = in.nextInt();
                    Integer expertiseD = in.nextInt();
                    Integer expertiseE = in.nextInt();

                    bb.playerAgent.target = target;
                    bb.playerAgent.eta = eta;
                    bb.playerAgent.score = score;
                    bb.playerAgent.validateStorageBags(MoleculeType.A, storageA);
                    bb.playerAgent.validateStorageBags(MoleculeType.B, storageB);
                    bb.playerAgent.validateStorageBags(MoleculeType.C, storageC);
                    bb.playerAgent.validateStorageBags(MoleculeType.D, storageD);
                    bb.playerAgent.validateStorageBags(MoleculeType.E, storageE);
                    bb.playerAgent.validateExpertiseBag(MoleculeType.A, expertiseA);
                    bb.playerAgent.validateExpertiseBag(MoleculeType.B, expertiseB);
                    bb.playerAgent.validateExpertiseBag(MoleculeType.C, expertiseC);
                    bb.playerAgent.validateExpertiseBag(MoleculeType.D, expertiseD);
                    bb.playerAgent.validateExpertiseBag(MoleculeType.E, expertiseE);
                }),
                new ABehaviourTree.Execute("ReadEnemyState", (bb) -> {
                    String target = in.next();
                    Integer eta = in.nextInt();
                    Integer score = in.nextInt();
                    Integer storageA = in.nextInt();
                    Integer storageB = in.nextInt();
                    Integer storageC = in.nextInt();
                    Integer storageD = in.nextInt();
                    Integer storageE = in.nextInt();
                    Integer expertiseA = in.nextInt();
                    Integer expertiseB = in.nextInt();
                    Integer expertiseC = in.nextInt();
                    Integer expertiseD = in.nextInt();
                    Integer expertiseE = in.nextInt();

                    bb.enemyAgent.target = target;
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
                    Integer sampleCount = in.nextInt();
                    for (int i = 0; i < sampleCount; i++) {

                        Integer id = in.nextInt();
                        Integer carriedBy = in.nextInt();
                        Integer rank = in.nextInt();
                        String expertiseGain = in.next();
                        Integer health = in.nextInt();
                        Integer costA = in.nextInt();
                        Integer costB = in.nextInt();
                        Integer costC = in.nextInt();
                        Integer costD = in.nextInt();
                        Integer costE = in.nextInt();

                        Sample sample = new Sample(id, rank, health, MoleculeType.valueOf(expertiseGain.toUpperCase()));
                        sample.requiredMoleculeBag.put(MoleculeType.A, new AtomicInteger(costA));
                        sample.requiredMoleculeBag.put(MoleculeType.B, new AtomicInteger(costB));
                        sample.requiredMoleculeBag.put(MoleculeType.C, new AtomicInteger(costC));
                        sample.requiredMoleculeBag.put(MoleculeType.D, new AtomicInteger(costD));
                        sample.requiredMoleculeBag.put(MoleculeType.E, new AtomicInteger(costE));
                        switch (carriedBy) {
                            case -1:
                                bb.cloudStorage.storeSampleIfAbsent(sample);
                                bb.enemyAgent.removeSampleIfPresent(sample);
                                break;
                            case 0:
                                bb.playerAgent.assertCarryingSample(sample);
                                bb.cloudStorage.assertNotStoringSample(sample);
                                bb.enemyAgent.assertNotCarryingSample(sample);
                                break;
                            case 1:
                                bb.enemyAgent.carrySampleIfAbsent(sample);
                                bb.cloudStorage.removeSampleIfPresent(sample);
                                break;
                            default:
                                throw new RuntimeException("unhandled carrier");
                        }
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
                                new ABehaviourTree.Guard("isAtSamples", bb -> bb.playerAgent.target.equals("SAMPLES")),
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
                                                                                (bb.playerAgent.expertise.get(MoleculeType.A).get() >= 2 ? 1 : 0) +
                                                                                        (bb.playerAgent.expertise.get(MoleculeType.B).get() >= 2 ? 1 : 0) +
                                                                                        (bb.playerAgent.expertise.get(MoleculeType.C).get() >= 2 ? 1 : 0) +
                                                                                        (bb.playerAgent.expertise.get(MoleculeType.D).get() >= 2 ? 1 : 0) +
                                                                                        (bb.playerAgent.expertise.get(MoleculeType.E).get() >= 2 ? 1 : 0)
                                                                        ),
                                                                        new ABehaviourTree.Guard("has2Rank1Samples", bb -> bb.playerAgent.carriedSamples().stream().filter(s -> s.rank == 1).count() == 2)
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
                                                        new ABehaviourTree.Guard("isCarryingUndiagnosedSamples", bb -> !bb.playerAgent.undiagnosedSamples.isEmpty()),
                                                        new ABehaviourTree.Execute("gotoDiagnosis", bb -> System.out.println("GOTO DIAGNOSIS"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToMolecules",
                                                        new ABehaviourTree.Guard("areAnySamplesIncomplete", bb -> !bb.playerAgent.incompleteSamples.isEmpty()),
                                                        new ABehaviourTree.Execute("gotoMolecules", bb -> System.out.println("GOTO MOLECULES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToLaboratory",
                                                        new ABehaviourTree.Guard("ifThereAreCompleteSamples", bb -> !bb.playerAgent.completeSamples.isEmpty()),
                                                        new ABehaviourTree.Execute("gotoLaboratory", bb -> System.out.println("GOTO LABORATORY"))
                                                )
                                        )
                                )
                        ),
                        new ABehaviourTree.Sequence("AtDiagnosis",
                                new ABehaviourTree.Guard("isAtDiagnosis", bb -> bb.playerAgent.target.equals("DIAGNOSIS")),
                                new ABehaviourTree.Selector("SelectDiagnosisAction",
                                        new ABehaviourTree.Sequence("DiagnoseUndiagnosedSamples",
                                                new ABehaviourTree.Guard("isCarryingUndiagnosedSamples", bb -> !bb.playerAgent.undiagnosedSamples.isEmpty()),
                                                new ABehaviourTree.Guard("isSampleIdentified", bb -> !Objects.isNull(bb.store.get("diagnosis:undiagnosed-sample"))),
                                                new ABehaviourTree.Execute("diagnoseSample", bb -> System.out.println(String.format("CONNECT %s", ((Sample) bb.store.get("diagnosis:undiagnosed-sample")).id)))
                                        ),
                                        new ABehaviourTree.Selector("LeaveDiagnosis",
                                                new ABehaviourTree.Sequence("GoToSamples",
                                                        new ABehaviourTree.Guard("notCarryingSamples", bb -> bb.playerAgent.carriedSamples().isEmpty()),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO SAMPLES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToLaboratory",
                                                        new ABehaviourTree.Guard("ifThereAreCompleteSamples", bb -> !bb.playerAgent.completeSamples.isEmpty()),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO LABORATORY"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToMolecules",
                                                        new ABehaviourTree.Guard("areAnySamplesIncomplete", bb -> !bb.playerAgent.incompleteSamples.isEmpty()),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO MOLECULES"))
                                                )
                                        )
                                )
                        ),

                        new ABehaviourTree.Sequence("AtMolecules",
                                new ABehaviourTree.Guard("isAtMolecules", bb -> bb.playerAgent.target.equals("MOLECULES")),
                                new ABehaviourTree.Selector("SelectMoleculesAction",
                                        new ABehaviourTree.Sequence("CollectMolecules",
                                                new ABehaviourTree.Guard("isThereSpaceForMolecules", bb -> bb.playerAgent.canCarryMoreMolecules()),
                                                new ABehaviourTree.Guard("areAnySamplesIncomplete", bb -> !bb.playerAgent.incompleteSamples.isEmpty()),
                                                new ABehaviourTree.Guard("isMoleculeIdentified", bb -> bb.store.containsKey("<molecule-id>") && bb.store.get("<molecule-id>") != null),
                                                new ABehaviourTree.Execute("CollectMolecule", bb -> {
                                                    System.out.println(String.format("CONNECT %s", bb.store.get("<molecule-id>")));
                                                    bb.playerAgent.collectMolecule((Sample) bb.store.get("molecule:selected-sample"), (MoleculeType) bb.store.get("molecule:selected-molecule"));
                                                })
                                        ),
                                        new ABehaviourTree.Sequence("WaitForMolecules",
                                                new ABehaviourTree.Guard("isThereSpaceForMolecules", bb -> bb.playerAgent.canCarryMoreMolecules()),
                                                new ABehaviourTree.Guard("ifThereAreNoCompleteSamples", bb -> !bb.playerAgent.completeSamples.isEmpty()),
                                                new ABehaviourTree.Guard("ifEnemyAtLaboratory", bb -> bb.enemyAgent.target.equals("LABORATORY")),
                                                new ABehaviourTree.Execute("WaitForMolecules", bb -> System.out.println("WAIT"))
                                        ),
                                        new ABehaviourTree.Selector("LeaveMolecules",
                                                new ABehaviourTree.Sequence("GoToLaboratory",
                                                        new ABehaviourTree.Guard("ifThereAreCompleteSamples", bb -> !bb.playerAgent.completeSamples.isEmpty()),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO LABORATORY"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToSamples",
                                                        new ABehaviourTree.Guard("ifThereAreNoCompleteSamples", bb -> bb.playerAgent.completeSamples.isEmpty()),
                                                        new ABehaviourTree.Guard("ifThereIsSpaceForSamples", bb -> bb.playerAgent.canCarryMoreSamples()),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO SAMPLES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToDiagnosis",
                                                        new ABehaviourTree.Guard("isCarryingUndiagnosedSamples", bb -> !bb.playerAgent.undiagnosedSamples.isEmpty()),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO DIAGNOSIS"))
                                                ),
                                                new ABehaviourTree.Execute("ExecuteWait", bb -> System.out.println("WAIT"))
                                        )
                                )
                        ),
                        new ABehaviourTree.Sequence("AtLaboratory",
                                new ABehaviourTree.Guard("isAtLaboratory", bb -> bb.playerAgent.target.equals("LABORATORY")),
                                new ABehaviourTree.Selector("SelectLaboratoryAction",
                                        new ABehaviourTree.Selector("LeaveLaboratory",
                                                new ABehaviourTree.Sequence("GoToSamples",
                                                        new ABehaviourTree.Guard("notCarryingSamples", bb -> bb.playerAgent.carriedSamples().isEmpty()),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO SAMPLES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToMolecules",
                                                        new ABehaviourTree.Guard("doesAnySampleNeedMolecules", bb -> !bb.playerAgent.incompleteSamples.isEmpty()),
                                                        new ABehaviourTree.Execute("ExecuteGoto", bb -> System.out.println("GOTO MOLECULES"))
                                                ),
                                                new ABehaviourTree.Sequence("GoToDiagnosis",
                                                        new ABehaviourTree.Guard("isCarryingUndiagnosedSamples", bb -> !bb.playerAgent.undiagnosedSamples.isEmpty()),
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
        List<Sample> undiagnosedSamples = new ArrayList<>();
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

        void collectSample(Sample sample) {
            if (carriedSamples().contains(sample)) {
                throw new NullPointerException("already carrying sample");
            }

            if (sample.isComplete(futureExpertise, expertise)) {
                completeSamples.add(sample);
            } else {
                incompleteSamples.add(sample);
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

            if (sample.isComplete(futureExpertise, expertise)) {
                throw new IllegalStateException("sample is complete but still listed in incomplete samples");
            }

            if (!canCarryMoreMolecules()) {
                throw new IllegalStateException("cannot collect more molecules as player agent is full");
            }

            sample.addMolecule(moleculeType, futureExpertise, expertise);

            if (sample.isComplete(futureExpertise, expertise)) {
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

        void assertCarryingSample(Sample sample) {
            if (!carriedSamples().contains(sample)) {
                throw new NullPointerException("agent not carrying sample");
            }
        }

        void assertNotCarryingSample(Sample sample) {
            if (carriedSamples().contains(sample)) {
                throw new NullPointerException("agent should not be carrying sample");
            }
        }
    }

    static class EnemyAgent extends PlayerAgent {
        void carrySampleIfAbsent(Sample sample) {
            if (!carriedSamples().contains(sample)) {
                collectSample(sample);
            }
        }

        void removeSampleIfPresent(Sample sample) {
            if (incompleteSamples.contains(sample)) {
                incompleteSamples.remove(sample);
            }

            if (completeSamples.contains(sample)) {
                completeSamples.remove(sample);
            }
        }
    }

    static class Sample {
        final Integer id;
        final Integer rank;
        final Integer health;
        final MoleculeType expertiseGain;
        final Map<MoleculeType, AtomicInteger> requiredMoleculeBag = new HashMap<>();
        final Map<MoleculeType, AtomicInteger> carriedMoleculeBag = new HashMap<>();

        Sample(Integer id, Integer rank, Integer health, MoleculeType expertiseGain) {
            this.id = id;
            this.rank = rank;
            this.health = health;
            this.expertiseGain = expertiseGain;
        }

        Integer carriedMoleculeCount() {
            return carriedMoleculeBag.values().stream().mapToInt(AtomicInteger::get).sum();
        }

        Boolean isComplete(Map<MoleculeType, AtomicInteger> futureExpertise, Map<MoleculeType, AtomicInteger> expertise) {
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

        void addMolecule(MoleculeType moleculeType, Map<MoleculeType, AtomicInteger> futureExpertise, Map<MoleculeType, AtomicInteger> expertise) {
            if (isComplete(futureExpertise, expertise)) {
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
            blackboard.store.clear();

            return Status.Success;
        }
    }

    static class CloudStorage {
        Set<Sample> samples = new HashSet<>();

        void storeSampleIfAbsent(Sample sample) {
            samples.add(sample);
        }

        void assertNotStoringSample(Sample sample) {
            if (samples.contains(sample)) {
                throw new NullPointerException("already storing sample");
            }
        }

        void removeSampleIfPresent(Sample sample) {
            if (samples.contains(sample)) {
                samples.remove(sample);
            }
        }
    }
}

class Blackboard {
    List<Player.Project> completeProjects = new ArrayList<>();
    List<Player.Project> incompleteProjects = new ArrayList<>();
    Player.CloudStorage cloudStorage = new Player.CloudStorage();
    Player.PlayerAgent playerAgent = new Player.PlayerAgent();
    Player.EnemyAgent enemyAgent = new Player.EnemyAgent();
    Map<Player.MoleculeType, AtomicInteger> moleculePoolMax = new HashMap<>();
    Map<Player.MoleculeType, AtomicInteger> moleculePool = new HashMap<>();

    Map<String, Object> store = new HashMap<>();
    public Stack stack = new Stack();

    void validateMoleculePool(Player.MoleculeType moleculeType, Integer count) {
        if (moleculePool.get(moleculeType).get() != count) {
            throw new IllegalStateException("blackboard state does not equal system state");
        }
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
