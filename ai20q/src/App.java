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

            System.out.println("Question #" + solver.getQuestionIndex());

            if(solver.getQuestionIndex() >= 20) {
                String animal = step.substring("ANIMAL:".length());
                System.out.println("My last guess: Is it a " + animal + "? (y/n)");
                String ans = sc.nextLine().trim().toLowerCase();
                if (ans.startsWith("y")) {
                    System.out.println("Yay! I guessed it right.");
                    solved = true;
                } else if (ans.startsWith("n")) {
                    if (!solver.hasMoreConcreteGuesses()) {
                        System.out.println("I couldn't guess your animal in 20 questions.");
                        break;
                    }
                } else {
                    System.out.println("Please answer 'y' or 'n'.");
                }
            }

            if (step.startsWith("ATTR:")) {
                String attr = step.substring("ATTR:".length());
                System.out.println(attr + "? (y/n/m)");
                String ans = sc.nextLine().trim().toLowerCase();
                if (ans.equals("y")) {
                    solver.applyAnswer(attr, Answer.YES);
                } else if (ans.equals("n")) {
                    solver.applyAnswer(attr, Answer.NO);
                } else if (ans.equals("m") || ans.equals("s")) {
                    solver.applyAnswer(attr, Answer.MAYBE);
                } else {
                    System.out.println("Please answer 'y', 'n', or 'm' (maybe).");
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
