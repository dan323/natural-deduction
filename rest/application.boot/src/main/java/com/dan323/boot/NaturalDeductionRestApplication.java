package com.dan323.boot;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.dan323.bean","com.dan323.api","com.dan323.adapter"})
public class NaturalDeductionRestApplication {

    public static void main(String[] args){
        new SpringApplicationBuilder(NaturalDeductionRestApplication.class)
                .web(WebApplicationType.REACTIVE)
                .run(args);
    }

}
