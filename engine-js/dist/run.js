#!/usr/bin/env node
/**
 * CLI for the JS/TS recipe transformer.
 * Usage: node dist/run.js [--config PATH] [--resources DIR] <inputDir> <outputDir>
 * Or:    node dist/run.js <inputDir> <outputDir>  (uses config from ../config-js.json and ../resources)
 */
import { resolve } from "path";
import { existsSync } from "fs";
import { loadRecipesFromConfig } from "./loadRecipes.js";
import { validateRecipes } from "./validateRecipes.js";
import { run } from "./pipeline.js";
const args = process.argv.slice(2);
let configPath = resolve(process.cwd(), "config-js.json");
let resourcesDir = resolve(process.cwd(), "resources");
let inputDir = null;
let outputDir = null;
for (let i = 0; i < args.length; i++) {
    if (args[i] === "--config" && args[i + 1]) {
        configPath = resolve(args[++i]);
    }
    else if (args[i] === "--resources" && args[i + 1]) {
        resourcesDir = resolve(args[++i]);
    }
    else if (!inputDir) {
        inputDir = resolve(args[i]);
    }
    else if (!outputDir) {
        outputDir = resolve(args[i]);
    }
}
if (!inputDir || !outputDir) {
    console.error("Usage: node run.js [--config PATH] [--resources DIR] <inputDir> <outputDir>");
    console.error("  --config    Path to config JSON (array of recipe names). Default: config-js.json");
    console.error("  --resources Path to resources dir (recipe JSON files). Default: resources");
    process.exit(1);
}
if (!existsSync(configPath)) {
    console.error("Config not found:", configPath);
    process.exit(2);
}
if (!existsSync(inputDir)) {
    console.error("Input directory not found:", inputDir);
    process.exit(3);
}
const recipes = loadRecipesFromConfig(configPath, resourcesDir);
validateRecipes(recipes);
console.log("[INFO] Loaded", recipes.length, "recipe(s)");
run(inputDir, outputDir, recipes);
console.log("[INFO] Done");
