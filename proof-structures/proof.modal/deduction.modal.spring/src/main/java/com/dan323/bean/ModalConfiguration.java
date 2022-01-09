package com.dan323.bean;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.bean.*;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.Step;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.ModalAction;
import com.dan323.proof.modal.proof.LabeledExpression;
import com.dan323.proof.modal.proof.LabeledProofStep;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ParseModalAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

@Configuration
public class ModalConfiguration {

    @Bean
    public <T> NaturalDeductionFactory<ModalNaturalDeduction<T>, LabeledExpression<T>, LabeledExpression<T>, ModalOperation, LabeledProofStep<T>> ModalNDFactory() {
        return NaturalDeductionFactory.of(() -> "modal", ModalNaturalDeduction::new);
    }

    @Bean
    public ExpressionParser<ModalOperation> parseModal() {
        return ExpressionParser.of(ParseModalAction::parseExpression, () -> "modal");
    }

    @Bean
    public <T> Actions<Input<T>,ModalOperation, ModalAction<T>> modalActions() {
        return new ModalActions<>();
    }

    @Bean
    public <B extends ToAssmParser.AssumptionSupplier<T>, T> ToAssmParser<B, T, LabeledExpression<T>> modalToAssms(ExpressionParser<ModalOperation> parser) {
        return ToAssmParser.from(() -> "modal", (assmSupp) -> {
            ModalOperation mo = parser.apply(assmSupp.expression());
            if (mo instanceof ModalLogicalOperation mlo) {
                return new LabeledExpression<>(assmSupp.extraInformation(), mlo);
            } else if (mo instanceof RelationOperation ro) {
                return new LabeledExpression<>((RelationOperation<T>) ro);
            } else {
                return null;
            }
        });
    }

    @Bean
    public <B extends ToGoalParser.GoalSupplier<T>, T> ToGoalParser<B, T, LabeledExpression<T>> modalToGoal(ExpressionParser<ModalOperation> parser) {
        return ToGoalParser.from(() -> "modal", (assmSupp) -> {
            ModalOperation mo = parser.apply(assmSupp.expression());
            if (mo instanceof ModalLogicalOperation mlo) {
                return new LabeledExpression<>(assmSupp.extraInformation(), mlo);
            } else if (mo instanceof RelationOperation ro) {
                return new LabeledExpression<>((RelationOperation<T>) ro);
            } else {
                return null;
            }
        });
    }

    @Bean
    public <T extends Serializable> ToModel<T, LabeledProofStep<T>, ModalOperation> toModelModal(ExpressionParser<ModalOperation> parser) {
        Function<Step<T>, LabeledProofStep<T>> function = (step) -> {
            var mo = parser.apply(step.expression());
            if (mo instanceof RelationOperation ro) {
                return new LabeledProofStep<T>(step.assmsLevel(), (RelationOperation<T>) ro, new ProofReason(step.ruleString().name(), step.ruleString().lines()));
            } else {
                return new LabeledProofStep<>(step.assmsLevel(), step.extraInformation(), (ModalLogicalOperation) mo, new ProofReason(step.ruleString().name(), step.ruleString().lines()));
            }
        };
        Function<LabeledProofStep<T>,Step<T>> funcInv = step -> new Step<>() {
            @Override
            public String expression() {
                return step.getStep().toString();
            }

            @Override
            public T extraInformation() {
                return step.getState().orElse(null);
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
        return ToModel.from(() -> "modal", function, funcInv);
    }

    @Bean
    public <T> ParseAction<AbstractModalAction<T>, ModalNaturalDeduction<T>> modalParser() {
        return ParseAction.toParseAction(() -> "modal", ParseModalAction::parse);
    }

}
