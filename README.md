# Generic Recipe-Based Java Parser Transformer

This tool is a generic recipe-based transformer for Java source code, built on top of the JavaParser library. It allows you to define a series of transformations in JSON files (recipes) that can be applied to a Java project to perform large-scale refactoring, such as migrating from Java EE to Spring Boot.

## Building the Project

To build the engine, run the following Maven command from the root directory of the project:

```bash
mvn clean install
```

This will create two important artifacts:
1.  A shaded JAR file in `engine/target/` named `engine-1.0-SNAPSHOT-shaded.jar`. This JAR contains all the necessary dependencies to run the tool.
2.  A JAR file in `custom-actions/target/` named `custom-actions-1.0-SNAPSHOT.jar`. This project, located in `custom-actions/`, is where you can define your own transformation actions. These actions are loaded into the engine at runtime via Java's `ServiceLoader` mechanism.

## How It Works

The transformation process is driven by JSON recipes. You specify a sequence of recipes to be executed in a `config.json` file. The engine applies each recipe to the input source code, with the output of one recipe serving as the input for the next.

The recipes listed in `config.json` are executed sequentially.

**Example `config.json`:**
```json
[
  "ejb-to-spring-beans",
  "jsf-beans-to-spring-components",
  "producer-to-configuration",
  "cdi-to-spring-injection",
  "jaxrs-to-spring-mvc",
  "lifecycle-and-logging",
  "jax-spring-annotation-mappings"
]
```

### The Recipe Structure

A recipe is a JSON file containing one or more named recipes. Each recipe has a list of steps, and each step has a `match` block to identify the code to be modified and an `actions` block that defines the modifications.

Here is a more detailed example:

```json
{
  "recipes": [
    {
        "name": "ReplaceDateWithLocalDateTime",
        "description": "Replace Date with LocalDateTime and new Date() with LocalDateTime.now().",
        "rollbackOnError": "TypeCompatibilityRule",
        "imports": {
            "add": [
                "java.time.LocalDateTime"
            ],
            "remove": []
        },
        "steps": [
            {
                "match": {
                    "nodeType": "ObjectCreationExpr",
                    "fqn": "java.util.Date"
                },
                "actions": [
                    {
                        "replaceWithMethodCall": {
                            "scope": "LocalDateTime",
                            "method": "now"
                        }
                    }
                ]
            },
            {
                "match": {
                    "nodeType": "VariableDeclarationExpr",
                    "type": "java.util.Date"
                },
                "actions": [
                    {
                        "changeType": {
                            "newType": "java.time.LocalDateTime"
                        }
                    }
                ]
            },
            {
                "match": {
                    "nodeType": "Parameter",
                    "type": "java.util.Date"
                },
                "actions": [
                    {
                        "changeType": {
                            "newType": "java.time.LocalDateTime"
                        }
                    }
                ]
            },
            {
                "match": {
                    "nodeType": "MethodCallExpr",
                    "methodName": "awaitUntil",
                    "argumentType": "java.time.LocalDateTime",
                    "expectedParamType": "java.util.Date"
                },
                "actions": [
                    {
                        "wrapArgument": {
                            "template": "Date.from($ARG$.atZone(ZoneId.systemDefault()).toInstant())",
                            "addImports": [
                                "java.util.Date",
                                "java.time.ZoneId"
                            ]
                        }
                    }
                ]
            }
        ]
    }
  ]
}
```
- **name**: A unique identifier for the recipe.
- **imports**: A list of imports to be added to any file modified by this recipe.
- **rollbackOnError**: The name of a validator. If this validator fails after the recipe runs, all changes from this recipe will be rolled back.
- **steps**: An array of match/action blocks that perform the actual transformation.

### Understanding the Documentation

To create effective recipes, you must understand the available `match` keys and `actions`. This information is located in the `docs/` directory:

-   `docs/nodeTypes.yml`: Lists all the AST node types that the tool can match against.
-   `docs/matches.yml`: Describes all the possible keys you can use in a `match` block.
-   `docs/actions.yml`: Details all available actions and their parameters.

**Example snippet from `docs/actions.yml`:**
```yaml
- key: changeType
  class: ChangeTypeAction
  description: >
    Change the declared type of a variable, parameter, method return,
    object creation, or field to a new type.
  parameters:
    - name: newType
  appliesTo:
    - VariableDeclarationExpr
    - FieldDeclaration
```

### Validators and Rollback

The engine supports validators that can check the code for certain conditions. If a validator fails, and the recipe is configured with `rollbackOnError`, the engine will revert the changes made by that recipe.

**Example `output.log` snippet showing a rollback:**
```
INFO: Executing recipe: Migrate-Javax-Annotations
INFO: Matched node: @Resource private UserTransaction transaction;
INFO: Executing action: changeType
INFO: Executing action: addAnnotation
INFO: Finished recipe: Migrate-Javax-Annotations
INFO: Running validator: check-for-javax
ERROR: Validator 'check-for-javax' failed for file: com/mycompany/MyService.java. Found 'javax.transaction.UserTransaction'.
INFO: Rolling back changes for: com/mycompany/MyService.java
```

## Running the Application

You can run the transformation from the command line.

### CLI Arguments

The basic command to run the tool is:
```bash
java -cp "engine/target/engine-1.0-SNAPSHOT-shaded.jar" gst.Main <input_path>
```
**Example:**
```bash
java -cp "engine/target/engine-1.0-SNAPSHOT-shaded.jar" gst.Main resources/input/java-application-petstore-ee7
```

### Using Custom Actions

To use custom actions, include the `custom-actions.jar` in the classpath, separated by a semicolon (Windows) or colon (Linux/macOS).
```bash
# Windows
java -cp "engine/target/engine-1.0-SNAPSHOT-shaded.jar;custom-actions/target/custom-actions-1.0-SNAPSHOT.jar" gst.Main <input_path>
```

### Debugging

-   `output/output.log`: Contains detailed logs of matched nodes and actions performed.

**Example `output.log` snippet:**

    ```
    INFO: Matched node: private Logger logger = Logger.getLogger(SampleController.class.getName());
    INFO: Executing action: replaceWithMethodCall
    ```

-   `--match-debug`: A flag for more verbose logging to debug why a node did or did not match.
    ```bash
    java -cp "engine/target/engine-1.0-SNAPSHOT-shaded.jar" gst.Main <input_path> --match-debug
    ```

## Customization

The engine is designed to be extensible.

### Adding a New Node Type to Match

To process a new AST node type, you need to add it to the `findCandidates` method in `engine/src/main/java/gst/engine/matcher/NodeMatcher.java`.

**Example: Adding `RecordDeclaration`**
```java
// ... in NodeMatcher.java
public static List<Node> findCandidates(Node root, String nodeType) {
    return switch (nodeType) {
        // ... existing cases
        case "RecordDeclaration":
            return root.findAll(RecordDeclaration.class).stream().map(n -> (Node) n).collect(Collectors.toList());
        default:
            // ...
    };
}
```

### Adding New Match Logic

To add a new match key (e.g., `hasConstructor`), first add the field to `gst.api.Match.java`, then implement the logic in the `matches` method of `NodeMatcher.java`.

**Example: Adding a new check in `matches`**
```java
// ... in NodeMatcher.java
public static MatchResult matches(Node node, Match m, CombinedTypeSolver typeSolver) {
    List<String> failures = new ArrayList<>();
    // ... existing checks

    // New check for a custom key `namePattern` on a ClassOrInterfaceDeclaration
    if (m.namePattern != null && node instanceof ClassOrInterfaceDeclaration coid) {
        if (!Pattern.matches(m.namePattern, coid.getNameAsString())) {
            failures.add("namePattern '" + m.namePattern + "' does not match class name '" + co.getNameAsString() + "'");
        }
    }

    return new MatchResult(failures.isEmpty(), failures);
}
```

### The `Match.java` Class

This class holds all supported fields for the `match` block in a recipe. When adding a new match key, you must first declare it here.

**Snippet from `Match.java`:**
```java
// ... in gst.api.Match.java
@JsonIgnoreProperties(ignoreUnknown = false)
public class Match {
    public String nodeType;
    public String fqn;
    public String type;
    public String methodName;
    public String fqnScope;
    public String annotation;
    public String namePattern;
    // ... other fields
}
```

### Adding a New Action

To add a new action:
1.  Create a new class that implements the `Action` interface (e.g., in the `custom-actions` project).
2.  Register the action by adding a `case` for it in the `create` method of `engine/src/main/java/gst/engine/actions/ActionFactory.java`.

**Example: Adding a `myNewAction` case in `ActionFactory.java`**
```java
// ... in ActionFactory.java
public static Action create(String name, Map<String, Object> params) {
    // ...
    switch (name) {
        // ... existing cases
        case "addAnnotationToParentClass":
            return new AddAnnotationToParentClassAction(params);
        case "myNewAction":
            return new MyNewAction(params);
    }
    // ...
}
```

## Things to Look Out For

-   **Correct Match Keys:** Always use the correct match keys for a given `nodeType` as specified in `docs/matches.yml`. Using an unsupported key for a node type will be ignored.
-   **NodeMatcher Configuration:** If a `nodeType` needs to support a new match key, you must add the logic for it in `NodeMatcher.java`.
-   **Action Parameters:** Ensure you provide all necessary parameters for an action as documented in `docs/actions.yml`. Missing parameters will cause the action to fail.
