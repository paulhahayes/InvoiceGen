package org.example;
import java.util.Random;
public class ABNGenerator {
    public String generateRandomABN() {
        Random random = new Random();
        StringBuilder abnString = new StringBuilder();

        for (int i = 0; i < 11; i++) {
            abnString.append(random.nextInt(10));
        }

        return abnString.toString();
    }
}
