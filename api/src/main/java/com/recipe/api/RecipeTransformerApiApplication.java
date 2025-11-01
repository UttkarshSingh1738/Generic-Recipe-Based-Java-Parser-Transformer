package com.recipe.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {"com.recipe.api", "com.recipe.rag"})
public class RecipeTransformerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecipeTransformerApiApplication.class, args);
    }
}

