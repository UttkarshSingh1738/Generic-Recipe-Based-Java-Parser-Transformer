import template from "@babel/template";
import type { NodePath } from "@babel/traverse";
import type { Node } from "@babel/types";
import type { ActionFn } from "./types.js";

/**
 * Replace the matched node with the AST produced by parsing the template string.
 * Params: template (string) - code that parses to a single expression or statement.
 */
export const replaceWithTemplate: ActionFn = (path, params, _ctx) => {
  const code = params["template"];
  if (typeof code !== "string" || !code.trim()) return;
  const ast = template.ast(code.trim());
  const node = Array.isArray(ast) ? ast[0] : ast;
  if (node) path.replaceWith(node as Node);
};
