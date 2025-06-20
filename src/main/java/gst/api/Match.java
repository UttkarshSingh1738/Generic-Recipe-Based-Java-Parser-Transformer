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

    // our new schema fields for semantic checks & require‐initializer
    public Boolean requireInitializer;
    public Boolean requireNoTypeArgs;
    public String argumentType;
    public String expectedParamType;

    // for future pattern/any‐of‐type matching
    public List<String> typeAny;
    public String typePattern;
}
