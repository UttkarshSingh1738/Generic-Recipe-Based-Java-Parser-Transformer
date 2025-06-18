package gst.engine.validator;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;

public class ValidationError {
    private final String filePath;
    private final Range location;
    private final String message;

    public ValidationError(String filePath, Node node, String message) {
        this.filePath = filePath;
        this.location = node.getRange().orElse(null);
        this.message  = message;
    }

    @Override
    public String toString() {
        String loc = location != null
            ? filePath + ":" + location.begin.line + ":" + location.begin.column
            : filePath;
        return loc + "  ▶ " + message;
    }
}
