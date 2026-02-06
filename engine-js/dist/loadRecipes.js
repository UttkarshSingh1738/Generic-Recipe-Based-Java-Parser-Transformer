import { readFileSync } from "fs";
import { resolve } from "path";
/**
 * Load recipe names from config (JSON array of strings).
 */
export function loadConfig(configPath) {
    const raw = readFileSync(configPath, "utf-8");
    return JSON.parse(raw);
}
/**
 * Load a single recipe file (same shape as Java: { recipes: [ ... ] }).
 */
export function loadRecipeFile(filePath) {
    const raw = readFileSync(filePath, "utf-8");
    const container = JSON.parse(raw);
    return container.recipes ?? [];
}
/**
 * Resolve recipe names from config to recipe objects from resourcesDir.
 */
export function loadRecipesFromConfig(configPath, resourcesDir) {
    const names = loadConfig(configPath);
    const all = [];
    for (const name of names) {
        const file = resolve(resourcesDir, `${name}.json`);
        try {
            const recipes = loadRecipeFile(file);
            all.push(...recipes);
        }
        catch (e) {
            console.error(`[ERROR] Failed to load recipe file: ${file}`, e);
            throw e;
        }
    }
    return all;
}
