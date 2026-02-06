import type { NodePath } from "@babel/traverse";
import type { Node } from "@babel/types";
import type { ActionFn } from "./types.js";

/**
 * Remove the matched node from the AST. Uses path.remove().
 */
export const removeNode: ActionFn = (path, _params, _ctx) => {
  path.remove();
};
