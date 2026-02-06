import { describe, it } from "node:test";
import assert from "node:assert";
import { parse } from "@babel/parser";
import traverseModule from "@babel/traverse";
import generateModule from "@babel/generator";
import { addComment } from "../actions/addComment.js";
const traverse = (typeof traverseModule === "function" ? traverseModule : traverseModule.default);
const generate = (typeof generateModule === "function" ? generateModule : generateModule.default);
describe("addComment action", () => {
    it("adds leading line comment to class declaration", () => {
        const code = "class Foo {}";
        const ast = parse(code, { sourceType: "module", plugins: ["typescript"] });
        let captured = null;
        traverse(ast, {
            ClassDeclaration(p) {
                captured = p;
            },
        });
        assert(captured != null);
        addComment(captured, { comment: "TODO: migrate" }, { filePath: "test.ts", recipeName: "R" });
        const out = generate(ast);
        assert(out.code.includes("// TODO: migrate"));
        assert(out.code.includes("class Foo"));
    });
});
