package com.dan323.uses.classical;

import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.LogicalSolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClassicalConfiguration {

    @Bean
    public LogicalSolver classicalSolver(){
        return new ClassicSolver();
    }

    @Bean
    public LogicalGetActions classicalActions(){
        return new ClassicGetActions();
    }

}
