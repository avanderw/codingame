package net.avdw.codingame;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MainTest {
    @Test
    public void testCanBrew() {

        Inventory inventory = new Inventory(3, 0, 0, 0, 0);
        Action brew = new Action(1, ActionType.BREW, -1, -1, -1, -3, 20, 0, 0, true, false);
        assertFalse(Main.validAction(inventory, brew));
        brew = new Action(1, ActionType.BREW, -3, 0, 0, 0, 20, 0, 0, true, false);
        assertTrue(Main.validAction(inventory, brew));

        brew = new Action(1, ActionType.BREW, -2, 0, 0, 0, 20, 0, 0, true, false);
        assertTrue(Main.validAction(inventory, brew));

        brew = new Action(1, ActionType.BREW, -4, 0, 0, 0, 20, 0, 0, true, false);
        assertFalse(Main.validAction(inventory, brew));
    }

    @Test
    public void testDiff() {
        Action brew = new Action(1, ActionType.BREW, -1, -1, -1, -1, 20, 0, 0, true, false);
        assertEquals(1, Main.dist(new Inventory(0,1,1,1,0), brew));
        assertEquals(2, Main.dist(new Inventory(1,0,1,1,0), brew));
        assertEquals(4, Main.dist(new Inventory(1,1,0,1,0), brew));
        assertEquals(8, Main.dist(new Inventory(1,1,1,0,0), brew));
        assertEquals(15, Main.dist(new Inventory(0,0,0,0,0), brew));
    }

    @Test
    public void testGrowGraph() {
        Inventory inv = new Inventory(2, 0, 0, 0, 0);
        Action brew = new Action(1, ActionType.BREW, -1, -1, -1, -3, 20, 0, 0, true, false);
        int depth = 1;
        List<Node> iter0 = Arrays.asList(new Node(inv, brew, depth), new Node(inv, brew, depth), new Node(inv, brew, depth), new Node(inv, brew, depth));
        Action spell0 = new Action(2, ActionType.CAST, 2, 0, 0, 0, 0, 0, 0, false, false);
        Action spell1 = new Action(3, ActionType.CAST, -1, 1, 0, 0, 0, 0, 0, false, false);
        Action spell2 = new Action(4, ActionType.CAST, 0, -1, 1, 0, 0, 0, 0, false, false);
        Action spell3 = new Action(5, ActionType.CAST, 0, 0, -1, 1, 0, 0, 0, false, false);
        List<Action> actions = Arrays.asList(spell0, spell1, spell2, spell3);
        List<Node> iter1 =  Main.grow(iter0, actions, depth);
        List<Node> iter2 =  Main.grow(iter1, actions, depth);
        assertEquals(iter0.size(), iter1.size());
        assertNotEquals(iter1.size(), iter2.size());
    }

    @Test
    public void testShortestPathFail() {
        Inventory inventory = new Inventory(3, 0, 0, 0, 0);
        Action brew = new Action(1, ActionType.BREW, -1, -1, -1, -3, 20, 0, 0, true, false);
        Action spell0 = new Action(2, ActionType.CAST, 2, 0, 0, 0, 0, 0, 0, true, false);
        Action spell1 = new Action(3, ActionType.CAST, -1, 1, 0, 0, 0, 0, 0, true, false);
        Action spell2 = new Action(4, ActionType.CAST, 0, -1, 1, 0, 0, 0, 0, true, false);
        Action spell3 = new Action(5, ActionType.CAST, 0, 0, -1, 1, 0, 0, 0, true, false);
        List<Action> spells = Arrays.asList(spell0, spell1, spell2, spell3);
        assertFalse(Main.shortestBrewPath(inventory, brew, spells));
    }

    @Test
    public void testShortestPathPass() {
        Inventory inventory = new Inventory(3, 0, 0, 0, 0);
        Action brew = new Action(1, ActionType.BREW, -1, -1, -1, 0, 20, 0, 0, true, false);
        Action spell0 = new Action(2, ActionType.CAST, 2, 0, 0, 0, 0, 0, 0, true, false);
        Action spell1 = new Action(3, ActionType.CAST, -1, 1, 0, 0, 0, 0, 0, true, false);
        Action spell2 = new Action(4, ActionType.CAST, 0, -1, 1, 0, 0, 0, 0, true, false);
        Action spell3 = new Action(5, ActionType.CAST, 0, 0, -1, 1, 0, 0, 0, true, false);
        List<Action> spells = Arrays.asList(spell0, spell1, spell2, spell3);
        assertTrue(Main.shortestBrewPath(inventory, brew, spells));
    }

    @Test
    public void testShortestPathRefresh() {
        Inventory inventory = new Inventory(2, 0, 1, 0, 0);
        Action brew = new Action(1, ActionType.BREW, -3, 0, -2, 0, 9, 0, 0, true, false);
        Action spell0 = new Action(2, ActionType.CAST, 2, 0, 0, 0, 0, 0, 0, true, false);
        Action spell1 = new Action(3, ActionType.CAST, -1, 1, 0, 0, 0, 0, 0, false, false);
        Action spell2 = new Action(4, ActionType.CAST, 0, -1, 1, 0, 0, 0, 0, false, false);
        Action spell3 = new Action(5, ActionType.CAST, 0, 0, -1, 1, 0, 0, 0, true, false);
        List<Action> spells = Arrays.asList(spell0, spell1, spell2, spell3);
        assertTrue(Main.shortestBrewPath(inventory, brew, spells));
        brew.path.forEach(System.out::println);
    }

    @Test
    public void testBrewShortestPath() {
        Inventory inventory = new Inventory(3, 0, 2, 0, 0);
        Action brew = new Action(1, ActionType.BREW, -3, 0, -2, 0, 9, 0, 0, true, false);
        Action spell0 = new Action(2, ActionType.CAST, 2, 0, 0, 0, 0, 0, 0, true, false);
        Action spell1 = new Action(3, ActionType.CAST, -1, 1, 0, 0, 0, 0, 0, false, false);
        Action spell2 = new Action(4, ActionType.CAST, 0, -1, 1, 0, 0, 0, 0, false, false);
        Action spell3 = new Action(5, ActionType.CAST, 0, 0, -1, 1, 0, 0, 0, true, false);
        List<Action> spells = Arrays.asList(spell0, spell1, spell2, spell3);
        assertTrue(Main.shortestBrewPath(inventory, brew, spells));
        brew.path.forEach(System.out::println);
    }

    @Test
    public void testShortestPathToBrew() {
        Inventory inventory = new Inventory(2, 0, 1, 0, 0);
        Action brew = new Action(1, ActionType.BREW, -3, 0, -2, 0, 9, 0, 0, true, false);
        Action spell0 = new Action(2, ActionType.CAST, 2, 0, 0, 0, 0, 0, 0, true, false);
        Action spell1 = new Action(3, ActionType.CAST, -1, 1, 0, 0, 0, 0, 0, false, false);
        Action spell2 = new Action(4, ActionType.CAST, 0, -1, 1, 0, 0, 0, 0, false, false);
        Action spell3 = new Action(5, ActionType.CAST, 0, 0, -1, 1, 0, 0, 0, true, false);
        List<Action> spells = Arrays.asList(spell0, spell1, spell2, spell3);

        List<Node> path = Main.shortestPathToBrew(inventory, brew, spells);
        path.forEach(System.out::println);
        assertEquals(6, path.size());
    }

    @Test
    public void testShortestPathWithRefreshPass() {
        Inventory inventory = new Inventory(2, 0, 0, 0, 0);
        Action brew = new Action(1, ActionType.BREW, -2, -1, -1, 0, 20, 0, 0, true, false);


        Action spell0 = new Action(2, ActionType.CAST, 2, 0, 0, 0, 0, 0, 0, true, false);
        Action spell1 = new Action(3, ActionType.CAST, -1, 1, 0, 0, 0, 0, 0, true, false);
        Action spell2 = new Action(4, ActionType.CAST, 0, -1, 1, 0, 0, 0, 0, true, false);
        Action spell3 = new Action(5, ActionType.CAST, 0, 0, -1, 1, 0, 0, 0, true, false);
        List<Action> spells = Arrays.asList(spell0, spell1, spell2, spell3);
        assertTrue(Main.shortestBrewPath(inventory, brew, spells));
    }
}