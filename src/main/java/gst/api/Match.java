package gst.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Match {
    public String nodeType;
    public String fqn;
    public String type;
    public String methodName;
    public String fqnScope;
    public String annotation;
    public String matchExpr;
    public String operator;
    public String literalPattern;
    public String initVarPattern;      
    public String conditionPattern;
    public String updatePattern;
    public String accessPattern;
    
    public Boolean literalOnly;
    public Boolean requireInitializer;
    public Boolean requireNoTypeArgs;
    public String argumentType;
    public String expectedParamType;

    public List<String> typeAny;
    public String typePattern;

    public String parentNodeType;
    public String namePattern;   
    public String scopePattern;
    public String hasModifier;
    public String returnTypePattern;
    public Integer paramCount;  
    public String requiresImport;     
    public String forbidsImport;

    public Integer beforeLine;
    public Integer afterLine;
}
