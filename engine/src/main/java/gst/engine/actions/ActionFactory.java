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
            case "addAnnotation":               return new AddAnnotationAction(params);
            case "removeComment":               return new RemoveCommentAction(stringParams);
            case "removeExceptionFromCatch":    return new RemoveExceptionFromCatchAction(stringParams);
            case "renameMethodCall":            return new RenameMethodCallAction(stringParams);
            case "removeModifier":              return new RemoveModifierAction(stringParams);
            case "clearInitializer":            return new ClearInitializerAction(stringParams);
            case "removeStatements":            return new RemoveStatementsAction(stringParams);
            case "removeAnnotation":            return new RemoveAnnotationAction(stringParams);
            case "updateAnnotationAttribute":   return new UpdateAnnotationAttributeAction(stringParams);
            case "addModifier":                 return new AddModifierAction(stringParams);
            case "setAccessLevel":              return new SetAccessLevelAction(stringParams);
            case "renameVariable":              return new RenameVariableAction(stringParams);
            case "renameClass":                 return new RenameClassAction(stringParams);
            case "wrapWithTryCatch":            return new WrapWithTryCatchAction(stringParams);
            case "migrateAnnotation":           return new MigrateAnnotationAction(params);
            case "replaceWithScope":            return new ReplaceWithScopeAction(stringParams);
            case "updateImplements":            return new UpdateImplementsAction(stringParams);
            case "renameMethod":                return new RenameMethodAction(stringParams);
            case "removeParameter":             return new RemoveParameterAction(stringParams);
            case "removeArgument":              return new RemoveArgumentAction(stringParams);
            case "changeMethodReturnType":      return new ChangeMethodReturnTypeAction(stringParams);
            case "instanceOfToPattern":         return new InstanceOfToPatternAction(stringParams);
        }

        for(var p : CUSTOM_PROVIDERS) {
            if(p.getActionName().equalsIgnoreCase(name)) {
                return p.create(stringParams);
            }
        }

        throw new IllegalArgumentException("Unknown action: " + name);
    }
}
