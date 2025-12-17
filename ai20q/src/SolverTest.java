import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.*;

public class SolverTest {

    private Solver Solver() {
        // Minimal knowledge base
        Map<String, Map<String, Boolean>> data = new HashMap<>();

        data.put("dog", Map.of(
                "hasFur", true,
                "isDomestic", true,
                "isPredator", true
        ));

        data.put("cow", Map.of(
                "hasFur", true,
                "isDomestic", true,
                "isPredator", false
        ));

        data.put("shark", Map.of(
                "hasFur", false,
                "isDomestic", false,
                "isPredator", true
        ));

        return new Solver(data);
    }

    // ---------------------------------------------
    // Initialization
    // ---------------------------------------------

    @Test
    void testInitialProbabilitiesSumToOne() {
        Solver solver = Solver();

        double sum = 0.0;
        for (String animal : List.of("dog", "cow", "shark")) {
            sum += getProbability(solver, animal);
        }

        assertEquals(1.0, sum, 1e-6);
    }

    // ---------------------------------------------
    // Bayesian update
    // ---------------------------------------------

    @Test
    void testApplyAnswerUpdatesProbabilities() {
        Solver solver = Solver();

        solver.applyAnswer("hasFur", Answer.YES);

        double dog = getProbability(solver, "dog");
        double cow = getProbability(solver, "cow");
        double shark = getProbability(solver, "shark");

        assertTrue(dog > shark);
        assertTrue(cow > shark);
    }

    // ---------------------------------------------
    // Question vs Guess behavior
    // ---------------------------------------------

    @Test
    void testSolverAsksAttributeInitially() {
        Solver solver = Solver();

        String step = solver.makeGuess();
        assertTrue(step.startsWith("ATTR:"));
    }

    @Test
    void testHighConfidenceGuessTriggersAnimalGuess() {
        Solver solver = Solver();

        // Force strong evidence toward cow
        solver.applyAnswer("hasFur", Answer.YES);
        solver.applyAnswer("isDomestic", Answer.YES);
        solver.applyAnswer("isPredator", Answer.NO);

        String step = solver.makeGuess();
        assertTrue(step.startsWith("ANIMAL:"));
    }

    // ---------------------------------------------
    // 20-question limit
    // ---------------------------------------------

    @Test
    void testQuestionLimitStopsAskingAttributes() {
        Solver solver = Solver();

        for (int i = 0; i < 20; i++) {
            solver.makeGuess();
        }

        assertTrue(solver.getQuestionIndex() >= 20);
    }

    // ---------------------------------------------
    // Helper: reflection-free probability access
    // ---------------------------------------------

    private double getProbability(Solver solver, String animal) {
        try {
            var field = Solver.class.getDeclaredField("probabilities");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Double> probs = (Map<String, Double>) field.get(solver);
            return probs.get(animal);
        } catch (Exception e) {
            fail("Unable to access probabilities map");
            return 0.0;
        }
    }
}
