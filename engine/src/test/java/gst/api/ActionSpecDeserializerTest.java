package gst.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

/**
 * Tests that recipe JSON deserializes so that each action is a single-key object:
 * the key becomes the action name (getKey()), the value becomes params (getParams()).
 */
public class ActionSpecDeserializerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void deserializeStep_withAddComment_actionKeyIsAddComment() throws Exception {
        String json = "{\"match\":{\"nodeType\":\"ClassOrInterfaceDeclaration\"},\"actions\":[{\"addComment\":{\"comment\":\"TODO: migrate\"}}]}";
        Step step = MAPPER.readValue(json, Step.class);
        assertNotNull(step.actions);
        assertEquals(1, step.actions.size());
        ActionSpec spec = step.actions.get(0);
        assertEquals("addComment", spec.getKey());
        assertEquals("TODO: migrate", spec.getParams().get("comment"));
    }

    @Test
    public void deserializeRecipeContainer_actionsHaveCorrectKeys() throws Exception {
        String json = "{\"recipes\":[{\"name\":\"R\",\"steps\":[{\"match\":{\"nodeType\":\"MethodDeclaration\"},\"actions\":[{\"removeNode\":{}}]}]}]}";
        RecipeContainer container = MAPPER.readValue(json, RecipeContainer.class);
        assertNotNull(container.recipes);
        assertEquals(1, container.recipes.size());
        List<ActionSpec> actions = container.recipes.get(0).steps.get(0).actions;
        assertEquals(1, actions.size());
        assertEquals("removeNode", actions.get(0).getKey());
        assertEquals(Map.of(), actions.get(0).getParams());
    }
}
