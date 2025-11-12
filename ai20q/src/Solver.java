import java.util.*;

public class Solver {
    private Map<String, Map<String, Boolean>> allAnimals;
    private List<String> remaining;
    private Set<String> askedAttributes = new HashSet<>();
    private int guessIndex = 0;
    private Random rnd = new Random();

    public Solver(Map<String, Map<String, Boolean>> data) {
        this.allAnimals = data;
        // store animal names so concrete guesses can be made
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
        // 1) look for an attribute that has not been asked and that actually splits remaining
        Set<String> attrs = new HashSet<>();
        for (String animal : remaining) {
            Map<String, Boolean> mp = allAnimals.get(animal);
            if (mp != null) attrs.addAll(mp.keySet());
        }

        List<String> candidateAttrs = new ArrayList<>();
        for (String a : attrs) {
            if (askedAttributes.contains(a)) continue;
            // collect distinct values for this attribute among remaining
            Set<Boolean> vals = new HashSet<>();
            for (String animal : remaining) {
                Map<String, Boolean> mp = allAnimals.get(animal);
                Boolean v = (mp == null) ? null : mp.get(a);
                // treat null as separate from true/false so attributes present on all animals are preferred
                if (v == null) {
                    // if any animal lacks the attribute, this attribute won't reliably split -> skip
                    vals.add(null);
                } else {
                    vals.add(v);
                }
                if (vals.size() > 1) break; // splits (true/false) -> good
            }
            // only consider attributes that split (i.e. both true and false among remaining)
            if (vals.contains(Boolean.TRUE) && vals.contains(Boolean.FALSE)) {
                candidateAttrs.add(a);
            }
        }

        if (!candidateAttrs.isEmpty()) {
            // pick a random splittable attribute
            String pick = candidateAttrs.get(rnd.nextInt(candidateAttrs.size()));
            return "ATTR:" + pick;
        }

        // 2) no more useful attributes -> make concrete guesses from remaining
        if (remaining.isEmpty()) return null;
        if (guessIndex >= remaining.size()) return null;
        String animalGuess = remaining.get(guessIndex++);
        return "ANIMAL:" + animalGuess;
    }

    // apply an answer (true/false) for an attribute and filter remaining animals
    public boolean applyAnswer(String attribute, boolean answer) {
        askedAttributes.add(attribute);
        int before = remaining.size();
        remaining.removeIf(animal -> {
            Map<String, Boolean> attrs = allAnimals.get(animal);
            Boolean value = (attrs == null) ? null : attrs.get(attribute);
            // if attribute missing or differs from the answer, remove
            return value == null || value != answer;
        });
        // reset guess index so concrete guesses start from beginning of filtered list
        guessIndex = 0;
        return remaining.size() < before;
    }

    // helper used by App when user answers concrete guess "no" to skip to next guess
    public boolean hasMoreConcreteGuesses() {
        return guessIndex < remaining.size();
    }
}
