import ai20q.KnowledgeBase;

public class App {
    public static void main(String[] args) throws Exception {
        KnowledgeBase kb = new KnowledgeBase("C:\\Users\\jarja\\Desktop\\School\\B351\\20 Questions Solver\\20-questions-solver\\animals.json");
        System.out.println("Animals loaded: " + kb.getAnimals().size());
        System.out.println("Sample attributes for dog:");
        System.out.println(kb.getAttributes("dog"));
    }
}
