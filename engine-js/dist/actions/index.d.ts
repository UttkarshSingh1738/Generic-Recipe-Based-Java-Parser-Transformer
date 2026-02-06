import type { ActionFn } from "./types.js";
import { addComment } from "./addComment.js";
import { removeNode } from "./removeNode.js";
export declare function getKnownActionNames(): Set<string>;
export declare function createAction(name: string): ActionFn;
export { addComment, removeNode };
export type { ActionFn } from "./types.js";
