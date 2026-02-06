import { describe, it } from "node:test";
import assert from "node:assert";
import { parse } from "@babel/parser";
import traverseModule from "@babel/traverse";
import generateModule from "@babel/generator";
import type { NodePath } from "@babel/traverse";
import type { Node } from "@babel/types";
import { addComment } from "../actions/addComment.js";

const traverse = (typeof traverseModule === "function" ? traverseModule : (traverseModule as unknown as { default: (a: Node, v: object) => void }).default) as unknown as (ast: Node, visitors: object) => void;
const generate = (typeof generateModule === "function" ? generateModule : (generateModule as unknown as { default: (a: Node) => { code: string } }).default) as unknown as (ast: Node) => { code: string };

describe("addComment action", () => {
  it("adds leading line comment to class declaration", () => {
    const code = "class Foo {}";
    const ast = parse(code, { sourceType: "module", plugins: ["typescript"] });
    let captured: NodePath<Node> | null = null;
    traverse(ast as Node, {
      ClassDeclaration(p: NodePath<Node>) {
        captured = p;
      },
    });
    assert(captured != null);
    addComment(captured!, { comment: "TODO: migrate" }, { filePath: "test.ts", recipeName: "R" });
    const out = generate(ast);
    assert(out.code.includes("// TODO: migrate"));
    assert(out.code.includes("class Foo"));
  });
});
