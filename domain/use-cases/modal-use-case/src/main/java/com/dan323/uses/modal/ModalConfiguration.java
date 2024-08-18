package com.dan323.uses.modal;

import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.ProofParser;
import com.dan323.uses.Transformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModalConfiguration {

    @Bean
    public LogicalSolver modalSolver() {
        return new LogicalSolver(modalTransformer());
    }

    @Bean
    public LogicalGetActions modalActions() {
        return new ModalGetActions();
    }

    @Bean
    public Transformer modalTransformer() {
        return new ModalProofTransformer();
    }

    @Bean
    public ProofParser modalProofParser() {
        return new ModalProofParser();
    }

}
