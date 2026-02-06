function getDecoratorName(d) {
    const expr = d.expression;
    if (!expr || expr.type !== "CallExpression")
        return null;
    return expr.callee?.name ?? null;
}
/**
 * Remove a decorator by name from the node (e.g. @Component from a class).
 * Params: name (string) - the decorator name to remove (e.g. "Component").
 */
export const removeDecorator = (path, params, _ctx) => {
    const name = params["name"];
    if (typeof name !== "string" || !name.trim())
        return;
    const node = path.node;
    if (!node.decorators?.length)
        return;
    const toRemove = name.trim();
    node.decorators = node.decorators.filter((d) => getDecoratorName(d) !== toRemove);
};
