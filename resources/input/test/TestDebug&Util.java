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