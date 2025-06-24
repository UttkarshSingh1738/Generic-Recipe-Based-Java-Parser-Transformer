package gst.engine.actions;

import java.util.HashMap;
import java.util.Map;

public class ActionFactory {
    public static Action create(String name, Map<String, Object> params) {
        Map<String, String> stringParams = new HashMap<>();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                stringParams.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
            }
        }
        return switch (name) {
            case "changeType"               -> new ChangeTypeAction(stringParams);
            case "replaceWithMethodCall"    -> new ReplaceWithMethodCallAction(stringParams);
            case "wrapArgument"             -> new WrapArgumentAction(params);
            case "switchToReturnExpression" -> new SwitchToReturnExpressionAction(stringParams);
            case "collapseLiteralConcat"    -> new CollapseLiteralConcatAction(stringParams);
            case "forToForEach"             -> new ForToForEachAction(stringParams);
            case "insertBefore"             -> new InsertBeforeAction(stringParams);
            case "insertAfter"              -> new InsertAfterAction(stringParams);
            case "removeNode"               -> new RemoveNodeAction(stringParams);
            case "replaceWithTemplate"      -> new ReplaceWithTemplateAction(stringParams);
            case "addImport"                -> new AddImportAction(stringParams);
            case "removeImport"             -> new RemoveImportAction(stringParams);
            case "addAnnotation"            -> new AddAnnotationAction(stringParams);

            default -> throw new IllegalArgumentException("Unknown action: " + name);
        };
    }
}
