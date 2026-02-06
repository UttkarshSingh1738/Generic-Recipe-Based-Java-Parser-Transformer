package gst;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import gst.engine.Pipeline;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java -cp \"engine.jar;custom-actions.jar\" gst.Main <inputSourceRoot> [--match-debug]");
            System.err.println("  <inputSourceRoot>  Path to input directory, relative to project root (directory containing config.json).");
            System.err.println("  Example: resources/input/NetworkManagement");
            System.exit(1);
        }

        boolean matchDebug = args.length == 2 && "--match-debug".equals(args[1]);

        Path cwd = Paths.get("").toAbsolutePath();

        // Project base = directory containing config.json (cwd or parent, so running from engine/ still works)
        Path baseDir = cwd;
        Path configInCwd = cwd.resolve("config.json");
        if (!Files.isRegularFile(configInCwd) && cwd.getParent() != null) {
            Path configInParent = cwd.getParent().resolve("config.json");
            if (Files.isRegularFile(configInParent)) {
                baseDir = cwd.getParent();
            }
        }
        Path configJson = baseDir.resolve("config.json");
        if (!Files.isRegularFile(configJson)) {
            System.err.println(">>> Missing config.json. Looked in: " + cwd + " and " + (cwd.getParent() != null ? cwd.getParent() : "(no parent)"));
            System.err.println(">>> Put config.json in the project root (or run from the project root).");
            System.exit(3);
        }
        System.err.println("> Project base: " + baseDir);

        Path logsDir = baseDir.resolve("output");
        Files.createDirectories(logsDir);
        Path logFile = logsDir.resolve("output.log");
        PrintStream ps = new PrintStream(
                Files.newOutputStream(logFile,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING), /* autoFlush */ true
        );
        System.setOut(ps);

        Path inputRoot = baseDir.resolve(args[0]).normalize();
        if (!Files.isDirectory(inputRoot)) {
            System.err.println(">>> Input path is not a directory: " + inputRoot);
            System.err.println(">>> (Resolved from base " + baseDir + " + " + args[0] + ")");
            System.exit(2);
        }
        List<String> mappingNames;
        try {
            mappingNames = new ObjectMapper()
                    .readValue(Files.newBufferedReader(configJson),
                            new TypeReference<List<String>>() {
                    });
        } catch (IOException e) {
            System.err.println(">>> Failed to parse config.json: " + e.getMessage());
            System.exit(4);
            return;
        }
        if (mappingNames.isEmpty()) {
            System.err.println(">>> config.json contains no mappings to apply");
            System.exit(5);
        }

        List<Path> extraJars = new ArrayList<>();
        for (String dir : List.of("JARs", "jars")) {
            Path d = baseDir.resolve(dir);
            if (Files.isDirectory(d)) {
                try (Stream<Path> s = Files.list(d)) {
                    s.filter(p -> p.toString().endsWith(".jar"))
                            .forEach(extraJars::add);
                }
            }
        }
        System.err.println("> Found support JARs: " + extraJars);

        Files.createDirectories(logsDir);

        Path currentIn = inputRoot;
        for (int i = 0; i < mappingNames.size(); i++) {
            String mapName = mappingNames.get(i);
            Path mappingFile = baseDir.resolve("resources").resolve(mapName + ".json");
            if (!Files.isRegularFile(mappingFile)) {
                System.err.println(">>> Mapping not found: " + mappingFile);
                System.exit(6);
            }

            Path outRoot = logsDir.resolve(mapName);
            Files.createDirectories(outRoot);

            Path thisOut = outRoot.resolve(inputRoot.getFileName().toString());
            Files.createDirectories(thisOut);

            System.err.println(">  [" + (i + 1) + "/" + mappingNames.size() + "] "
                    + mapName + ": " + currentIn + " >>> " + thisOut);

            Pipeline.run(mappingFile, currentIn, thisOut, extraJars, matchDebug);
            currentIn = thisOut;
        }

        System.err.println("All mappings applied; final output in " + currentIn);
    }
}