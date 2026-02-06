import { describe, it } from "node:test";
import assert from "node:assert";
import { mkdtempSync, writeFileSync, readFileSync, rmSync } from "node:fs";
import { join } from "node:path";
import { tmpdir } from "node:os";
import { run } from "../pipeline.js";
describe("Pipeline integration", () => {
    it("addComment recipe produces output with comment", () => {
        const inputDir = mkdtempSync(join(tmpdir(), "recipe-in-"));
        const outputDir = mkdtempSync(join(tmpdir(), "recipe-out-"));
        try {
            const inputFile = join(inputDir, "Sample.ts");
            writeFileSync(inputFile, "class Sample { }\n", "utf-8");
            const recipes = [
                {
                    name: "AddComment",
                    steps: [
                        {
                            match: { nodeType: "ClassDeclaration", namePattern: "Sample" },
                            actions: [{ addComment: { comment: "Integration test comment" } }],
                        },
                    ],
                },
            ];
            run(inputDir, outputDir, recipes);
            const outputFile = join(outputDir, "Sample.ts");
            const output = readFileSync(outputFile, "utf-8");
            assert(output.includes("Integration test comment"));
            assert(output.includes("class Sample"));
        }
        finally {
            rmSync(inputDir, { recursive: true, force: true });
            rmSync(outputDir, { recursive: true, force: true });
        }
    });
});
