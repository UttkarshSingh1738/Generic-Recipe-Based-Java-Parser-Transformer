package gst.engine.utils;

import java.util.Set;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.Type;

public class VariableNameGenerator {
    
    private static final Set<String> RESERVED_KEYWORDS = Set.of(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", 
        "class", "const", "continue", "default", "do", "double", "else", "enum", 
        "extends", "final", "finally", "float", "for", "goto", "if", "implements", 
        "import", "instanceof", "int", "interface", "long", "native", "new", "package", 
        "private", "protected", "public", "return", "short", "static", "strictfp", 
        "super", "switch", "synchronized", "this", "throw", "throws", "transient", 
        "try", "void", "volatile", "while", "var", "yield", "record", "sealed", 
        "permits", "when", "module", "exports", "opens", "provides", "requires", 
        "to", "transitive", "uses", "with"
    );
    
    /**
     * Generates a safe variable name for ForEach loops based on collection expression and element type
     */
    public static String generateForEachVariableName(Expression colExpr, String elemTypeName) {
        // Try to derive from collection name (remove 's' suffix)
        if (colExpr.isNameExpr()) {
            String colName = colExpr.asNameExpr().getNameAsString();
            if (colName.endsWith("s") && colName.length() > 1) {
                String candidate = colName.substring(0, colName.length() - 1);
                if (!isReservedKeyword(candidate)) {
                    return candidate;
                }
            }
        }
        
        // Try to derive from element type
        if (elemTypeName != null && !elemTypeName.equals("var")) {
            String typeBasedName = elemTypeName.toLowerCase();
            if (typeBasedName.contains(".")) {
                typeBasedName = typeBasedName.substring(typeBasedName.lastIndexOf(".") + 1);
            }
            if (typeBasedName.endsWith("[]")) {
                typeBasedName = typeBasedName.substring(0, typeBasedName.length() - 2) + "Element";
            }
            if (!isReservedKeyword(typeBasedName)) {
                return typeBasedName;
            }
        }
        
        return "item";
    }
    
    /**
     * Generates a safe variable name for instanceof pattern matching based on the tested type
     */
    public static String generatePatternVariableName(Type testedType) {
        String typeStr = testedType.asString();
        
        // Handle array types like "byte[]", "int[]", etc.
        if (typeStr.endsWith("[]")) {
            String baseType = typeStr.substring(0, typeStr.length() - 2);
            String candidate = baseType + "Array";
            if (isReservedKeyword(candidate.toLowerCase())) {
                return candidate + "Value";
            }
            return candidate;
        }
        
        String candidateName;
        
        // Handle simple class types
        if (testedType.isClassOrInterfaceType()) {
            String simple = testedType.asClassOrInterfaceType().getName().asString();
            candidateName = Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
        } else {
            // Handle primitive types and other cases
            String simple = typeStr.replace("[]", "Array");
            if (simple.length() == 0) return null;
            candidateName = Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
        }
        
        // Avoid reserved keywords
        if (isReservedKeyword(candidateName)) {
            return candidateName + "Value";
        }
        
        return candidateName;
    }
    
    public static boolean isReservedKeyword(String name) {
        return RESERVED_KEYWORDS.contains(name);
    }
}
