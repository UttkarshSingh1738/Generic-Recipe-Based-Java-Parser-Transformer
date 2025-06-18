package gst;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import gst.api.MappingLoader;
import gst.api.Recipe;

public class Main {
    public static void main(String[] args) throws Exception {
        Path mappingFile = Paths.get("src", "main", "resources", "mappingsV3.json");
        List<Recipe> recipes = MappingLoader.load(mappingFile);
        System.out.println("Loaded " + recipes.size() + " recipes:");
        for (Recipe r : recipes) {
            System.out.println("  - " + r.name + ": " + r.description);
        }
    }
}
