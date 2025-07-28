package gst.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;

public class ActionSpecDeserializer extends JsonDeserializer<ActionSpec> {
    @Override
    public ActionSpec deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec oc = p.getCodec();
        TreeNode tree = oc.readTree(p);

        if (!(tree instanceof ObjectNode)) {
            throw new JsonMappingException(p, "Expected ActionSpec to be an object");
        }
        ObjectNode obj = (ObjectNode)tree;

        var fields = obj.fieldNames();
        if (!fields.hasNext()) {
            throw new JsonMappingException(p, "Empty action spec");
        }
        String actionKey = fields.next();
        JsonNode  paramsNode = obj.get(actionKey);

        Map<String,Object> params = Map.of();
        if (paramsNode != null && paramsNode.isObject()) {
            params = ((ObjectMapper) oc).convertValue(paramsNode, new TypeReference<Map<String,Object>>() {});
        }

        return new ActionSpec(actionKey, params);
    }
}
