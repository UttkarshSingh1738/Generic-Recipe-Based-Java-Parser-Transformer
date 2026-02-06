import traverseModule, { type NodePath } from "@babel/traverse";
import type { Node } from "@babel/types";
import type { Match } from "./types.js";

const traverse = (typeof traverseModule === "function" ? traverseModule : (traverseModule as unknown as { default: (a: Node, v: object) => void }).default) as unknown as (ast: Node, visitors: object) => void;

/**
 * Collect all paths whose node.type equals nodeType.
 */
export function findCandidates(
  ast: Node,
  nodeType: string
): NodePath<Node>[] {
  const paths: NodePath<Node>[] = [];
  traverse(ast, {
    [nodeType](path) {
      paths.push(path);
    },
  } as Record<string, (path: NodePath<Node>) => void>);
  return paths;
}

/**
 * Whether the path matches the match criteria.
 * Supports nodeType, namePattern, decoratorName (ClassDeclaration), methodName (ClassMethod/ClassProperty).
 */
export function matches(path: NodePath<Node>, m: Match): boolean {
  const node = path.node as Node & { type: string; name?: string; id?: { name?: string }; decorators?: Array<{ expression?: { type?: string; callee?: { name?: string } } }> };
  if ((node as { type?: string }).type !== m.nodeType) return false;

  if (m.namePattern) {
    const name = getNodeName(node);
    if (name == null) return false;
    const re = new RegExp(m.namePattern);
    if (!re.test(name)) return false;
  }

  if (m.decoratorName && node.type === "ClassDeclaration") {
    const decorators = (node as { decorators?: Array<{ expression?: { type?: string; callee?: { name?: string } } }> }).decorators;
    if (!decorators?.length) return false;
    const hasDecorator = decorators.some((d) => {
      const expr = d.expression;
      if (!expr || (expr as { type?: string }).type !== "CallExpression") return false;
      const callee = (expr as { callee?: { name?: string } }).callee;
      return callee?.name === m.decoratorName;
    });
    if (!hasDecorator) return false;
  }

  if (m.methodName) {
    const name = getNodeName(node);
    if (name == null) return false;
    const re = new RegExp(m.methodName);
    if (!re.test(name)) return false;
  }

  if (node.type === "ImportDeclaration") {
    const imp = node as { source?: { value?: string }; specifiers?: Array<{ local?: { name?: string }; imported?: { name?: string } }> };
    if (m.sourcePattern) {
      const source = imp.source?.value ?? "";
      const re = new RegExp(m.sourcePattern);
      if (!re.test(source)) return false;
    }
    if (m.importName) {
      const re = new RegExp(m.importName);
      const hasMatch = imp.specifiers?.some((s) => {
        const n = s.local?.name ?? s.imported?.name;
        return n != null && re.test(n);
      });
      if (!hasMatch) return false;
    }
  }

  return true;
}

function getNodeName(node: Node & { name?: string; id?: { name?: string }; key?: { name?: string } }): string | null {
  const n = node as unknown as Record<string, unknown>;
  if (typeof n.name === "string") return n.name;
  if (n.id != null && typeof n.id === "object" && typeof (n.id as { name?: string }).name === "string") {
    return (n.id as { name: string }).name;
  }
  if (n.key != null && typeof n.key === "object" && typeof (n.key as { name?: string }).name === "string") {
    return (n.key as { name: string }).name;
  }
  return null;
}
