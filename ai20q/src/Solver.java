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
    private Map<String, Double> probabilities = new HashMap<>();


    public Solver(Map<String, Map<String, Boolean>> data) {
        this.allAnimals = data;
        this.currentCandidates = new ArrayList<>(data.keySet());
        for (String animal : data.keySet()) {
            probabilities.put(animal, 1.0 / data.size());
        }
    }

    public String makeGuess() {
        // 1. Calculate scores for ALL animals and sort them
        List<String> sortedAnimals = new ArrayList<>(allAnimals.keySet());
        sortedAnimals.sort((a, b) -> Double.compare(calculateScore(b), calculateScore(a)));
        for (String animal : sortedAnimals) {
            System.out.println("Animal: " + animal + ", Score: " + calculateScore(animal));
        }

        // 2. Filter to get the "current candidates"
        // We take the top animals (e.g., top 15) or those with scores close to the max
        this.currentCandidates = new ArrayList<>(); // Clear old list
        
        List<String> sortedProbabilities = new ArrayList<>(probabilities.keySet());
        sortedProbabilities.sort((a, b) -> Double.compare(calculateScore(b), calculateScore(a)));

        double bestProb = calculateScore(sortedProbabilities.get(0));

        for (String animal : sortedProbabilities) {
            if(this.currentCandidates.size() < 5 || 
                probabilities.get(animal) >= bestProb * 0.25) {
                this.currentCandidates.add(animal);
            }
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
            double pYes = yesGroup.stream().mapToDouble(animal -> probabilities.get(animal)).sum();
            double pNo = noGroup.stream().mapToDouble(animal -> probabilities.get(animal)).sum();
            double pMaybe = maybeGroup.stream().mapToDouble(animal -> probabilities.get(animal)).sum();

            double pTotal = pYes + pNo + pMaybe;
            pYes /= pTotal;
            pNo /= pTotal;
            pMaybe /= pTotal;
            
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
        double total = 0.0;

        for(String animal : probabilities.keySet()) {
            Boolean value = allAnimals.get(animal).get(attribute);
            double likelihood = likelihood(answer, value);

            double posterior = probabilities.get(animal) * likelihood;
            probabilities.put(animal, posterior);

            total += posterior;
        }
        
        // Normalize probabilities
        for (String animal : probabilities.keySet()) {
            probabilities.put(animal, probabilities.get(animal) / total);
        }

        return true;
    }

    public boolean hasMoreConcreteGuesses() {
        return guessIndex < currentCandidates.size();
    }

    private double log2(double x) {
        return FastMath.log(x) / FastMath.log(2);
    }

    private double likelihood(Answer userAnswer, Boolean animalValue) {
        if (animalValue == null) {
            return 0.5;
        }

        switch (userAnswer) {
            case YES:
                return animalValue ? 0.85 : 0.1;
            case NO:
                return animalValue ? 0.1 : 0.85;
            case MAYBE:
                return 0.5;
            default:
                return 0.5;
        }
    }

    private double calculateScore(String animal) {
        return probabilities.get(animal);
    }
}
