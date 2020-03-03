import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

class Player {
// ************************************
// ** CompositeNode.java
// ************************************

public abstract static class CompositeNode implements Node {
    private final List<Node> children = new ArrayList<>();
    protected CompositeNode() {
    }
    public void addChild(Node child) {
        children.add(child);
    }
    protected List<Node> getChildren() {
        return children;
    }
}

// ************************************
// ** ParallelNode.java
// ************************************

public static class ParallelNode extends CompositeNode {
    public ParallelNode() {
    }
    @Override
    public NodeStatus tick() {
        throw new UnsupportedOperationException();
    }
}

// ************************************
// ** PriorityNode.java
// ************************************

public static class PriorityNode extends CompositeNode {
    public PriorityNode() {
        super();
    }
    @Override
    public NodeStatus tick() {
        System.err.println(String.format("%s -> [%s]", this.getClass().getSimpleName(), childrenNames()));
        for (Node child : getChildren()) {
            System.err.println(String.format("%s -> %s", this.getClass().getSimpleName(), child.getClass().getSimpleName()));
            NodeStatus status = child.tick();
            System.err.println(String.format("%s <- %s <- %s", this.getClass().getSimpleName(), child.getClass().getSimpleName(), status));
            switch (status) {
                case SUCCESS:
                case RUNNING:
                    System.err.println(String.format("%s <- %s", this.getClass().getSimpleName(), status));
                    return status;
                case FAILURE:
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
        System.err.println(String.format("%s <- %s", this.getClass().getSimpleName(), NodeStatus.FAILURE));
        return NodeStatus.FAILURE;
    }
    private String childrenNames() {
        return getChildren().stream().map(child->child.getClass().getSimpleName()).collect(Collectors.joining(", "));
    }
}

// ************************************
// ** SequenceNode.java
// ************************************

public static class SequenceNode extends CompositeNode {
    public SequenceNode() {
    }
    @Override
    public NodeStatus tick() {
        System.err.println(String.format("%s -> [%s]", this.getClass().getSimpleName(), childrenNames()));
        for (Node child : getChildren()) {
            System.err.println(String.format("%s -> %s", this.getClass().getSimpleName(), child.getClass().getSimpleName()));
            NodeStatus status = child.tick();
            System.err.println(String.format("%s <- %s <- %s", this.getClass().getSimpleName(), child.getClass().getSimpleName(), status));
            switch (status) {
                case FAILURE:
                case RUNNING:
                    System.err.println(String.format("%s <- %s", this.getClass().getSimpleName(), status));
                    return status;
                case SUCCESS:
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
        System.err.println(String.format("%s <- %s", this.getClass().getSimpleName(), NodeStatus.SUCCESS));
        return NodeStatus.SUCCESS;
    }
    private String childrenNames() {
        return getChildren().stream().map(child->child.getClass().getSimpleName()).collect(Collectors.joining(", "));
    }
}

// ************************************
// ** DecoratorNode.java
// ************************************

public abstract static class DecoratorNode implements Node {
    private final Node child;
    protected DecoratorNode(final Node child) {
        this.child = child;
    }
    public Node getChild() {
        return child;
    }
}

// ************************************
// ** InverterNode.java
// ************************************

public static class InverterNode extends DecoratorNode {
    public InverterNode(final Node child) {
        super(child);
    }
    @Override
    public NodeStatus tick() {
        NodeStatus nodeStatus = getChild().tick();
        switch (nodeStatus) {
            case SUCCESS:
                return NodeStatus.FAILURE;
            case FAILURE:
                return NodeStatus.SUCCESS;
            case RUNNING:
                return NodeStatus.RUNNING;
            default:
                throw new UnsupportedOperationException();
        }
    }
}

// ************************************
// ** RepeaterNode.java
// ************************************

public static class RepeaterNode extends DecoratorNode {
    public RepeaterNode(final Node child) {
        super(child);
    }
    @Override
    public NodeStatus tick() {
        NodeStatus nodeStatus = getChild().tick();
        while (nodeStatus.equals(NodeStatus.SUCCESS) || nodeStatus.equals(NodeStatus.RUNNING)) {
            nodeStatus = getChild().tick();
        }
        return nodeStatus;
    }
}

// ************************************
// ** ActionNode.java
// ************************************

public static interface ActionNode extends LeafNode {
}

// ************************************
// ** ConditionNode.java
// ************************************

public static interface ConditionNode extends LeafNode {
}

// ************************************
// ** LeafNode.java
// ************************************

public static interface LeafNode extends Node {
}

// ************************************
// ** Node.java
// ************************************

@FunctionalInterface
public static interface Node {
    NodeStatus tick();
}

// ************************************
// ** NodeStatus.java
// ************************************

public static enum NodeStatus {
    SUCCESS,
    FAILURE,
    RUNNING
}

// ************************************
// ** RootNode.java
// ************************************

public static class RootNode implements Node {
    private final Node child;
    public RootNode(final Node child) {
        this.child = child;
    }
    @Override
    public NodeStatus tick() {
        return child.tick();
    }
}

// ************************************
// ** BehaviourTree.java
// ************************************

public static class BehaviourTree extends SequenceNode {
    public BehaviourTree(final RobotPlayer robotPlayer) {
        addChild(new GatherSamplesTree(robotPlayer));
        addChild(new AnalyseSamplesTree());
        addChild(new GatherMoleculesTree());
        addChild(new ProduceMedicineTree());
        addChild(new MovementTree(robotPlayer));
        addChild(new GoToModuleAction(ModuleType.SAMPLES));
    }
}

// ************************************
// ** AnalyseSampleAction.java
// ************************************

public static class AnalyseSampleAction implements ActionNode {
    @Override
    public NodeStatus tick() {
        return NodeStatus.SUCCESS;
    }
}

// ************************************
// ** AnalyseSamplesTree.java
// ************************************

public static class AnalyseSamplesTree extends PriorityNode {
    public AnalyseSamplesTree() {
        addChild(new AnalyseSampleAction());
    }
}

// ************************************
// ** DiagnosisModule.java
// ************************************

public static class DiagnosisModule {
}

// ************************************
// ** LaboratoryModule.java
// ************************************

public static class LaboratoryModule {
}

// ************************************
// ** ProduceMedicineAction.java
// ************************************

public static class ProduceMedicineAction implements ActionNode {
    @Override
    public NodeStatus tick() {
        return NodeStatus.SUCCESS;
    }
}

// ************************************
// ** ProduceMedicineTree.java
// ************************************

public static class ProduceMedicineTree extends PriorityNode {
    public ProduceMedicineTree() {
        addChild(new ProduceMedicineAction());
    }
}

// ************************************
// ** MedicalComplex.java
// ************************************

public static class MedicalComplex {
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

// ************************************
// ** MedicalComplexReader.java
// ************************************

public static class MedicalComplexReader {
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

// ************************************
// ** ModuleType.java
// ************************************

public static enum ModuleType {
    START_POS, DIAGNOSIS, LABORATORY, MOLECULES, SAMPLES
}

// ************************************
// ** GatherMoleculeAction.java
// ************************************

public static class GatherMoleculeAction implements ActionNode {
    @Override
    public NodeStatus tick() {
        return NodeStatus.SUCCESS;
    }
}

// ************************************
// ** GatherMoleculesTree.java
// ************************************

public static class GatherMoleculesTree extends PriorityNode {
    public GatherMoleculesTree() {
        addChild(new GatherMoleculeAction());
    }
}

// ************************************
// ** MoleculeCarryLimitCondition.java
// ************************************

public static class MoleculeCarryLimitCondition {
    private final int maxCarryCount;
    MoleculeCarryLimitCondition(int maxCarryCount) {
        this.maxCarryCount = maxCarryCount;
    }
}

// ************************************
// ** MoleculesModule.java
// ************************************

public static class MoleculesModule {
}

// ************************************
// ** MoleculeType.java
// ************************************

public static enum MoleculeType {
    A,B,C,D,E, UNKNOWN
}

// ************************************
// ** AtModuleCondition.java
// ************************************

public static class AtModuleCondition implements Node {
    private final RobotPlayer robotPlayer;
    private final ModuleType moduleType;
    public AtModuleCondition(final RobotPlayer robotPlayer, final ModuleType moduleType) {
        this.robotPlayer = robotPlayer;
        this.moduleType = moduleType;
    }
    @Override
    public NodeStatus tick() {
        if (moduleType.equals(robotPlayer.target) && robotPlayer.eta == 0) {
            return NodeStatus.SUCCESS;
        } else {
            return NodeStatus.FAILURE;
        }
    }
}

// ************************************
// ** GoToDiagnosisTree.java
// ************************************

public static class GoToDiagnosisTree extends SequenceNode {
    public GoToDiagnosisTree() {
        addChild(new GoToModuleAction(ModuleType.DIAGNOSIS));
    }
}

// ************************************
// ** GoToLaboratoryTree.java
// ************************************

public static class GoToLaboratoryTree extends SequenceNode {
    public GoToLaboratoryTree() {
        addChild(new GoToModuleAction(ModuleType.LABORATORY));
    }
}

// ************************************
// ** GoToModuleAction.java
// ************************************

public static class GoToModuleAction implements ActionNode {
    private final ModuleType moduleType;
    public GoToModuleAction(final ModuleType moduleType) {
        this.moduleType = moduleType;
    }
    @Override
    public NodeStatus tick() {
        System.out.println(String.format("GOTO %s", moduleType));
        return NodeStatus.SUCCESS;
    }
}

// ************************************
// ** GoToMoleculesTree.java
// ************************************

public static class GoToMoleculesTree extends SequenceNode {
    public GoToMoleculesTree() {
        addChild(new GoToModuleAction(ModuleType.MOLECULES));
    }
}

// ************************************
// ** GoToSamplesTree.java
// ************************************

public static class GoToSamplesTree extends PriorityNode {
    public GoToSamplesTree(final RobotPlayer robotPlayer) {
        addChild(new AtModuleCondition(robotPlayer, ModuleType.SAMPLES));
        addChild(new GoToModuleAction(ModuleType.SAMPLES));
    }
}

// ************************************
// ** MovementTree.java
// ************************************

public static class MovementTree extends PriorityNode {
    public MovementTree(final RobotPlayer robotPlayer) {
        addChild(new GoToSamplesTree(robotPlayer));
        addChild(new GoToDiagnosisTree());
        addChild(new GoToMoleculesTree());
        addChild(new GoToLaboratoryTree());
    }
}

// ************************************
// ** RobotMovementMatrix.java
// ************************************

public static class RobotMovementMatrix {
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

// ************************************
// ** RobotPlayer.java
// ************************************

public static class RobotPlayer {
    public final ModuleType target;
    public final int eta;
    public final int score;
    public final Map<MoleculeType, Integer> storedMoleculeMap = new HashMap<MoleculeType, Integer>();
    public final Map<MoleculeType, Integer> expertiseMoleculeMap = new HashMap<MoleculeType, Integer>();
    public final List<Sample> storedSampleList = new ArrayList<>();
    public RobotPlayer(final ModuleType target, final int eta, final int score, final int storageA, final int storageB, final int storageC, final int storageD, final int storageE, final int expertiseA, final int expertiseB, final int expertiseC, final int expertiseD, final int expertiseE) {
        this.target = target;
        this.eta = eta;
        this.score = score;
        storedMoleculeMap.put(MoleculeType.A, storageA);
        storedMoleculeMap.put(MoleculeType.B, storageB);
        storedMoleculeMap.put(MoleculeType.C, storageC);
        storedMoleculeMap.put(MoleculeType.D, storageD);
        storedMoleculeMap.put(MoleculeType.E, storageE);
        expertiseMoleculeMap.put(MoleculeType.A, expertiseA);
        expertiseMoleculeMap.put(MoleculeType.B, expertiseB);
        expertiseMoleculeMap.put(MoleculeType.C, expertiseC);
        expertiseMoleculeMap.put(MoleculeType.D, expertiseD);
        expertiseMoleculeMap.put(MoleculeType.E, expertiseE);
    }
    @Override
    public String toString() {
        return "RobotPlayer{" +
                "target=" + target +
                ", eta=" + eta +
                ", score=" + score +
                ", storedMoleculeMap=" + storedMoleculeMap +
                ", expertiseMoleculeMap=" + expertiseMoleculeMap +
                '}';
    }
    public void addSample(final Sample sample) {
        storedSampleList.add(sample);
    }
}

// ************************************
// ** RobotPlayerReader.java
// ************************************

public static class RobotPlayerReader {
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

// ************************************
// ** CollectSampleAction.java
// ************************************

public static class CollectSampleAction implements ActionNode {
    private final Integer rank;
    CollectSampleAction(Integer rank) {
        this.rank = rank;
    }
    @Override
    public NodeStatus tick() {
        System.out.println(String.format("CONNECT %s", rank));
        return NodeStatus.SUCCESS;
    }
}

// ************************************
// ** GatherSamplesTree.java
// ************************************

public static class GatherSamplesTree extends PriorityNode {
    public GatherSamplesTree(final RobotPlayer robotPlayer) {
        addChild(new InverterNode(new AtModuleCondition(robotPlayer, ModuleType.SAMPLES)));
        addChild(new SampleCarryLimitCondition(robotPlayer));
        addChild(new CollectSampleAction(1));
    }
}

// ************************************
// ** Sample.java
// ************************************

public static class Sample {
    public final int sampleId;
    public final SampleOwner carriedBy;
    public final int rank;
    public final MoleculeType expertiseGain;
    public final int health;
    public final Map<MoleculeType, Integer> moleculeCostMap = new HashMap<MoleculeType, Integer>();
    public final SampleState sampleState;
    public Sample(final int sampleId, final SampleOwner carriedBy, final int rank, final MoleculeType expertiseGain, final int health, final int costA, final int costB, final int costC, final int costD, final int costE) {
        this.sampleId = sampleId;
        this.carriedBy = carriedBy;
        this.rank = rank;
        this.expertiseGain = expertiseGain;
        this.health = health;
        moleculeCostMap.put(MoleculeType.A, costA);
        moleculeCostMap.put(MoleculeType.B, costB);
        moleculeCostMap.put(MoleculeType.C, costC);
        moleculeCostMap.put(MoleculeType.D, costD);
        moleculeCostMap.put(MoleculeType.E, costE);
        if (expertiseGain.equals(MoleculeType.UNKNOWN)) {
            sampleState = SampleState.UNDIAGNOSED;
        } else {
            sampleState = SampleState.DIAGNOSED;
        }
    }
    @Override
    public String toString() {
        return "SampleData{" +
                "sampleId=" + sampleId +
                ", carriedBy=" + carriedBy +
                ", rank=" + rank +
                ", expertiseGain=" + expertiseGain +
                ", health=" + health +
                ", sampleDataState=" + sampleState +
                ", moleculeCostMap=" + moleculeCostMap +
                '}';
    }
}

// ************************************
// ** SampleCarryLimitCondition.java
// ************************************

public static class SampleCarryLimitCondition implements ConditionNode {
    public static final int CARRY_LIMIT = 3;
    private final RobotPlayer robotPlayer;
    SampleCarryLimitCondition(final RobotPlayer robotPlayer) {
        this.robotPlayer = robotPlayer;
    }
    @Override
    public NodeStatus tick() {
        if (robotPlayer.storedSampleList.size() == CARRY_LIMIT) {
            return NodeStatus.SUCCESS;
        } else {
            return NodeStatus.FAILURE;
        }
    }
}

// ************************************
// ** SampleOwner.java
// ************************************

public static enum SampleOwner {
    ME, OTHER, CLOUD
}

// ************************************
// ** SampleReader.java
// ************************************

public static class SampleReader {
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

// ************************************
// ** SamplesModule.java
// ************************************

public static class SamplesModule {
}

// ************************************
// ** SampleState.java
// ************************************

public static enum SampleState {
    DIAGNOSED, UNDIAGNOSED
}

// ************************************
// ** ScienceProject.java
// ************************************

public static class ScienceProject {
    public final Map<MoleculeType, Integer> moleculeTypeCost = new HashMap<MoleculeType, Integer>();
    public ScienceProject(final int a, final int b, final int c, final int d, final int e) {
        moleculeTypeCost.put(MoleculeType.A, a);
        moleculeTypeCost.put(MoleculeType.B, b);
        moleculeTypeCost.put(MoleculeType.C, c);
        moleculeTypeCost.put(MoleculeType.D, d);
        moleculeTypeCost.put(MoleculeType.E, e);
    }
    @Override
    public String toString() {
        return "ScienceProject{" +
                "moleculeTypeCost=" + moleculeTypeCost +
                '}';
    }
}

// ************************************
// ** ScienceProjectReader.java
// ************************************

public static class ScienceProjectReader {
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