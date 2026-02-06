/**
 * Recipe types - same JSON shape as the Java engine.
 */

export interface Match {
  nodeType: string;
  /** Regex for node name (class name, function name, method key, etc.). */
  namePattern?: string;
  /** Require class to have a decorator with this name (e.g. "Component"). */
  decoratorName?: string;
  /** Require method/property name to match (for ClassMethod, ClassProperty). */
  methodName?: string;
  /** Regex or string for import source (e.g. "@angular/core"). For ImportDeclaration. */
  sourcePattern?: string;
  /** Regex or string for imported binding name (e.g. "Component"). For ImportDeclaration. */
  importName?: string;
  [key: string]: unknown;
}

export interface ActionSpec {
  [actionName: string]: Record<string, unknown>;
}

export interface Step {
  match: Match;
  actions: ActionSpec[];
}

export interface Recipe {
  name: string;
  description?: string;
  steps: Step[];
  language?: string;
  [key: string]: unknown;
}

export interface RecipeContainer {
  recipes: Recipe[];
}
