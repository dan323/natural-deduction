package com.dan323.bean;

import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.bean.Actions;
import com.dan323.proof.generic.bean.ExpressionParser;
import com.dan323.proof.generic.bean.NaturalDeductionFactory;
import com.dan323.proof.generic.proof.ProofStep;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClassicalConfiguration {

    @Bean
    public NaturalDeductionFactory<NaturalDeduction,ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> naturalDeductionFactory(){
        return NaturalDeductionFactory.of(() -> "classical", NaturalDeduction::new);
    }

    @Bean
    public ExpressionParser<ClassicalLogicOperation> parse(){
        return ExpressionParser.of(ParseClassicalAction::parseExpression, () -> "classical");
    }

    @Bean
    public Actions classicalActions(){
        return new ClassicalActions();
    }
}
