import java.util.*;
import org.apache.commons.math3.util.FastMath;

enum Answer {
    YES, NO, MAYBE
}

public class Solver {
    private Map<String, Map<String, Boolean>> allAnimals;
    private Set<String> askedAttributes = new HashSet<>();
    private int guessIndex = 0;

    private List<String> currentCandidates = new ArrayList<>();
    private Map<String, Answer> userAnswers = new HashMap<>();

    public Solver(Map<String, Map<String, Boolean>> data) {
        this.allAnimals = data;
        this.currentCandidates = new ArrayList<>(data.keySet());
    }

    public String makeGuess() {
        // 1. Calculate scores for ALL animals and sort them
        List<String> sortedAnimals = new ArrayList<>(allAnimals.keySet());
        sortedAnimals.sort((a, b) -> Double.compare(calculateScore(b), calculateScore(a)));

        // 2. Filter to get the "current candidates"
        // We take the top animals (e.g., top 15) or those with scores close to the max
        this.currentCandidates = new ArrayList<>(); // Clear old list
        double bestScore = calculateScore(sortedAnimals.get(0));
        
        for (String animal : sortedAnimals) {
            double s = calculateScore(animal);
            if (this.currentCandidates.size() < 5 || (bestScore - s < 3.0)) {
                this.currentCandidates.add(animal);
            }
        }
        
        // If only 1 candidate remains, guess it
        if (this.currentCandidates.size() == 1) {
             return "ANIMAL:" + this.currentCandidates.get(0);
        }

        // 3. Information Gain Logic (Applied to 'candidates' instead of 'remaining')
        Set<String> attrs = new HashSet<>();
        for (String animal : this.currentCandidates) {
            Map<String, Boolean> mp = allAnimals.get(animal);
            if (mp != null) attrs.addAll(mp.keySet());
        }

        String bestAttr = null;
        double bestGain = Double.NEGATIVE_INFINITY;

        // Iterate over attributes to find the best split
        for (String attr : attrs) {
            if (askedAttributes.contains(attr)) continue;

            // --- Entropy Calculation on 'candidates' ---
            List<String> yesGroup = new ArrayList<>();
            List<String> noGroup = new ArrayList<>();
            List<String> maybeGroup = new ArrayList<>(); // Animals with null/unknown for this attr

            for (String animal : this.currentCandidates) {
                Boolean value = allAnimals.get(animal).get(attr);
                if (value == null) maybeGroup.add(animal);
                else if (value) yesGroup.add(animal);
                else noGroup.add(animal);
            }

            // If a question doesn't split the group at all, skip it
            if (yesGroup.isEmpty() && noGroup.isEmpty()) continue;
            
            // Calculate IG (standard logic)
            int total = this.currentCandidates.size();
            double hBefore = log2(total);
            double pYes = yesGroup.size() / (double) total;
            double pNo = noGroup.size() / (double) total;
            double pMaybe = maybeGroup.size() / (double) total;
            
            double hYes = (yesGroup.size() == 0) ? 0 : log2(yesGroup.size());
            double hNo = (noGroup.size() == 0) ? 0 : log2(noGroup.size());
            double hMaybe = (maybeGroup.size() == 0) ? 0 : log2(maybeGroup.size());

            double infoGain = hBefore - (pYes * hYes + pNo * hNo + pMaybe * hMaybe);

            if (infoGain > bestGain) {
                bestGain = infoGain;
                bestAttr = attr;
            }
        }

        // 4. Decision: Ask Attribute or Guess Animal
        // If we found a good attribute, ask it
        if (bestAttr != null && bestGain > 0.001) { 
            // System.out.println("Debug: Best attr " + bestAttr + " gain=" + bestGain);
            return "ATTR:" + bestAttr;
        }

        // If no good attribute found (or IG is 0), start guessing from the top of the sorted list
        if (guessIndex < this.currentCandidates.size()) {
            return "ANIMAL:" + this.currentCandidates.get(guessIndex++);
        }

        return null;
    }

    public boolean applyAnswer(String attribute, Answer answer) {
        userAnswers.put(attribute, answer);
        askedAttributes.add(attribute);
        guessIndex = 0;
        return true;
    }

    public boolean hasMoreConcreteGuesses() {
        return guessIndex < currentCandidates.size();
    }

    private double log2(double x) {
        return FastMath.log(x) / FastMath.log(2);
    }

    private double calculateScore(String animal) {
        double score = 0.0;
        Map<String, Boolean> animalAttrs = allAnimals.get(animal);

        for (Map.Entry<String, Answer> entry : userAnswers.entrySet()) {
            String attr = entry.getKey();
            Answer userAns = entry.getValue();
            Boolean animalValue = animalAttrs.get(attr);

            // If animal data is missing this attribute, treat it as neutral (0 change)
            if (animalValue == null) continue;

            if (userAns == Answer.YES) {
                if (animalValue) score += 1.0;       // Match
                else score -= 5.0;                   // Strong penalty for contradiction
            } else if (userAns == Answer.NO) {
                if (!animalValue) score += 1.0;      // Match
                else score -= 5.0;                   // Strong penalty for contradiction
            } 
            // Answer.MAYBE results in 0.0 change (neutral)
        }
        return score;
    }
}
