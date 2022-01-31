package com.dan323.bean;

import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.bean.*;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;

import com.dan323.proof.generic.proof.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;

@Configuration
public class ClassicalConfiguration {

    @Bean
    public NaturalDeductionFactory<NaturalDeduction, ClassicalLogicOperation, ClassicalLogicOperation, ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>> naturalDeductionFactory(){
        return NaturalDeductionFactory.of(() -> "classical", NaturalDeduction::new);
    }

    @Bean
    public ExpressionParser<ClassicalLogicOperation> parse(){
        return ExpressionParser.of(ParseClassicalAction::parseExpression, () -> "classical");
    }

    @Bean
    public Actions<Input<Void>,ClassicalLogicOperation,ClassicalAction> classicalActions(){
        return new ClassicalActions();
    }

    @Bean
    public <B extends ToAssmParser.AssumptionSupplier<ClassicalLogicOperation>> ToAssmParser<B,ClassicalLogicOperation, ClassicalLogicOperation> toAssmParser(ExpressionParser<ClassicalLogicOperation> parser){
        return ToAssmParser.from(() -> "classical", (assmsSupp) -> parser.apply(assmsSupp.expression()));
    }

    @Bean
    public <B extends ToGoalParser.GoalSupplier<ClassicalLogicOperation>> ToGoalParser<B,ClassicalLogicOperation, ClassicalLogicOperation> toGoalParser(ExpressionParser<ClassicalLogicOperation> parser){
        return ToGoalParser.from(() -> "classical", (assmsSupp) -> parser.apply(assmsSupp.expression()));
    }

    @Bean
    public ToModel<Void, ProofStep<ClassicalLogicOperation>, ClassicalLogicOperation> toModelClassical(ExpressionParser<ClassicalLogicOperation> parser){
        Function<Step<Void>, ProofStep<ClassicalLogicOperation>> fun = (step) -> new ProofStep<>(step.assmsLevel(), parser.apply(step.expression()), new ProofReason(step.ruleString().name(), step.ruleString().lines()));
        Function<ProofStep<ClassicalLogicOperation>, Step<Void>> funInv = step -> new Step<>() {
            @Override
            public String expression() {
                return step.getStep().toString();
            }

            @Override
            public Void extraInformation() {
                return null;
            }

            @Override
            public int assmsLevel() {
                return step.getAssumptionLevel();
            }

            @Override
            public StepRule ruleString() {
                return new StepRule() {
                    @Override
                    public String name() {
                        return step.getProof().getNameProof();
                    }

                    @Override
                    public List<Integer> lines() {
                        return step.getProof().getLines();
                    }
                };
            }
        };

        return ToModel.from(() -> "classical", fun, funInv);
    }

    @Bean
    public static ParseAction<ClassicalAction, NaturalDeduction> parseAction(){
        return new ParseAction<>() {
            @Override
            public String ofLogic() {
                return "classical";
            }

            @Override
            public ClassicalAction parse(NaturalDeduction proof, int pos) {
                return ParseClassicalAction.parse(proof, pos);
            }
        };
    }

}
