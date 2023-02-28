package com.dan323.uses.classical;

import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.LogicalSolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClassicalConfiguration {

    @Bean
    LogicalSolver classicalSolver(){
        return new ClassicSolver();
    }

    @Bean
    LogicalGetActions classicalActions(){
        return new ClassicGetActions();
    }

}
