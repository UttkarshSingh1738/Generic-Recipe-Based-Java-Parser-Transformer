package gst.engine.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationFactory {
    
    private static final Map<String, ValidationRule> VALIDATORS = new HashMap<>();
    
    static {
        register(new SwitchExpressionCompletenessRule());
        register(new TypeCompatibilityRule());
        register(new OverrideRule());
        
        register(new PatternVariableUsageRule());
        register(new EnhancedForUsageRule());
    }
    
    private static void register(ValidationRule rule) {
        VALIDATORS.put(rule.getRuleName(), rule);
    }
    
    public static ValidationRule create(String ruleName) {
        ValidationRule validator = VALIDATORS.get(ruleName);
        if (validator == null) {
            throw new IllegalArgumentException("Unknown validation rule: " + ruleName);
        }
        return validator;
    }
    
    public static boolean exists(String ruleName) {
        return VALIDATORS.containsKey(ruleName);
    }
    
    public static List<String> getAvailableValidators() {
        return new ArrayList<>(VALIDATORS.keySet());
    }
}
