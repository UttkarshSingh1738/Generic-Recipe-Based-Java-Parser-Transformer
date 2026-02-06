package gst.engine;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

/**
 * Integration test: run the pipeline on a minimal fixture (one Java file + one recipe)
 * and assert no exception and output contains the transformation.
 */
public class PipelineIntegrationTest {

    @Test
    public void pipeline_runWithAddCommentRecipe_producesOutputWithComment() throws Exception {
        Path inputDir = Files.createTempDirectory("recipe-input");
        Path outputDir = Files.createTempDirectory("recipe-output");
        Path recipeFile = Files.createTempFile("recipe", ".json");
        try {
            Path javaFile = inputDir.resolve("Sample.java");
            Files.writeString(javaFile,
                "public class Sample { }\n",
                StandardCharsets.UTF_8);

            String recipeJson = """
                {
                  "recipes": [{
                    "name": "AddComment",
                    "steps": [{
                      "match": { "nodeType": "ClassOrInterfaceDeclaration", "namePattern": "Sample" },
                      "actions": [{"addComment": {"comment": "Integration test comment"}}]
                    }]
                  }]
                }
                """;
            Files.writeString(recipeFile, recipeJson);

            Pipeline.run(recipeFile, inputDir, outputDir);

            Path outJava = outputDir.resolve("Sample.java");
            assertTrue("output file should exist", Files.isRegularFile(outJava));
            String output = Files.readString(outJava);
            assertTrue("output should contain the comment", output.contains("Integration test comment"));
        } finally {
            deleteRecursively(inputDir);
            deleteRecursively(outputDir);
            Files.deleteIfExists(recipeFile);
        }
    }

    private static void deleteRecursively(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (var s = Files.list(path)) {
                    s.forEach(PipelineIntegrationTest::deleteRecursively);
                }
            }
            Files.deleteIfExists(path);
        } catch (Exception ignored) { }
    }
}
