import { addComment } from "./addComment.js";
import { removeNode } from "./removeNode.js";
import { removeDecorator } from "./removeDecorator.js";
import { removeImport } from "./removeImport.js";
import { replaceWithTemplate } from "./replaceWithTemplate.js";
const KNOWN_ACTIONS = {
    addComment,
    removeNode,
    removeDecorator,
    removeImport,
    replaceWithTemplate,
};
export function getKnownActionNames() {
    return new Set(Object.keys(KNOWN_ACTIONS));
}
export function createAction(name) {
    const fn = KNOWN_ACTIONS[name];
    if (!fn) {
        throw new Error(`Unknown action: '${name}'. Each action must be a single-key object where the key is the action name, e.g. {"addComment": {"comment": "..."}}. See docs/actions-es.yml for valid action names.`);
    }
    return fn;
}
export { addComment, removeNode };
