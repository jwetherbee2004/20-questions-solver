import java.util.*;
import org.apache.commons.math3.util.FastMath;

enum Answer {
    YES, NO, MAYBE
}

public class Solver {
    private Map<String, Map<String, Boolean>> allAnimals;
    private List<String> remaining;
    private Set<String> askedAttributes = new HashSet<>();
    private int guessIndex = 0;

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
            double bestGain = Double.NEGATIVE_INFINITY;

            System.out.println("\n--- Information Gain Evaluation ---");

            double hBefore = log2(remaining.size());

            for (String attr : candidateAttrs) {

                List<String> yesGroup = new ArrayList<>();
                List<String> noGroup = new ArrayList<>();
                List<String> maybeGroup = new ArrayList<>();

                for (String animal : remaining) {
                    Boolean value = allAnimals.get(animal).get(attr);
                    if (value == null) {
                        maybeGroup.add(animal);
                    } else if (value) {
                        yesGroup.add(animal);
                    } else {
                        noGroup.add(animal);
                    }
                }

                int yesCnt = yesGroup.size();
                int noCnt = noGroup.size();
                
                int total = remaining.size();

                int maybeCnt = maybeGroup.size();

                double hYes   = yesCnt   == 0 ? 0 : log2(yesCnt);
                double hNo    = noCnt    == 0 ? 0 : log2(noCnt);
                double hMaybe = maybeCnt == 0 ? 0 : log2(maybeCnt);

                double pYes   = yesCnt   / (double) total;
                double pNo    = noCnt    / (double) total;
                double pMaybe = maybeCnt / (double) total;

                double expectedAfter =
                        pYes   * hYes +
                        pNo    * hNo +
                        pMaybe * hMaybe;

                double infoGain = hBefore - expectedAfter;                 

                System.out.println(
                    "Attribute: " + attr +
                    " | yes=" + yesCnt + ", no=" + noCnt +
                    " | IG=" + infoGain
                );
                
                if (infoGain > bestGain) {
                    bestGain = infoGain;
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

    public boolean applyAnswer(String attribute, Answer answer) {
        askedAttributes.add(attribute);
        int before = remaining.size();
        remaining.removeIf(animal -> {
            Boolean value = allAnimals.get(animal).get(attribute);
            if (answer == Answer.MAYBE) return false;
            if (answer == Answer.YES) return value == null || !value;
            if (answer == Answer.NO) return value == null || value;

            return false;
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
}
