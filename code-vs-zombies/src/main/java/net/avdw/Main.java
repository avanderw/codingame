package net.avdw;

import java.util.*;

/**
 * The order in which actions happens in between two rounds is:
 * Zombies move towards their targets.
 * Ash moves towards his target.
 * Any zombie within a 2000 unit range around Ash is destroyed.
 * Zombies eat any human they share coordinates with.
 * <p>
 * Scoring works as follows:
 * A zombie is worth the number of humans still alive squared x10, not including Ash.
 * If several zombies are destroyed during on the same round,
 * the nth zombie killed's worth is multiplied by the (n+2)th number of the Fibonnacci sequence (1, 2, 3, 5, 8, and so on).
 * As a consequence, you should kill the maximum amount of zombies during a same turn.
 **/
class Main {

    // constraints
    static final int minX = 0;
    static final int maxX = 16_000;
    static final int minY = 0;
    static final int maxY = 9_000;
    static final int minHuman = 0;
    static final int maxHuman = 100;
    static final int minZombies = 0;
    static final int maxZombies = 100;
    static final int maxResponseTime = 150_000_000;

    // arguments
    static final int zombieSpeed = 400;
    static final int mySpeed = 1000;
    static final int attackZone = 2000;
    static Strategy best;

    public static void main(String[] args) {
        long maxCount = 0;
        long score = 0;
        Scanner in = new Scanner(System.in);
        // game loop
        while (true) {
            long start = System.nanoTime();
            String input = "";
            int x = in.nextInt();
            int y = in.nextInt();
            Entity me = new Entity(x, y);

            List<Human> humanList = new ArrayList<>();
            int humanCount = in.nextInt();
            input += String.format("%s %s %s", x, y, humanCount);
            for (int i = 0; i < humanCount; i++) {
                int humanId = in.nextInt();
                int humanX = in.nextInt();
                int humanY = in.nextInt();
                humanList.add(new Human(humanId, humanX, humanY));
                input += String.format(" %s %s %s", humanId, humanX, humanY);
            }

            List<Zombie> zombieList = new ArrayList<>();
            int zombieCount = in.nextInt();
            input += String.format(" %s", zombieCount);
            for (int i = 0; i < zombieCount; i++) {
                int zombieId = in.nextInt();
                int zombieX = in.nextInt();
                int zombieY = in.nextInt();
                int zombieXNext = in.nextInt();
                int zombieYNext = in.nextInt();
                zombieList.add(new Zombie(zombieId, zombieX, zombieY, zombieXNext, zombieYNext));
                input += String.format(" %s %s %s %s %s", zombieId, zombieX, zombieY, zombieXNext, zombieYNext);
            }


            /*
              Ash works as follows:
              Ash can be told to move to any point within the game zone by outputting a coordinate X Y. The top-left point is 0 0.
              Each turn, Ash will move exactly 1000 units towards the target coordinate, or onto the target coordinates if he is less than 1000 units away.
              If at the end of a turn, a zombie is within 2000 units of Ash, he will shoot that zombie and destroy it. More details on combat further down.
             */
            Target target = new Target(0, 0, "Error");
            long count = 0;
            long end = System.nanoTime();
            while (end - start < maxResponseTime) {
                count++;
                updateTargetMonteMax(target, zombieList, humanList, me);
                maxCount = Math.max(count, maxCount);
                end = System.nanoTime();
            }
            System.err.printf("iterated %s times (max=%s)%n", count, maxCount);

            input += String.format(":%s %s", target.x, target.y);
            System.err.println(input);
            System.out.printf("%s %s %s%n", target.x, target.y, target.message);
        }
    }



    private static void updateTargetSimple(final Target target, final List<Zombie> zombieList, final List<Human> humanList, final Entity my) {
        Optional<Human> closestHuman = humanList.stream().min(Comparator.comparingDouble(human -> Math.pow(my.y - human.y, 2) + Math.pow(my.x - human.x, 2)));
        if (closestHuman.isPresent()) {
            target.x = closestHuman.get().x;
            target.y = closestHuman.get().y;
            target.message = "Coming";
        }
    } // 1_870_702 max iterations

    private static void updateTargetMonteMax(final Target target, final List<Zombie> zombieList, final List<Human> humanList, final Entity my) {
        Strategy strategy = generateStrategyMonteMax(zombieList);
        simulateStrategyMonteMax(strategy);

        if (best == null || strategy.score > best.score) {
            best = strategy;
        }

        Target next = best.nextTarget();
        target.x = next.x;
        target.y = next.y;
        target.message = next.message;
    } // 3_162_991 max iterations

    private static Strategy generateStrategyMonteMax(final List<Zombie> zombieList) {
        Strategy strategy = new Strategy();
        int moves = random.nextInt(3);
        for (int i = 0; i < moves; i++) {
            strategy.add(new Target(random.nextInt(maxX), random.nextInt(maxY), "Move"));
        }
        zombieList.forEach(zombie -> strategy.add(new Target(zombie.xNext, zombie.yNext, "Zombie")));
        strategy.nextTarget = new Target(0,0,"MonteMax");
        return strategy;
    } // 777_449 max iterations

    static Random random = new Random();
    private static void simulateStrategyMonteMax(final Strategy strategy) {
        for (int i = 0; i < strategy.targetList.size(); i++) {
            // move zombies

            // move me
            Target target = strategy.targetList.get(i);

            // kill zombies
            // eat humans
        }
    }

    private static void updateTargetMax(final Target target, final List<Zombie> zombieList, final List<Human> humanList, final Entity my) {

    } // 3_448_883 max iterations
}