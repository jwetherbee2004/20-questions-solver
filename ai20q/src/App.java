public class App {
    public static void main(String[] args) throws Exception {
        KnowledgeBase kb = new KnowledgeBase("animals.json");
        System.out.println("Animals loaded: " + kb.getAnimals().size());
        for (String animal : kb.getAnimals()) {
            System.out.println("Animal: " + animal + ", Attributes: " + kb.getAttributes(animal).size());
        }
    }
}
