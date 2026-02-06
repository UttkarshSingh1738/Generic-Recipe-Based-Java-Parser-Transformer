import template from "@babel/template";
/**
 * Replace the matched node with the AST produced by parsing the template string.
 * Params: template (string) - code that parses to a single expression or statement.
 */
export const replaceWithTemplate = (path, params, _ctx) => {
    const code = params["template"];
    if (typeof code !== "string" || !code.trim())
        return;
    const ast = template.ast(code.trim());
    const node = Array.isArray(ast) ? ast[0] : ast;
    if (node)
        path.replaceWith(node);
};
