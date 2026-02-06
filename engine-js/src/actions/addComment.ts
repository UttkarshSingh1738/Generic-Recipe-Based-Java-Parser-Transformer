import type { NodePath } from "@babel/traverse";
import type { Node } from "@babel/types";
import type { ActionFn } from "./types.js";

/**
 * Add a line comment (// ...) to the node. Uses Babel's comment API.
 * placement: "leading" (default) = before the node; "trailing" = after the node; "firstMember" = on first class member (for ClassDeclaration, keeps "export class Name" on one line).
 */
export const addComment: ActionFn = (path, params, _ctx) => {
  const comment = params["comment"];
  if (typeof comment !== "string" || !comment.trim()) return;
  const placement = params["placement"] === "trailing" ? "trailing" : params["placement"] === "firstMember" ? "firstMember" : "leading";
  const node = path.node as Node & { leadingComments?: Array<{ type: string; value: string }>; trailingComments?: Array<{ type: string; value: string }>; body?: { body?: unknown[] } };
  if (placement === "firstMember" && node.type === "ClassDeclaration" && node.body?.body?.length) {
    const first = node.body.body[0] as Node & { leadingComments?: Array<{ type: string; value: string }> };
    if (!first.leadingComments) first.leadingComments = [];
    first.leadingComments.push({ type: "CommentLine", value: " " + comment.trim() });
    return;
  }
  const key = placement === "trailing" ? "trailingComments" : "leadingComments";
  if (!node[key]) node[key] = [];
  node[key].push({ type: "CommentLine", value: " " + comment.trim() });
};
