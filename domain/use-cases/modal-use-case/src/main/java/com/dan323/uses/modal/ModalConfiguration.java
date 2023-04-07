package com.dan323.uses.modal;

import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.LogicalSolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModalConfiguration {

    @Bean
    public LogicalSolver modalSolver(){
        return new ModalSolver<>();
    }

    @Bean
    public LogicalGetActions modalActions(){
        return new ModalGetActions();
    }

}
