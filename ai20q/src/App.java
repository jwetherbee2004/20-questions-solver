import java.util.Scanner;

public class App {
    private static Boolean solved = false;
    public static void main(String[] args) throws Exception {
        KnowledgeBase kb = new KnowledgeBase("animals.json");
        Solver solver = new Solver(kb.getData());
        Scanner sc = new Scanner(System.in);

        System.out.println("Think of an animal, and I will try to guess it.");

        while (!solved) {
            String step = solver.makeGuess();
            if (step == null) {
                System.out.println("I don't have any more questions or guesses. I give up.");
                break;
            }

            if (step.startsWith("ATTR:")) {
                String attr = step.substring("ATTR:".length());
                System.out.println(attr + "? (y/n)");
                String ans = sc.nextLine().trim().toLowerCase();
                if (ans.startsWith("y")) {
                    solver.applyAnswer(attr, true);
                } else if (ans.startsWith("n")) {
                    solver.applyAnswer(attr, false);
                } else {
                    System.out.println("Please answer 'y' or 'n'.");
                }
            } else if (step.startsWith("ANIMAL:")) {
                String animal = step.substring("ANIMAL:".length());
                System.out.println("Is it a " + animal + "? (y/n)");
                String ans = sc.nextLine().trim().toLowerCase();
                if (ans.startsWith("y")) {
                    System.out.println("Yay! I guessed it right.");
                    solved = true;
                } else if (ans.startsWith("n")) {
                    if (!solver.hasMoreConcreteGuesses()) {
                        System.out.println("I couldn't guess your animal from my knowledge base.");
                        break;
                    }
                } else {
                    System.out.println("Please answer 'y' or 'n'.");
                }
            } else {
                System.out.println("Unexpected solver response: " + step);
                break;
            }
        }

        sc.close();
    }
}
