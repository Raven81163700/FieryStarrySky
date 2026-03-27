package com.aurora.character;

import java.util.Random;

public final class RandomNameGenerator {

    private static final String[] FIRST_NAMES = {
            "James", "Oliver", "Liam", "Noah", "Ethan", "Mason", "Logan", "Lucas",
            "Henry", "Leo", "Alexander", "Benjamin", "William", "Michael", "Daniel", "Samuel",
            "Elijah", "Jacob", "Sebastian", "Owen", "Isabella", "Emma", "Ava", "Sophia",
            "Mia", "Charlotte", "Amelia", "Harper", "Evelyn", "Abigail", "Emily", "Ella",
            "Grace", "Chloe", "Lily", "Zoe", "Nora", "Aria", "Layla", "Victoria",
            "Madison", "Aurora", "Scarlett", "Hazel", "Lucy", "Ruby", "Alice", "Claire"
    };

    private static final String[] LAST_NAMES = {
            "Smith", "Johnson", "Brown", "Taylor", "Anderson", "Thomas", "Moore", "Martin",
            "Jackson", "White", "Harris", "Clark", "Lewis", "Walker", "Hall", "Young",
            "King", "Wright", "Lopez", "Hill", "Scott", "Green", "Adams", "Baker",
            "Gonzalez", "Nelson", "Carter", "Mitchell", "Perez", "Roberts", "Turner", "Phillips",
            "Campbell", "Parker", "Evans", "Edwards", "Collins", "Stewart", "Sanchez", "Morris",
            "Rogers", "Reed", "Cook", "Morgan", "Bell", "Murphy", "Bailey", "Rivera",
            "Cooper", "Richardson"
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
