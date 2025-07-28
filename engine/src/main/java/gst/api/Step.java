package gst.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Step {
    public Match match;
    public List<ActionSpec> actions;
}
