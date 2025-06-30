package com.rest.app.controller;


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
            return HealthIndicationStatus.healthy(name);    //Health.up().build();
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
