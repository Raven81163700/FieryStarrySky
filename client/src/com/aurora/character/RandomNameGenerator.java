package com.aurora.character;

import java.util.Random;

public final class RandomNameGenerator {

    private static final String[] FIRST_NAMES = {
            "James", "Oliver", "Liam", "Noah", "Ethan", "Mason", "Logan", "Lucas",
            "Henry", "Leo", "Ava", "Emma", "Mia", "Sofia", "Chloe", "Luna"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Brown", "Taylor", "Anderson", "Thomas", "Moore", "Martin",
            "Jackson", "White", "Harris", "Clark", "Lewis", "Walker", "Hall", "Young"
    };

    private final Random random;

    public RandomNameGenerator() {
        random = new Random();
    }

    public String nextName() {
        String first = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String last = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        int n = random.nextInt(10000);
        String suffix = String.valueOf(10000 + n).substring(1);
        return first + last + "#" + suffix;
    }
}
