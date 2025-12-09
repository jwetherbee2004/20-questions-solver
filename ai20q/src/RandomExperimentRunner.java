import java.util.*;

public class RandomExperimentRunner {
    private static final int TRIALS = 10000;

    public static void main(String[] args) throws Exception {
        // Load JSON once using KnowledgeBase
        KnowledgeBase kb = new KnowledgeBase("animals.json");
        Map<String, Map<String, Boolean>> animals = kb.getData();
        List<String> animalList = new ArrayList<>(kb.getAnimals());
        Random random = new Random();

        int successes = 0;
        int failures = 0;
        Map<String, Integer> successCount = new HashMap<>();
        Map<String, Integer> failureCount = new HashMap<>();
        int totalQuestions = 0;

        System.out.println("Running " + TRIALS + " simulated games...\n");

        for (int trial = 0; trial < TRIALS; trial++) {

            String target = animalList.get(random.nextInt(animalList.size()));
            List<String> possibleAnimals = new ArrayList<>(animalList);
            int qCount = 0;

            //Randomly selects an animal to guess 
            String guessAnimal = null;
            while (qCount <= 20) {
                guessAnimal = possibleAnimals.get(random.nextInt(possibleAnimals.size()));
                qCount++;
                if(guessAnimal.equals(target)) {
                    break;
                } else {
                    possibleAnimals.remove(guessAnimal);
                }
            }
            totalQuestions += qCount;

            // System.out.println("Target animal is '" + target + "'.");
            if(guessAnimal.equals(target)) {
                successes++;
                successCount.put(target, successCount.getOrDefault(target, 0) + 1);
            } else {
                failures++;
                failureCount.put(target, failureCount.getOrDefault(target, 0) + 1);
            }
        }

        // Summary
        System.out.println("======================================");
        System.out.println("          EXPERIMENT RESULTS");
        System.out.println("======================================");
        System.out.println("Trials: " + TRIALS);
        System.out.println("Successes: " + successes);
        System.out.println("Failures: " + failures);
        System.out.println("Success Rate: " + String.format("%.2f", (successes * 100.0 / TRIALS)) + "%");
        System.out.println("Average Questions: " + String.format("%.2f", (totalQuestions / (double) TRIALS)));
        if (successes > 0) {
            System.out.println("\nSuccessfully guessed the following animals:");
            for (String animal : successCount.keySet()) {
                int count = successCount.get(animal);
                System.out.println("- " + animal + ": " + count + " time(s)");
            }
        }
        System.out.println("======================================");   
        if (failures > 0) {
            System.out.println("\nFailed to guess the following animals:");
            for (String animal : failureCount.keySet()) {
                int count = failureCount.get(animal);
                System.out.println("- " + animal + ": " + count + " time(s)");
            }
        }
        System.out.println("======================================");
        
        // Check animals that were both succeeded and failed
        printSuccessVsFailure(successCount, failureCount);
    }

    private static void printSuccessVsFailure(Map<String, Integer> successCount, Map<String, Integer> failureCount) {
        Set<String> bothSuccessAndFail = new HashSet<>(successCount.keySet());
        bothSuccessAndFail.retainAll(failureCount.keySet());

        if (bothSuccessAndFail.isEmpty()) {
            System.out.println("\nNo animals were both successfully and unsuccessfully guessed.");
            return;
        }

        System.out.println("\nAnimals guessed both successfully and unsuccessfully:");
        System.out.println("----------------------------------------");
        for (String animal : bothSuccessAndFail) {
            int successes = successCount.get(animal);
            int failures = failureCount.get(animal);
            int total = successes + failures;
            double successRate = (successes * 100.0 / total);
            System.out.println("- " + animal + ": " + successes + " success(es), " + failures + " failure(s) (" + 
                String.format("%.2f", successRate) + "% success rate)");
        }
        System.out.println("======================================");
    }
}
