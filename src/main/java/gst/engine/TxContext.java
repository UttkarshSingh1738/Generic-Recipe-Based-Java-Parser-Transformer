package gst.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

public final class TxContext {
    // track variable name → new type
    private final Map<String, String> varTypeChanges = new HashMap<>();
    // future maps: methodRenames, removedNodes, etc.
    private final Set<String> removedVariables = new HashSet<>();

    public void registerVarType(String varName, String newType) {
        varTypeChanges.put(varName, newType);
    }
    public Optional<String> getVarType(String varName) {
        return Optional.ofNullable(varTypeChanges.get(varName));
    }
    public boolean hasVarChanged(String varName) {
        return varTypeChanges.containsKey(varName);
    }

    public void registerRemovedVariable(String varName) {
        removedVariables.add(varName);
    }
    public boolean isRemovedVariable(String varName) {
        return removedVariables.contains(varName);
    }
}
