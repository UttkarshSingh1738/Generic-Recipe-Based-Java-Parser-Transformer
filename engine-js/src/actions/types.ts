import type { NodePath } from "@babel/traverse";
import type { Node } from "@babel/types";
import type { RunContext } from "../context.js";

export type ActionFn = (
  path: NodePath<Node>,
  params: Record<string, unknown>,
  ctx: RunContext
) => void;
