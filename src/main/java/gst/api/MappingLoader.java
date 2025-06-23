package gst.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MappingLoader {
     // Loads the JSON at mappingsV3.json into a List<Recipe>.
    public static List<Recipe> load(Path jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        RecipeContainer container = mapper.readValue(jsonFile.toFile(),RecipeContainer.class);
        return container.recipes;
    }
}
