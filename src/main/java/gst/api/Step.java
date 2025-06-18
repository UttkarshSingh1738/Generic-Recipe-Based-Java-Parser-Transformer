package gst.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Step {
    public Match match;
    /** 
     * Each action entry is a single-key map, e.g.
     *   { "changeType": { "newType": "var" } }
     */
    public List<Map<String,Map<String,Object>>> actions;
}
