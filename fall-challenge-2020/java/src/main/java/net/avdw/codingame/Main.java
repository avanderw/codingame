package net.avdw.codingame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Main {
    private static Action refresh = new Action(0, ActionType.REST, 0, 0, 0, 0, 0, 0, 0, true, true);

    public static int dist(final Inventory inv, final Action target) {
        int diff0 = Math.abs(-target.delta0 - inv.inv0);
        int diff1 = Math.abs(-target.delta1 - inv.inv1);
        int diff2 = Math.abs(-target.delta2 - inv.inv2);
        int diff3 = Math.abs(-target.delta3 - inv.inv3);

        // modifiers should be determined by amount of actions to generate
        return diff0 + diff1 * 2 + diff2 * 4 + diff3 * 8;
    }

    private static List<Node> followParentToRoot(final Node node) {
        List<Node> path = new ArrayList<>();
        Node current = node;
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path);
        path.add(path.remove(0));
        return path;
    }

    public static List<Node> grow(final List<Node> leaves, final List<Action> actions, int depth) {
//        System.err.printf("%2s. Growing %s leaves%n", depth, leaves.size());
        List<Node> newLeaves = new ArrayList<>();
        leaves.forEach(leaf -> {
            List<Action> refreshedActions = new ArrayList<>();
            if (leaf.action.type == ActionType.REST) {
                refreshedActions.addAll(actions.stream()
                        .map(spell -> new Action(spell, true))
                        .collect(Collectors.toList()));
            } else {
                refreshedActions.addAll(actions.stream().filter(action -> action.castable).collect(Collectors.toList()));
            }
            refreshedActions.add(refresh);
            refreshedActions.stream()
                    .filter(action -> action.castable)
                    .forEach(castableAction -> {
                        Action castedAction = new Action(castableAction, false);
                        Node newLeaf = new Node(updateInventory(leaf.inv, castedAction), castedAction, depth);
                        if (newLeaf.inv.valid()) {
                            leaf.children.add(newLeaf);
                            newLeaf.parent = leaf;
                            newLeaves.add(newLeaf);
                        }
                    });
        });
        return newLeaves;
    }

    public static void main(String[] args) {
        System.err.println("# Fall Challenge 2020");
        Scanner in = new Scanner(System.in);
        Map<ActionType, List<Action>> actionMap = new HashMap<>();
        // game loop
        while (true) {
            actionMap.clear();
            actionMap.put(ActionType.BREW, new ArrayList<>());
            actionMap.put(ActionType.CAST, new ArrayList<>());
            actionMap.put(ActionType.LEARN, new ArrayList<>());
            actionMap.put(ActionType.OPPONENT_CAST, new ArrayList<>());

            int actionCount = in.nextInt(); // the number of spells and recipes in play
            for (int i = 0; i < actionCount; i++) {
                int id = in.nextInt(); // the unique ID of this spell or recipe
                ActionType type = ActionType.valueOf(in.next()); // in the first league: BREW; later: CAST, OPPONENT_CAST, LEARN, BREW
                int delta0 = in.nextInt(); // tier-0 ingredient change
                int delta1 = in.nextInt(); // tier-1 ingredient change
                int delta2 = in.nextInt(); // tier-2 ingredient change
                int delta3 = in.nextInt(); // tier-3 ingredient change
                int price = in.nextInt(); // the price in rupees if this is a potion
                int tomeIndex = in.nextInt(); // in the first two leagues: always 0; later: the index in the tome if this is a tome spell, equal to the read-ahead tax; For brews, this is the value of the current urgency bonus
                int taxCount = in.nextInt(); // in the first two leagues: always 0; later: the amount of taxed tier-0 ingredients you gain from learning this spell; For brews, this is how many times you can still gain an urgency bonus
                boolean castable = in.nextInt() != 0; // in the first league: always 0; later: 1 if this is a castable player spell
                boolean repeatable = in.nextInt() != 0; // for the first two leagues: always 0; later: 1 if this is a repeatable player spell

                Action action = new Action(id, type, delta0, delta1, delta2, delta3, price, tomeIndex, taxCount, castable, repeatable);
                actionMap.get(action.type).add(action);
            }

            Inventory myInv = new Inventory(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
            Inventory theirInv = new Inventory(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());

            System.err.printf("My inventory = %s%n", myInv);
            System.err.printf("Their inventory = %s%n", theirInv);
            System.err.printf("%n### Brews%n");
            actionMap.get(ActionType.BREW).forEach(System.err::println);
            System.err.printf("%n### Spells%n");
            actionMap.get(ActionType.CAST).forEach(System.err::println);

            System.err.printf("%n### Normalising brew rewards%n");
            actionMap.get(ActionType.BREW).forEach(brew -> shortestBrewPath(myInv, brew, actionMap.get(ActionType.CAST)));
            Action target = actionMap.get(ActionType.BREW).stream().max(Comparator.comparingDouble(NormalPrice::calculate)).get();
            System.err.printf("Targeting brew %s%n", target);
            Action action = target.path.get(0).action;
            System.err.println("### Brew path");
            target.path.forEach(System.err::println);
            System.err.printf("Next action %s%n", action);
            System.out.printf("%s %s%n", action.type, action.id);
            // in the first league: BREW <id> | WAIT; later: BREW <id> | CAST <id> [<times>] | LEARN <id> | REST | WAIT
        }
    }

    private static List<Node> prune(final List<Node> leaves, final Action target) {
        int maxLeaves = 300;
        leaves.sort(Comparator.comparingInt((Node leaf) -> dist(leaf.inv, target)));
        return leaves.subList(0, Math.min(maxLeaves, leaves.size()));
    }

    public static boolean shortestBrewPath(final Inventory inventory, final Action brew, final List<Action> spells) {
        System.err.printf("Finding shortest path for %s%n", brew);
        brew.path.addAll(shortestPathToBrew(inventory, brew, spells));
        if (brew.path.size() > 1) {
            return validAction(inventory, brew.path.get(brew.path.size() - 2).action);
        } else {
            return true;
        }
    }

    public static List<Node> shortestPathToBrew(final Inventory inventory, final Action brew, final List<Action> spells) {
        int maxDepth = 6;
        int depth = 1;

        List<Node> roots = new ArrayList<>();
        int finalDepth = depth;
        spells.forEach(spell -> roots.add(new Node(inventory, brew, finalDepth)));
        List<Node> leaves = new ArrayList<>(roots);

        while (!leaves.isEmpty() && leaves.stream().noneMatch(leaf -> validAction(leaf.inv, brew))) {
            leaves = prune(leaves, brew);
            depth++;
            if (depth > maxDepth) {
//                System.err.printf("Brew is more than %s actions away%n", maxDepth);
                brew.pathFound = false;
                return followParentToRoot(leaves.get(0));
            } else {
                List<Node> newLeaves = grow(leaves, spells, depth);
                leaves.clear();
                leaves.addAll(newLeaves);
            }
        }

        Node focusLeaf = leaves.stream()
                .filter(leaf -> validAction(leaf.inv, brew))
                .collect(Collectors.toList()).get(0);
        brew.pathFound = true;
        return followParentToRoot(focusLeaf);
    }

    private static Inventory updateInventory(final Inventory inv, final Action action) {
        int inv0 = inv.inv0 + action.delta0;
        int inv1 = inv.inv1 + action.delta1;
        int inv2 = inv.inv2 + action.delta2;
        int inv3 = inv.inv3 + action.delta3;

        return new Inventory(inv0, inv1, inv2, inv3, inv.score);
    }

    public static boolean validAction(final Inventory inventory, final Action action) {
        boolean has0 = inventory.inv0 >= -action.delta0;
        boolean has1 = inventory.inv1 >= -action.delta1;
        boolean has2 = inventory.inv2 >= -action.delta2;
        boolean has3 = inventory.inv3 >= -action.delta3;

        return has0 && has1 && has2 && has3;
    }
}