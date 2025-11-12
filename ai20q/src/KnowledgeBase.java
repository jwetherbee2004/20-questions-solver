import com.google.gson.*;
import java.io.*;
import java.util.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class KnowledgeBase {
    private Map<String, Map<String, Boolean>> data;

    public KnowledgeBase(String resourceName) throws IOException {
        Gson gson = new Gson();
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            throw new FileNotFoundException(resourceName + " not found on classpath");
        }
        Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        this.data = gson.fromJson(reader, new com.google.gson.reflect.TypeToken<Map<String, Map<String, Boolean>>>(){}.getType());
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
