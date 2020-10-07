package net.avdw.codingame.samegame;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        String words = in.nextLine();

        Map<Character, Integer> count = new HashMap<>();
        for (int i = 0; i < words.length();i ++) {
            count.putIfAbsent(words.charAt(i), 0);
        }


        System.out.println("answer");
    }
}