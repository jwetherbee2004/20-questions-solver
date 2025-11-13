import com.google.gson.*;
import java.io.*;
import java.util.*;

public class KnowledgeBase {
    private Map<String, Map<String, Boolean>> data;

    public KnowledgeBase(String filePath) throws IOException {
        Gson gson = new Gson();
        Reader reader = new FileReader(filePath);
        this.data = gson.fromJson(reader,
            new com.google.gson.reflect.TypeToken<Map<String, Map<String, Boolean>>>(){}.getType());
        reader.close();
    }

    public Set<String> getAnimals() {
        return data.keySet();
    }

    public Map<String, Boolean> getAttributes(String animal) {
        return data.get(animal);
    }

    public Map<String, Map<String, Boolean>> getData() {
        return data;
    }
}
