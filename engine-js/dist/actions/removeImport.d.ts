import type { ActionFn } from "./types.js";
/**
 * Remove a named import from an ImportDeclaration.
 * Params: name (string) - the local binding name to remove (e.g. "Component").
 * If that was the only specifier, the whole ImportDeclaration is removed.
 */
export declare const removeImport: ActionFn;
