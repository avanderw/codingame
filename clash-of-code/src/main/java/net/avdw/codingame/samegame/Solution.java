package net.avdw.codingame.samegame;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
public class Solution {

    public static void main(String args[]) {
        List<String> vowels = new ArrayList<>();
        vowels.add("A");
        vowels.add("E");
        vowels.add("I");
        vowels.add("O");
        vowels.add("U");
        Scanner in = new Scanner(System.in);
        String sentence = in.nextLine();
        List<Integer> outputList = new ArrayList<>();
        for (String word : sentence.split("\\s")) {
            int count = 0;
            for (int i = 0; i < word.length(); i++) {
                String c = String.valueOf(word.charAt(i));
                if (vowels.contains(c.toUpperCase())) {
                    count+=1;
                }
            }
            outputList.add(count);
        }

        System.out.println(outputList.stream().map(String::valueOf).collect(Collectors.joining(" ")));
    }
}