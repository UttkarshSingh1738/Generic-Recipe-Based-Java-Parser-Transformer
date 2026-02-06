import type { Recipe, ActionSpec } from "./types.js";
import { getKnownActionNames } from "./actions/index.js";

/**
 * Validate that every action in every step uses a known action name.
 * Throws with a clear message if any action key is unknown.
 */
export function validateRecipes(recipes: Recipe[]): void {
  const known = getKnownActionNames();
  for (const recipe of recipes) {
    if (!recipe.steps) continue;
    let stepIndex = 0;
    for (const step of recipe.steps) {
      if (!step.actions) {
        stepIndex++;
        continue;
      }
      let actionIndex = 0;
      for (const actionSpec of step.actions) {
        const keys = Object.keys(actionSpec);
        if (keys.length === 0) {
          throw new Error(
            `Recipe '${recipe.name}' step ${stepIndex} action ${actionIndex}: action spec must be a single-key object (e.g. {"addComment": {"comment": "..."}}). Got empty object.`
          );
        }
        const actionName = keys[0]!;
        if (!known.has(actionName)) {
          throw new Error(
            `Recipe '${recipe.name}' step ${stepIndex} action ${actionIndex}: unknown action '${actionName}'. Each action must be a single-key object where the key is the action name. See docs/actions-es.yml for valid action names.`
          );
        }
        actionIndex++;
      }
      stepIndex++;
    }
  }
}

/**
 * Parse a single action spec (single-key object) into [actionName, params].
 */
export function parseActionSpec(spec: ActionSpec): [string, Record<string, unknown>] {
  const keys = Object.keys(spec);
  if (keys.length !== 1) {
    throw new Error(
      `Action spec must have exactly one key (the action name). Got: ${keys.join(", ")}`
    );
  }
  const name = keys[0]!;
  const params = (spec[name] as Record<string, unknown>) ?? {};
  return [name, params];
}
