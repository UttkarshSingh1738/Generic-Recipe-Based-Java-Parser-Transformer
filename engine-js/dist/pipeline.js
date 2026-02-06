import { readFileSync, writeFileSync, copyFileSync, mkdirSync, readdirSync, statSync } from "fs";
import { join, relative, dirname } from "path";
import { parse } from "@babel/parser";
import generateModule from "@babel/generator";
const generate = (typeof generateModule === "function" ? generateModule : generateModule.default);
import { findCandidates, matches } from "./matcher.js";
import { createAction } from "./actions/index.js";
import { parseActionSpec } from "./validateRecipes.js";
const JS_EXTENSIONS = new Set([".js", ".jsx", ".ts", ".tsx"]);
const PARSER_PLUGINS = ["typescript", "jsx", "decorators"];
function* walkFiles(dir, base = dir) {
    const entries = readdirSync(dir, { withFileTypes: true });
    for (const e of entries) {
        const full = join(dir, e.name);
        const rel = relative(base, full);
        if (e.isDirectory()) {
            yield* walkFiles(full, base);
        }
        else if (e.isFile()) {
            yield rel;
        }
    }
}
function parseFile(content, filename) {
    const ext = filename.endsWith(".tsx") || filename.endsWith(".ts")
        ? "ts"
        : "js";
    return parse(content, {
        sourceType: "module",
        plugins: [...PARSER_PLUGINS],
        sourceFilename: filename,
    });
}
/**
 * Run the pipeline: parse each JS/TS file, for each recipe/step find matching nodes (no actions yet), generate output.
 * Non-JS/TS files are copied as-is.
 */
export function run(inputRoot, outputRoot, recipes) {
    for (const rel of walkFiles(inputRoot)) {
        const inputPath = join(inputRoot, rel);
        const outputPath = join(outputRoot, rel);
        const stat = statSync(inputPath);
        if (!stat.isFile())
            continue;
        const ext = rel.includes(".") ? rel.slice(rel.lastIndexOf(".")) : "";
        if (!JS_EXTENSIONS.has(ext)) {
            mkdirSync(dirname(outputPath), { recursive: true });
            copyFileSync(inputPath, outputPath);
            console.log("[COPY]", rel);
            continue;
        }
        const content = readFileSync(inputPath, "utf-8");
        let ast;
        try {
            ast = parseFile(content, rel);
        }
        catch (err) {
            console.error("[ERROR] Failed to parse:", rel, err);
            mkdirSync(dirname(outputPath), { recursive: true });
            writeFileSync(outputPath, content);
            continue;
        }
        let changed = false;
        const ctx = { filePath: rel, recipeName: "" };
        for (const recipe of recipes) {
            ctx.recipeName = recipe.name;
            for (const step of recipe.steps) {
                const nodeType = step.match.nodeType;
                const candidates = findCandidates(ast, nodeType);
                for (const path of candidates) {
                    if (matches(path, step.match)) {
                        console.log("[MATCH]", nodeType, "at", rel, path.node.loc ? path.node.loc.start : "");
                        changed = true;
                        for (const actionSpec of step.actions ?? []) {
                            const [actionName, params] = parseActionSpec(actionSpec);
                            const action = createAction(actionName);
                            action(path, params, ctx);
                            console.log("[ACTION]", actionName, "on node at", path.node?.loc ? path.node.loc.start : "(removed)");
                        }
                    }
                }
            }
        }
        mkdirSync(dirname(outputPath), { recursive: true });
        const output = generate(ast, { retainLines: true }, content);
        writeFileSync(outputPath, output.code);
        if (changed) {
            console.log("[WRITE]", rel);
        }
    }
}
