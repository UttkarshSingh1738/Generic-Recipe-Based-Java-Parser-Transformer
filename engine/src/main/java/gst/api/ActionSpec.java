package gst.api;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = ActionSpecDeserializer.class)
public class ActionSpec {
    private final String key;
    private final Map<String,Object> params;

    public ActionSpec(String key, Map<String,Object> params) {
        this.key    = key;
        this.params = params;
    }

    public String getKey() {
        return key;
    }

    public Map<String,Object> getParams() {
        return params;
    }
}
