import type { NodePath } from "@babel/traverse";
import type { Node } from "@babel/types";
import type { ActionFn } from "./types.js";

function getDecoratorName(d: { expression?: { type?: string; callee?: { name?: string } } }): string | null {
  const expr = d.expression;
  if (!expr || (expr as { type?: string }).type !== "CallExpression") return null;
  return (expr as { callee?: { name?: string } }).callee?.name ?? null;
}

/**
 * Remove a decorator by name from the node (e.g. @Component from a class).
 * Params: name (string) - the decorator name to remove (e.g. "Component").
 */
export const removeDecorator: ActionFn = (path, params, _ctx) => {
  const name = params["name"];
  if (typeof name !== "string" || !name.trim()) return;
  const node = path.node as Node & { decorators?: Array<{ expression?: { type?: string; callee?: { name?: string } } }> };
  if (!node.decorators?.length) return;
  const toRemove = name.trim();
  node.decorators = node.decorators.filter((d) => getDecoratorName(d) !== toRemove);
};
