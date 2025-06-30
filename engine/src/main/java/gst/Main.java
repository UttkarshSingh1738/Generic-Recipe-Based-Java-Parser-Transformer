package gst;

import java.nio.file.Path;
import java.nio.file.Paths;

import gst.engine.Pipeline;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: java -jar engine.jar <mappingFile> <inputRoot> <outputRoot>");
            System.exit(1);
        }
        Path mappingFile = Paths.get(args[0]);
        Path inputRoot   = Paths.get(args[1]);
        Path outputRoot  = Paths.get(args[2]);

        Pipeline.run(mappingFile, inputRoot, outputRoot);
    }
}
