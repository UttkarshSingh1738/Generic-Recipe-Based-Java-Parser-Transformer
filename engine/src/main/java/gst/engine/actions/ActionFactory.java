package gst.engine.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import gst.engine.actions.spi.ActionProvider;

public class ActionFactory {

    private static final List<ActionProvider> CUSTOM_PROVIDERS = 
            ServiceLoader.load(ActionProvider.class, ActionFactory.class.getClassLoader())
                        .stream()
                        .map(ServiceLoader.Provider::get)
                        .collect(Collectors.toList());

    public static Action create(String name, Map<String, Object> params) {
        Map<String, String> stringParams = new HashMap<>();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                stringParams.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
            }
        }

        switch (name) {
            case "changeType":                  return new ChangeTypeAction(stringParams);
            case "replaceWithMethodCall":       return new ReplaceWithMethodCallAction(stringParams);
            case "wrapArgument":                return new WrapArgumentAction(params);
            case "switchToReturnExpression":    return new SwitchToReturnExpressionAction(stringParams);
            case "collapseLiteralConcat":       return new CollapseLiteralConcatAction(stringParams);
            case "forToForEach":                return new ForToForEachAction(stringParams);
            case "insertBefore":                return new InsertBeforeAction(stringParams);
            case "insertAfter":                 return new InsertAfterAction(stringParams);
            case "removeNode":                  return new RemoveNodeAction(stringParams);
            case "replaceWithTemplate":         return new ReplaceWithTemplateAction(stringParams);
            case "addImport":                   return new AddImportAction(stringParams);
            case "removeImport":                return new RemoveImportAction(stringParams);
            case "addAnnotation":               return new AddAnnotationAction(stringParams);
            case "removeComment":               return new RemoveCommentAction(stringParams);
            case "removeExceptionFromCatch":    return new RemoveExceptionFromCatchAction(stringParams);
            case "renameMethodCall":            return new RenameMethodCallAction(stringParams);
            case "removeModifier":              return new RemoveModifierAction(stringParams);
            case "clearInitializer":            return new ClearInitializerAction(stringParams);
        }

        for(ActionProvider p : CUSTOM_PROVIDERS) {
            if(p.getActionName().equalsIgnoreCase(name)) {
                return p.create(stringParams);
            }
        }

        throw new IllegalArgumentException("Unknown action: " + name);
    }
}
