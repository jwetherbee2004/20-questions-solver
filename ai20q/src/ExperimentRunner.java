import java.util.*;

public class ExperimentRunner {
    private static final int TRIALS = 1000;

    public static void main(String[] args) throws Exception {
        // Load JSON once using KnowledgeBase
        KnowledgeBase kb = new KnowledgeBase("animals.json");
        Map<String, Map<String, Boolean>> animals = kb.getData();
        List<String> animalList = new ArrayList<>(kb.getAnimals());
        Random random = new Random();

        int successes = 0;
        int failures = 0;
        int totalQuestions = 0;

        System.out.println("Running " + TRIALS + " simulated games...\n");

        for (int trial = 0; trial < TRIALS; trial++) {

            String target = animalList.get(random.nextInt(animalList.size()));
            Solver solver = new Solver(animals);

            int qCount = 0;

            while (true) {

                String result = solver.makeGuess();

                if (result == null) {
                    failures++;
                    break;
                }

                if (result.startsWith("ATTR:")) {
                    String attr = result.substring(5);

                    Boolean truth = kb.getAttributes(target).get(attr);
                    Answer answer;

                    if (truth == null) answer = Answer.MAYBE;
                    else answer = truth ? Answer.YES : Answer.NO;

                    solver.applyAnswer(attr, answer);
                    qCount++;
                }

                else if (result.startsWith("ANIMAL:")) {
                    String guessAnimal = result.substring(7);

                    if (guessAnimal.equals(target)) {
                        successes++;
                    } else {
                        failures++;
                    }

                    qCount++;
                    break;
                }

                // Safety: prevent infinite loops
                if (qCount >= 20) {
                    failures++;
                    break;
                }
            }

            totalQuestions += qCount;
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
        System.out.println("======================================");
    }
}
