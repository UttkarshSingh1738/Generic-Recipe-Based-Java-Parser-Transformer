package gst.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Recipe {
    public String name;
    public String description;
    public ImportMods imports;
    public List<Step> steps;
    public boolean rollbackOnError = false;
}
