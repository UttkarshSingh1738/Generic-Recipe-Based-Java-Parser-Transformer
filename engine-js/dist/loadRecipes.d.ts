import type { Recipe } from "./types.js";
/**
 * Load recipe names from config (JSON array of strings).
 */
export declare function loadConfig(configPath: string): string[];
/**
 * Load a single recipe file (same shape as Java: { recipes: [ ... ] }).
 */
export declare function loadRecipeFile(filePath: string): Recipe[];
/**
 * Resolve recipe names from config to recipe objects from resourcesDir.
 */
export declare function loadRecipesFromConfig(configPath: string, resourcesDir: string): Recipe[];
