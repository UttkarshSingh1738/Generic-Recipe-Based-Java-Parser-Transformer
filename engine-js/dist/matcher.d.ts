import { type NodePath } from "@babel/traverse";
import type { Node } from "@babel/types";
import type { Match } from "./types.js";
/**
 * Collect all paths whose node.type equals nodeType.
 */
export declare function findCandidates(ast: Node, nodeType: string): NodePath<Node>[];
/**
 * Whether the path matches the match criteria.
 * Supports nodeType, namePattern, decoratorName (ClassDeclaration), methodName (ClassMethod/ClassProperty).
 */
export declare function matches(path: NodePath<Node>, m: Match): boolean;
