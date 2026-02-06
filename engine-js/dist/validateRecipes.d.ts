import type { Recipe, ActionSpec } from "./types.js";
/**
 * Validate that every action in every step uses a known action name.
 * Throws with a clear message if any action key is unknown.
 */
export declare function validateRecipes(recipes: Recipe[]): void;
/**
 * Parse a single action spec (single-key object) into [actionName, params].
 */
export declare function parseActionSpec(spec: ActionSpec): [string, Record<string, unknown>];
