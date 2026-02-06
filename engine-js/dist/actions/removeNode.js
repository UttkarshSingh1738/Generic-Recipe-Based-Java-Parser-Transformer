/**
 * Remove the matched node from the AST. Uses path.remove().
 */
export const removeNode = (path, _params, _ctx) => {
    path.remove();
};
