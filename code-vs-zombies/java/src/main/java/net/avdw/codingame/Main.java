package net.avdw.codingame;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Save humans, destroy zombies!
 **/
class Main {

    private static final int Z_MOVE = 400;
    private static final int MY_MOVE = 1000;
    private static final int MAX_RESPONSE = 100;
    private static long turnStart = 0;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        // game loop
        while (true) {
            turnStart = System.currentTimeMillis();
            int x = in.nextInt();
            int y = in.nextInt();
            int humanCount = in.nextInt();
            List<Human> humans = new ArrayList<>();
            for (int i = 0; i < humanCount; i++) {
                int humanId = in.nextInt();
                int humanX = in.nextInt();
                int humanY = in.nextInt();
                humans.add(new Human(humanId, humanX, humanY));
            }
            List<Zombie> zombies = new ArrayList<>();
            int zombieCount = in.nextInt();
            for (int i = 0; i < zombieCount; i++) {
                int zombieId = in.nextInt();
                int zombieX = in.nextInt();
                int zombieY = in.nextInt();
                int zombieXNext = in.nextInt();
                int zombieYNext = in.nextInt();
                zombies.add(new Zombie(zombieId, zombieX, zombieY, zombieXNext, zombieYNext));
            }

            Position closestHuman = null;
            List<Human> canSave = canSave(x, y, humans, zombies);
            if (canSave.isEmpty()) {
                closestHuman = closest(x, y, humans.stream().map(human -> (Position) human).collect(Collectors.toList()));
            } else {
                closestHuman = closest(x, y, canSave.stream().map(human -> (Position) human).collect(Collectors.toList()));
            }

            System.out.printf("%d %d%n", closestHuman.getX(), closestHuman.getY());
        }
    }

    private static Position simulate(final State state) {
        int maxResult = 0;
        Position maxPosition = null;
        while (System.currentTimeMillis() - turnStart < MAX_RESPONSE) {
            State current = new State();
            current.humans = new ArrayList<>(state.humans);
            current.zombies = new ArrayList<>(state.zombies);
            current.me = new Position() {
                @Override
                public int getX() {
                    return state.me.getX();
                }

                @Override
                public int getY() {
                    return state.me.getY();
                }
            };

            State next = step(current);
            Position first = next.me;
            next = step(next);
            next = step(next);
            int result = evaluate(next);
            if (result > maxResult) {
                maxResult = result;
                maxPosition = first;
            }
        }
        return maxPosition;
    }

    private static int evaluate(State state) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static State step(State state) {
        State next = new State();
        next.humans = state.humans;
        List<Position> humans = state.humans.stream().map(human -> (Position) human).collect(Collectors.toList());
        humans.add(state.me);
        for (Zombie zombie : state.zombies) {
            Position closestHuman = closest(zombie.x1, zombie.y1, humans);
            Position moveZombie = move(zombie, closestHuman, Z_MOVE);
            next.zombies.add(new Zombie(zombie.id, zombie.x2, zombie.y2, moveZombie.getX(), moveZombie.getY()));
        }
        next.me = move(state.me, fullRandom(), MY_MOVE);
        return next;
    }

    private static Position fullRandom() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static Position move(Position from, Position to, int maxDistance) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static List<Human> canSave(int myX, int myY, List<Human> humans, List<Zombie> zombies) {
        if (humans.isEmpty()) {
            throw new IllegalArgumentException("No humans to save");
        }
        if (zombies.isEmpty()) {
            throw new IllegalArgumentException("No zombies to destroy");
        }
        List<Human> canSave = new ArrayList<>();
        for (Human human : humans) {
            Position closestZombie = closest(human.x, human.y, zombies.stream().map(zombie -> (Position) zombie).collect(Collectors.toList()));
            int zDist = distance(human.x, human.y, closestZombie.getX(), closestZombie.getY());
            int myDist = distance(myX, myY, human.x, human.y);
            if (Math.ceil(zDist * 1. / Z_MOVE) > Math.floor(myDist * 1. / MY_MOVE)) {
                canSave.add(human);
            }
        }
        return canSave;
    }

    private static Position closest(int x, int y, List<Position> positions) {
        if (positions.isEmpty()) {
            throw new IllegalArgumentException("No positions");
        }
        Position closestPosition = null;
        int closestDistance = Integer.MAX_VALUE;
        for (Position position : positions) {
            int distance = distance(x, y, position.getX(), position.getY());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPosition = position;
            }
        }
        return closestPosition;
    }

    private static int distance(int x, int y, int x1, int y1) {
        return (int) Math.sqrt(Math.pow(x - x1, 2) + Math.pow(y - y1, 2));
    }
}