package gst.engine;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import gst.api.Recipe;
import gst.api.Step;
import gst.api.Match;
import gst.api.ActionSpec;

public class RecipeValidatorTest {

    @Test
    public void validate_validRecipe_doesNotThrow() {
        Recipe r = new Recipe();
        r.name = "TestRecipe";
        Match m = new Match();
        m.nodeType = "ClassOrInterfaceDeclaration";
        Step step = new Step();
        step.match = m;
        step.actions = List.of(new ActionSpec("addComment", Map.of("comment", "x")));
        r.steps = List.of(step);
        RecipeValidator.validate(List.of(r));
    }

    @Test
    public void validate_unknownAction_throwsWithRecipeAndStepInfo() {
        Recipe r = new Recipe();
        r.name = "BadRecipe";
        Match m = new Match();
        m.nodeType = "ClassOrInterfaceDeclaration";
        Step step = new Step();
        step.match = m;
        step.actions = List.of(new ActionSpec("unknownActionName", Map.of()));
        r.steps = List.of(step);
        try {
            RecipeValidator.validate(List.of(r));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().contains("unknownActionName")) throw e;
            if (!e.getMessage().contains("BadRecipe")) throw e;
        }
    }
}
