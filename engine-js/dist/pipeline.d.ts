import type { Recipe } from "./types.js";
/**
 * Run the pipeline: parse each JS/TS file, for each recipe/step find matching nodes (no actions yet), generate output.
 * Non-JS/TS files are copied as-is.
 */
export declare function run(inputRoot: string, outputRoot: string, recipes: Recipe[]): void;
