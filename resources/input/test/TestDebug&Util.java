import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestTemplate;
import org.apache.logging.log4j.CustomLogManager;
import org.springframework.stereotype.HealthIndication; //import org.springframework.stereotype.HealthIndicator;
import org.springframework.stereotype.HealthIndicationStatus;   //import org.springframework.stereotype.Health;
import javax.inject.Inject;


public class CustomHealthCheck implements HealthIndication {    //HealthIndicator
    private static final Logger logger = CustomLogManager.getLogger(ProductController.class);
    @InjectRestServiceClient(name = "A")
    private RestTemplate restTemplateA;
    @InjectRestServiceClient(name = "B")
    private RestTemplate restTemplateB;
    @Override
    public HealthIndicationStatus health(String name) { //public Health isHealthy() {}
        try {
            restTemplateA.exchange();
            logger.info(restTemplateA);
            restTemplateB.exchange();
            logger.info(restTemplateB);
            return HealthIndicationStatus.healthy(name, testing, testing1);    // TODO: Health.up().build();
        } catch (Exception e) {
            logger.error("Connection failed");
            return HealthIndicationStatus.unHealthy(name);    //Health.down().build();
        }
    }
}

////////////////////////////////////////////////////////////

class TestExample {
    String greeting(String name) {
        return Optional.ofNullable(name)
                       .orElse("world");
    }
}

public class FooUtil {
    public static void help() {
        int sum = 1 + 2;
        System.out.println("debug!");
    }
}

public class TestExample {
    void foo() {
    }
}

class Demo {
    void foo() {
        System.out.println("hello");
        System.err.println("oops");
    }
}

public class PatternTest {
    void test(Object obj) {
        if (obj instanceof Integer) {
            Integer i = (Integer) obj;
            System.out.println(i + 1);
        }
    }
    
    // Additional test cases for InstanceOfToPatternAction
    void testString(Object obj) {
        if (obj instanceof String) {
            String s = (String) obj;
            System.out.println("String length: " + s.length());
        }
    }
    
    void testArrayType(Object obj) {
        if (obj instanceof int[]) {
            int[] arr = (int[]) obj;
            System.out.println("Array length: " + arr.length);
        }
    }
    
    void testCustomType(Object obj) {
        if (obj instanceof CustomHealthCheck) {
            CustomHealthCheck check = (CustomHealthCheck) obj;
            check.health("test");
        }
    }
}

public class StringFormatTest {
    private static final Random RANDOM = new Random();
    
    void testBasicStringFormat() {
        // Simple format calls
        String msg1 = String.format("Hello %s", "world");
        String msg2 = String.format("User %s has %d points", "John", 100);
        String msg3 = String.format("Value: %s", getValue());
        
        // Complex expressions as arguments
        this.setId(String.format("randomId-%s-%s", RANDOM.nextInt(100), System.currentTimeMillis() + ""));
        
        // Multi-line format
        String error = String.format("Workflow parsing process failed, \"%s\" may not be a valid workflow.", 
                                    workflow.getId());
        
        // Inside method calls
        log.error(String.format("Activity from workflow %s failed", execution.getProcessDefinitionId()), e);
        logger.info(String.format("Processing %s with status %s", item.getName(), item.getStatus()));
        
        // Multiple placeholders
        String path = String.format("%s.%s.%s", activityId, ActivityExecutorContext.OUTPUTS, key);
        String query = String.format("SELECT * FROM %s WHERE id = %s AND status = '%s'", 
                                    tableName, id, status);
        
        // Nested calls
        System.out.println(String.format("Result: %s", String.format("Count: %d", items.size())));
        
        // With constants
        String template = String.format(ERROR_TEMPLATE, errorCode, errorMessage);
        String url = String.format("https://api.example.com/users/%s/posts/%d", userId, postId);
    }
    
    void testEdgeCases() {
        // Single argument
        String single = String.format("Simple: %s", value);
        
        // No arguments (should not be transformed)
        String noArgs = String.format("No placeholders here");
        
        // Complex first argument (parentheses test)
        String complex = String.format(getTemplate() + " suffix", arg1, arg2);
        String methodCall = String.format(buildTemplate("prefix"), data);
        
        // With escape sequences
        String escaped = String.format("Path: %s\\n\\tFile: %s", dir, file);
    }
    
    private String getValue() { return "test"; }
    private void setId(String id) {}
}

public class TextBlockTest {
    
    void testBasicConcatenation() {
        // Basic string concatenation with newlines
        String html = "<ul>\n"
                    + "  <li>foo</li>\n"
                    + "  <li>bar</li>\n"
                    + "</ul>";
        
        // Multi-line SQL query
        String query = "SELECT u.name, u.email, p.title \n"
                     + "FROM users u \n"
                     + "JOIN posts p ON u.id = p.user_id \n"
                     + "WHERE u.active = true";
        
        // Log message with newlines
        String logMessage = "Error occurred during processing:\n"
                          + "  - Input validation failed\n"
                          + "  - Database connection lost\n"
                          + "  - Retry attempts exhausted";
    }
    
    void testComplexConcatenation() {
        // JSON-like structure
        String json = "{\n"
                    + "  \"name\": \"John Doe\",\n"
                    + "  \"age\": 30,\n"
                    + "  \"address\": {\n"
                    + "    \"street\": \"123 Main St\",\n"
                    + "    \"city\": \"Anytown\"\n"
                    + "  }\n"
                    + "}";
        
        // Multi-line error message
        String errorMsg = "An unknown error occurred while trying to connect to the database.\n"
                        + "Please check the following:\n"
                        + "  1. Network connectivity\n"
                        + "  2. Database credentials\n"
                        + "  3. Firewall settings\n"
                        + "For more information, contact support.";
    }
    
    void testEdgeCases() {
        // Concatenation without newlines (should/shouldn't convert based on stringsWithoutNewlines)
        String simple = "Hello " + "world " + "from " + "Java";
        
        // Mixed content with tabs and newlines
        String mixed = "Name:\tJohn\n"
                     + "Age:\t30\n"
                     + "City:\tNew York";
        
        // Empty lines in concatenation
        String withEmpty = "Line 1\n"
                         + "\n"
                         + "Line 3\n"
                         + "\n"
                         + "Line 5";
        
        // Single line with escaped characters
        String escaped = "Path: C:\\Users\\John\\Documents\n"
                       + "File: data.txt";
        
        // Documentation style
        String docs = "/**\n"
                    + " * This is a multi-line comment\n"
                    + " * that describes the function\n"
                    + " * @param input the input parameter\n"
                    + " * @return the result\n"
                    + " */";
    }
    
    void testRealWorldExamples() {
        // Example from the OpenRewrite documentation
        logDeprecation("The datafeed 1 service will be fully replaced by the datafeed 2 service in the future. "
            + "Please consider migrating over to datafeed 2. For more information on the timeline as well as on the "
            + "benefits of datafeed 2, please reach out to your Technical Account Manager or to our developer "
            + "documentation https://docs.developers.symphony.com/building-bots-on-symphony/datafeed)");
        
        // Error message example
        String messageError = String.format("An unknown error occurred while trying to connect to %s. Please check below "
            + "for more information: ", address);
        
        // Expected vs actual message
        String testFailure = "Expected: controller used to showcase what " 
                           + "happens when an exception is thrown";
        
        // Template with parameters
        String template = "Hello {{name}},\n"
                        + "\n"
                        + "Welcome to our service!\n"
                        + "\n"
                        + "Best regards,\n"
                        + "The Team";
    }
    
    private void logDeprecation(String message) {}
}

public class MethodTargetToStaticTest {
    
    void testToolProviderCalls() {
        // Instance creation + method call
        ToolProvider provider = new ToolProvider();
        JavaCompiler compiler1 = provider.getSystemJavaCompiler();
        DocumentationTool docTool1 = provider.getSystemDocumentationTool();
        ClassLoader loader1 = provider.getSystemToolClassLoader();
        
        // Direct instantiation + method call  
        JavaCompiler compiler2 = new ToolProvider().getSystemJavaCompiler();
        DocumentationTool docTool2 = new ToolProvider().getSystemDocumentationTool();
        ClassLoader loader2 = new ToolProvider().getSystemToolClassLoader();
        
        // Variable-based calls
        ToolProvider tp = getToolProvider();
        JavaCompiler compiler3 = tp.getSystemJavaCompiler();
    }
    
    void testModifierCalls() {
        // Instance-style Modifier calls
        Modifier mod = new Modifier();
        boolean isPublic = mod.isPublic(123);
        boolean isStatic = mod.isStatic(456);
        boolean isPrivate = mod.isPrivate(789);
        
        // Method call on new instance
        boolean isFinal = new Modifier().isFinal(999);
        String toString = new Modifier().toString(111);
    }
    
    void testConstantBootstrapsCalls() {
        // Instance-style ConstantBootstraps calls
        ConstantBootstraps bootstraps = new ConstantBootstraps();
        Object primitive = bootstraps.primitiveClass(lookup, "int", int.class);
        Object enumConstant = bootstraps.enumConstant(lookup, "VALUE", Enum.class);
        
        // Direct instantiation calls
        Object primitive2 = new ConstantBootstraps().primitiveClass(lookup, "int", int.class);
    }
    
    void testEdgeCases() {
        // Chained calls (should only convert the matching method)
        String result = new ToolProvider().getSystemJavaCompiler().toString();
        
        // Nested calls
        process(new ToolProvider().getSystemJavaCompiler());
        
        // Already static calls (should be ignored)
        JavaCompiler staticCompiler = ToolProvider.getSystemJavaCompiler();
    }
    
    private ToolProvider getToolProvider() { return new ToolProvider(); }
    private void process(Object obj) {}
}

// Test cases for ForToForEachAction
public class ForLoopTest {
    void testArrayLoop() {
        int[] numbers = {1, 2, 3, 4, 5};
        for (int i = 0; i < numbers.length; i++) {
            System.out.println(numbers[i]);
        }
    }
    
    void testListLoop() {
        java.util.List<String> items = java.util.Arrays.asList("a", "b", "c");
        for (int i = 0; i < items.size(); i++) {
            System.out.println(items.get(i));
        }
    }
    
    void testStringArrayLoop() {
        String[] words = {"hello", "world"};
        for (int j = 0; j < words.length; j++) {
            System.out.println(words[j].toUpperCase());
        }
    }
    
    void testUnsafeLoop() {
        int[] data = {1, 2, 3};
        for (int i = 0; i < data.length; i++) {
            System.out.println(i); // Uses index directly - should not convert
        }
    }
}

// Test cases for SwitchToReturnExpressionAction  
public class SwitchReturnTest {
    String getValueByType(int type) {
        switch (type) {
            case 1:
                return "One";
            case 2:
                return "Two";
            case 3:
                return "Three";
            default:
                return "Unknown";
        }
    }
    
    int getNumberByChar(char c) {
        switch (c) {
            case 'a':
                return 1;
            case 'b':
                return 2;
            default:
                return 0;
        }
    }
    
    void unsafeSwitch(int value) {
        switch (value) {
            case 1:
                System.out.println("Complex case");
                doSomething();
                return;
            case 2:
                return; // No expression - should not convert
        }
    }
    
    private void doSomething() {}
}