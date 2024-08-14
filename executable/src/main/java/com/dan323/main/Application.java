package com.dan323.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(ApplicationConfiguration.class)
public class Application {

    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        Application.applicationContext = SpringApplication.run(Application.class, args);
    }
}
