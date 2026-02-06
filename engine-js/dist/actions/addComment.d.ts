import type { ActionFn } from "./types.js";
/**
 * Add a line comment (// ...) to the node. Uses Babel's comment API.
 * placement: "leading" (default) = before the node; "trailing" = after the node; "firstMember" = on first class member (for ClassDeclaration, keeps "export class Name" on one line).
 */
export declare const addComment: ActionFn;
