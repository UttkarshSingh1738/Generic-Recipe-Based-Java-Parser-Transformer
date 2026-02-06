/**
 * Remove a named import from an ImportDeclaration.
 * Params: name (string) - the local binding name to remove (e.g. "Component").
 * If that was the only specifier, the whole ImportDeclaration is removed.
 */
export const removeImport = (path, params, _ctx) => {
    const node = path.node;
    if (node.type !== "ImportDeclaration" || !node.specifiers?.length)
        return;
    const name = params["name"];
    if (typeof name !== "string" || !name.trim())
        return;
    const toRemove = name.trim();
    const remaining = node.specifiers.filter((s) => s.local?.name !== toRemove);
    if (remaining.length === 0) {
        path.remove();
        return;
    }
    node.specifiers = remaining;
};
