import java.util.*;

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
            String pick = candidateAttrs.get(rnd.nextInt(candidateAttrs.size()));
            askedAttributes.add(pick);
            return "ATTR:" + pick;
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
}
