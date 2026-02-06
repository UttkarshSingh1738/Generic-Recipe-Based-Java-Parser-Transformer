import traverseModule from "@babel/traverse";
const traverse = (typeof traverseModule === "function" ? traverseModule : traverseModule.default);
/**
 * Collect all paths whose node.type equals nodeType.
 */
export function findCandidates(ast, nodeType) {
    const paths = [];
    traverse(ast, {
        [nodeType](path) {
            paths.push(path);
        },
    });
    return paths;
}
/**
 * Whether the path matches the match criteria.
 * Supports nodeType, namePattern, decoratorName (ClassDeclaration), methodName (ClassMethod/ClassProperty).
 */
export function matches(path, m) {
    const node = path.node;
    if (node.type !== m.nodeType)
        return false;
    if (m.namePattern) {
        const name = getNodeName(node);
        if (name == null)
            return false;
        const re = new RegExp(m.namePattern);
        if (!re.test(name))
            return false;
    }
    if (m.decoratorName && node.type === "ClassDeclaration") {
        const decorators = node.decorators;
        if (!decorators?.length)
            return false;
        const hasDecorator = decorators.some((d) => {
            const expr = d.expression;
            if (!expr || expr.type !== "CallExpression")
                return false;
            const callee = expr.callee;
            return callee?.name === m.decoratorName;
        });
        if (!hasDecorator)
            return false;
    }
    if (m.methodName) {
        const name = getNodeName(node);
        if (name == null)
            return false;
        const re = new RegExp(m.methodName);
        if (!re.test(name))
            return false;
    }
    if (node.type === "ImportDeclaration") {
        const imp = node;
        if (m.sourcePattern) {
            const source = imp.source?.value ?? "";
            const re = new RegExp(m.sourcePattern);
            if (!re.test(source))
                return false;
        }
        if (m.importName) {
            const re = new RegExp(m.importName);
            const hasMatch = imp.specifiers?.some((s) => {
                const n = s.local?.name ?? s.imported?.name;
                return n != null && re.test(n);
            });
            if (!hasMatch)
                return false;
        }
    }
    return true;
}
function getNodeName(node) {
    const n = node;
    if (typeof n.name === "string")
        return n.name;
    if (n.id != null && typeof n.id === "object" && typeof n.id.name === "string") {
        return n.id.name;
    }
    if (n.key != null && typeof n.key === "object" && typeof n.key.name === "string") {
        return n.key.name;
    }
    return null;
}
