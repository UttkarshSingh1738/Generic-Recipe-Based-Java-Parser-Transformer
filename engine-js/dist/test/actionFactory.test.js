import { describe, it } from "node:test";
import assert from "node:assert";
import { getKnownActionNames, createAction } from "../actions/index.js";
describe("ActionFactory", () => {
    it("getKnownActionNames contains addComment, removeNode, removeDecorator, replaceWithTemplate", () => {
        const names = getKnownActionNames();
        assert(names.has("addComment"));
        assert(names.has("removeNode"));
        assert(names.has("removeDecorator"));
        assert(names.has("replaceWithTemplate"));
    });
    it("createAction('addComment') returns a function", () => {
        const fn = createAction("addComment");
        assert.strictEqual(typeof fn, "function");
    });
    it("createAction('unknown') throws with clear message", () => {
        assert.throws(() => createAction("notAnAction"), /Unknown action.*notAnAction.*single-key/);
    });
});
