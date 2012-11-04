/*
 * Copyright (c) 2010, Hamish Morgan.
 * All Rights Reserved.
 */
package uk.ac.susx.mlcl.erl.snlp;

import java.util.ArrayList;
import org.junit.Test;

/**
 *
 * @author hamish
 */
public class TestArrayListRemove {

    public void testArrayListRemove() {

        // First lets create an ArrayList contain some words that are awesome.

        ArrayList<String> coolWords = new ArrayList<String>();
        coolWords.add("Discombobulated");
        coolWords.add("Shananigans");
        coolWords.add("Fluffalufapotamus");
        coolWords.add("Lackadaisical");
        coolWords.add("Withershins");
        coolWords.add("Hippopotomonstrosesquippedaliophobia");
        coolWords.add("Somnambulant");
        coolWords.add("Hemidemisemiquaver");

        // Now we desided one of those words is quite so awesome

        String notCoolWord = "Withershins";

        // Search the list for the index of the not so awesome word

        int foundIndex = -1;
        for (int i = 0; i < coolWords.size(); i++) {
            String word = coolWords.get(i);

            if (word.equals(notCoolWord)) {
                foundIndex = i;
            }
        }

        // If we found the word we can remove it AND print it. If
        // not then just print an appologetic message.

        if (foundIndex == -1) {
            System.out.println("Oh dear we didn't find the word.");
        } else {

            String wordToRemove = coolWords.get(foundIndex);
            coolWords.remove(foundIndex);

            System.out.println("We found the the word, now we could return it.");
            System.out.println("But let's print it instead: " + wordToRemove);
        }

    }
}
