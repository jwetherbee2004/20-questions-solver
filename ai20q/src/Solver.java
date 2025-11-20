import java.util.*;
import org.apache.commons.math3.util.FastMath;

public class Solver {
    private Map<String, Map<String, Boolean>> allAnimals;
    private List<String> remaining;
    private Set<String> askedAttributes = new HashSet<>();
    private int guessIndex = 0;
    private Random rnd = new Random();

    public Solver(Map<String, Map<String, Boolean>> data) {
        this.allAnimals = data;
        this.remaining = new ArrayList<>(data.keySet());
    }

    /**
     * Drives the next step of solving.
     * Returns a string starting with:
     *  - "ATTR:" + attributeName  => ask the user "attributeName?"
     *  - "ANIMAL:" + animalName  => make a concrete guess "Is it a animalName?"
     * Returns null if there is nothing left to ask/guess.
     */
    public String makeGuess() {
        Set<String> attrs = new HashSet<>();
        for (String animal : remaining) {
            Map<String, Boolean> mp = allAnimals.get(animal);
            if (mp != null) attrs.addAll(mp.keySet());
        }

        List<String> candidateAttrs = new ArrayList<>();
        for (String a : attrs) {
            if (askedAttributes.contains(a)) continue;
            Set<Boolean> vals = new HashSet<>();
            for (String animal : remaining) {
                Map<String, Boolean> mp = allAnimals.get(animal);
                Boolean v = (mp == null) ? null : mp.get(a);
                if (v == null) {
                    vals.add(null);
                } else {
                    vals.add(v);
                }
                if (vals.size() > 1) break;
            }
            if (vals.contains(Boolean.TRUE) && vals.contains(Boolean.FALSE)) {
                candidateAttrs.add(a);
            }
        }

        if (!candidateAttrs.isEmpty()) {
            String bestAttr = null;
            double bestEntropy = Double.POSITIVE_INFINITY;

            System.out.println("\n--- Entropy Evaluation for Attributes ---");

            for (String attr : candidateAttrs) {
                int yesCnt = 0;
                int noCnt = 0;
                for (String animal : remaining) {
                    boolean value = allAnimals.get(animal).get(attr);
                    if (value) {
                        yesCnt++;
                    } else {
                        noCnt++;
                    }
                }

                double h = entropy(yesCnt, noCnt);

                System.out.println(
                    "Attribute: " + attr +
                    " | yes=" + yesCnt + ", no=" + noCnt +
                    " | entropy=" + h
                );
                
                if (h < bestEntropy) {
                    System.out.println("NEW BEST ATTRIBUTE: " + attr + " (lower entropy)");
                    bestEntropy = h;
                    bestAttr = attr;
                }
            }

            System.out.println("FINAL CHOSEN ATTRIBUTE: " + bestAttr);
            System.out.println("----------------------------------------\n");

            askedAttributes.add(bestAttr);
            return "ATTR:" + bestAttr;
        }

        if (remaining.isEmpty()) return null;
        if (guessIndex >= remaining.size()) return null;
        String animalGuess = remaining.get(guessIndex++);
        return "ANIMAL:" + animalGuess;
    }

    public boolean applyAnswer(String attribute, boolean answer) {
        askedAttributes.add(attribute);
        int before = remaining.size();
        remaining.removeIf(animal -> {
            Map<String, Boolean> attrs = allAnimals.get(animal);
            Boolean value = (attrs == null) ? null : attrs.get(attribute);
            return value == null || value != answer;
        });
        guessIndex = 0;
        return remaining.size() < before;
    }

    public boolean hasMoreConcreteGuesses() {
        return guessIndex < remaining.size();
    }

    private double log2(double x) {
        return FastMath.log(x) / FastMath.log(2);
    }

    private double entropy(int yesCnt, int noCnt) {
        int total = yesCnt + noCnt;

        double pYes = (double) yesCnt / total;
        double pNo = (double) noCnt / total;

        double h = 0.0;

        if (pYes > 0) h -= pYes * log2(pYes);
        if (pNo > 0) h -= pNo * log2(pNo);

        return h;
    }
}
