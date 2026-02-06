import { readFileSync } from "fs";
import { resolve } from "path";
import type { Recipe, RecipeContainer } from "./types.js";

/**
 * Load recipe names from config (JSON array of strings).
 */
export function loadConfig(configPath: string): string[] {
  const raw = readFileSync(configPath, "utf-8");
  return JSON.parse(raw) as string[];
}

/**
 * Load a single recipe file (same shape as Java: { recipes: [ ... ] }).
 */
export function loadRecipeFile(filePath: string): Recipe[] {
  const raw = readFileSync(filePath, "utf-8");
  const container = JSON.parse(raw) as RecipeContainer;
  return container.recipes ?? [];
}

/**
 * Resolve recipe names from config to recipe objects from resourcesDir.
 */
export function loadRecipesFromConfig(
  configPath: string,
  resourcesDir: string
): Recipe[] {
  const names = loadConfig(configPath);
  const all: Recipe[] = [];
  for (const name of names) {
    const file = resolve(resourcesDir, `${name}.json`);
    try {
      const recipes = loadRecipeFile(file);
      all.push(...recipes);
    } catch (e) {
      console.error(`[ERROR] Failed to load recipe file: ${file}`, e);
      throw e;
    }
  }
  return all;
}
