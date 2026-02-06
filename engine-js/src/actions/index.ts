import type { ActionFn } from "./types.js";
import { addComment } from "./addComment.js";
import { removeNode } from "./removeNode.js";
import { removeDecorator } from "./removeDecorator.js";
import { removeImport } from "./removeImport.js";
import { replaceWithTemplate } from "./replaceWithTemplate.js";

const KNOWN_ACTIONS: Record<string, ActionFn> = {
  addComment,
  removeNode,
  removeDecorator,
  removeImport,
  replaceWithTemplate,
};

export function getKnownActionNames(): Set<string> {
  return new Set(Object.keys(KNOWN_ACTIONS));
}

export function createAction(name: string): ActionFn {
  const fn = KNOWN_ACTIONS[name];
  if (!fn) {
    throw new Error(
      `Unknown action: '${name}'. Each action must be a single-key object where the key is the action name, e.g. {"addComment": {"comment": "..."}}. See docs/actions-es.yml for valid action names.`
    );
  }
  return fn;
}

export { addComment, removeNode };
export type { ActionFn } from "./types.js";
