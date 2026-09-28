package com.dan323.proof.firstorder;

import com.dan323.expressions.firstorder.Exists;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.expressions.firstorder.Forall;
import com.dan323.expressions.firstorder.Term;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.firstorder.proof.ParseFirstOrderAction;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FirstOrderTestUtils {

    private FirstOrderTestUtils() {
    }

    static FirstOrderOperation formula(String text) {
        return ParseFirstOrderAction.parseExpression(text);
    }

    static Term term(String text) {
        return ParseFirstOrderAction.parseTerm(text);
    }

    static Forall forall(String text) {
        return (Forall) formula(text);
    }

    static Exists exists(String text) {
        return (Exists) formula(text);
    }

    /**
     * @return a proof whose premises are {@code premises} and whose goal is {@code goal}
     */
    static FirstOrderNaturalDeduction proofWithGoal(String goal, String... premises) {
        var proof = new FirstOrderNaturalDeduction();
        proof.initializeProof(Stream.of(premises).map(FirstOrderTestUtils::formula).toList(), formula(goal));
        return proof;
    }

    /**
     * @return a proof whose premises are {@code premises}, with a goal no test reaches
     */
    static FirstOrderNaturalDeduction proof(String... premises) {
        return proofWithGoal("Unreached", premises);
    }

    static void assume(FirstOrderNaturalDeduction proof, String formula) {
        apply(proof, new FirstOrderAssume(formula(formula)));
    }

    /**
     * Checks that {@code action} is valid and applies it.
     */
    static void apply(FirstOrderNaturalDeduction proof, FirstOrderAction action) {
        assertTrue(action.isValid(proof), () -> action.getClass().getSimpleName() + " should be valid on\n" + proof);
        action.apply(proof);
    }

    /**
     * Checks that {@code action} is valid, applies it and checks that the last step is {@code formula}.
     */
    static void applies(FirstOrderNaturalDeduction proof, FirstOrderAction action, String formula) {
        apply(proof, action);
        assertEquals(formula(formula), proof.getSteps().getLast().getStep());
    }

    /**
     * Rebuilds {@code proof} from its own steps, through {@link FirstOrderNaturalDeduction#parse()}, and checks that
     * the result prints the same.
     */
    static FirstOrderNaturalDeduction replay(FirstOrderNaturalDeduction proof) {
        List<FirstOrderAction> actions = proof.parse();
        assertEquals(proof.getSteps().size(), actions.size());
        var copy = new FirstOrderNaturalDeduction();
        copy.initializeProof(proof.getAssms(), proof.getGoal());
        for (FirstOrderAction action : actions.subList(proof.getAssms().size(), actions.size())) {
            apply(copy, action);
        }
        assertEquals(proof.toString(), copy.toString());
        return copy;
    }
}
