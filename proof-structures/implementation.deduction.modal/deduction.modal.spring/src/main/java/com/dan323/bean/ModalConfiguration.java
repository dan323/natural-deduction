package com.dan323.bean;

import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.bean.Actions;
import com.dan323.proof.generic.bean.ExpressionParser;
import com.dan323.proof.generic.bean.NaturalDeductionFactory;
import com.dan323.proof.generic.proof.ParseAction;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ParseModalAction;
import com.dan323.proof.modal.proof.ProofStepModal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class ModalConfiguration {

    @Bean
    public NaturalDeductionFactory<ModalNaturalDeduction,ModalOperation, ProofStepModal> parseAction() {
        return NaturalDeductionFactory.of();
    }

    @Bean
    public ExpressionParser<ModalOperation> parse(){
        return ExpressionParser.of(ParseModalAction::parseExpression,() -> "modal");
    }

    @Bean
    public Actions modalActions(){
        return new ModalActions();
    }

}
