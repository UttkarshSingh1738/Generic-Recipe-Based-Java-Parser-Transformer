package gst.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Recipe {
    public String name;
    public String description;
    public ImportMods imports;
    public List<Step> steps;
}
