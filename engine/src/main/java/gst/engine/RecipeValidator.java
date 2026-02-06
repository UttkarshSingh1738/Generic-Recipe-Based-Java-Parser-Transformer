package gst.engine;

import java.util.List;
import java.util.Set;

import gst.api.ActionSpec;
import gst.api.Recipe;
import gst.api.Step;
import gst.engine.actions.ActionFactory;

/**
 * Validates loaded recipes before the pipeline runs. Ensures every action
 * uses a known action name (single-key object where the key is the action name).
 * See docs/actions.yml for valid action names.
 */
public final class RecipeValidator {

    private RecipeValidator() {}

    /**
     * Validates that every action in every step of every recipe has a key
     * that is known to {@link ActionFactory}. Throws if any action key is unknown.
     *
     * @param recipes the loaded recipes (e.g. from MappingLoader.load)
     * @throws IllegalArgumentException if any action key is not a known action name
     */
    public static void validate(List<Recipe> recipes) {
        if (recipes == null) {
            return;
        }
        Set<String> knownNames = ActionFactory.getKnownActionNames();
        for (Recipe recipe : recipes) {
            if (recipe.steps == null) {
                continue;
            }
            int stepIndex = 0;
            for (Step step : recipe.steps) {
                if (step.actions == null) {
                    stepIndex++;
                    continue;
                }
                int actionIndex = 0;
                for (ActionSpec spec : step.actions) {
                    String key = spec.getKey();
                    if (key == null || key.isBlank()) {
                        throw new IllegalArgumentException(
                            "Recipe '" + recipe.name + "' step " + stepIndex + " action " + actionIndex
                                + ": action spec must be a single-key object (e.g. {\"addComment\": {\"comment\": \"...\"}}). Got empty key.");
                    }
                    if (!knownNames.contains(key)) {
                        throw new IllegalArgumentException(
                            "Recipe '" + recipe.name + "' step " + stepIndex + " action " + actionIndex
                                + ": unknown action '" + key + "'. Each action must be a single-key object where the key is the action name, e.g. {\"addComment\": {\"comment\": \"...\"}}. See docs/actions.yml for valid action names.");
                    }
                    actionIndex++;
                }
                stepIndex++;
            }
        }
    }
}
