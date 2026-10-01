package com.dan323.proof.firstorder.internal;

import com.dan323.expressions.firstorder.DisjunctionFirstOrder;
import com.dan323.expressions.firstorder.Exists;
import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.expressions.firstorder.Forall;
import com.dan323.proof.firstorder.*;
import com.dan323.proof.firstorder.internal.FirstOrderAutomate.Node;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Writes a derivation that {@link FirstOrderAutomate} found as steps of the proof, with the rules of first-order logic
 * only. A formula already on a valid line in scope is cited instead of being derived again; a subproof's lines go out
 * of scope when it closes.
 */
final class Emitter {

    private final FirstOrderNaturalDeduction proof;
    /** For each open subproof (the first one is the premises' level), the 1-based line of each formula derived in it. */
    private final Deque<Map<FirstOrderOperation, Integer>> scopes = new ArrayDeque<>();

    Emitter(FirstOrderNaturalDeduction proof) {
        this.proof = proof;
        Map<FirstOrderOperation, Integer> premises = new HashMap<>();
        for (int line = 1; line <= proof.getSteps().size(); line++) {
            premises.putIfAbsent(proof.getSteps().get(line - 1).getStep(), line);
        }
        scopes.push(premises);
    }

    /**
     * Writes the derivation and makes its formula the last line.
     *
     * @throws IllegalStateException if a rule does not accept a step of the derivation
     */
    void write(Node root) {
        last(emit(root));
    }

    private Integer lookup(FirstOrderOperation formula) {
        for (Map<FirstOrderOperation, Integer> scope : scopes) {
            Integer line = scope.get(formula);
            if (line != null) {
                return line;
            }
        }
        return null;
    }

    private int emit(Node node) {
        Integer known = lookup(node.formula());
        if (known != null) {
            return known;
        }
        switch (node.kind()) {
            case PREMISE, ASSUMPTION -> throw new IllegalStateException(node.formula() + " is not in scope");
            case AND_I -> apply(new FirstOrderAndI(emit(premise(node, 0)), emit(premise(node, 1))));
            case AND_E1 -> apply(new FirstOrderAndE1(emit(premise(node, 0))));
            case AND_E2 -> apply(new FirstOrderAndE2(emit(premise(node, 0))));
            case OR_I1 -> apply(new FirstOrderOrI1(emit(premise(node, 0)), ((DisjunctionFirstOrder) node.formula()).getRight()));
            case OR_I2 -> apply(new FirstOrderOrI2(emit(premise(node, 0)), ((DisjunctionFirstOrder) node.formula()).getLeft()));
            case OR_E -> {
                int disjunction = emit(premise(node, 0));
                int left = emit(premise(node, 1));
                int right = emit(premise(node, 2));
                apply(new FirstOrderOrE(disjunction, left, right));
            }
            case IMP_I -> subproof(node, premise(node, 0), new FirstOrderDeductionTheorem());
            case IMP_E -> {
                int implication = emit(premise(node, 0));
                apply(new FirstOrderModusPonens(implication, emit(premise(node, 1))));
            }
            case NOT_I -> subproof(node, premise(node, 0), new FirstOrderNotI());
            case NOT_E -> apply(new FirstOrderNotE(emit(premise(node, 0))));
            case FALSE_I -> {
                int positive = emit(premise(node, 0));
                apply(new FirstOrderFI(positive, emit(premise(node, 1))));
            }
            case FALSE_E -> apply(new FirstOrderFE(emit(premise(node, 0)), node.formula()));
            case FORALL_I -> apply(new FirstOrderForallI(emit(premise(node, 0)), (Forall) node.formula()));
            case FORALL_E -> apply(new FirstOrderForallE(emit(premise(node, 0)), node.term()));
            case EXISTS_I -> apply(new FirstOrderExistsI(emit(premise(node, 0)), (Exists) node.formula()));
            case EXISTS_E -> {
                int exists = emit(premise(node, 0));
                subproof(node, premise(node, 1), new FirstOrderExistsE(exists));
            }
            case EQ_I -> apply(new FirstOrderEqualsI(node.term()));
            case EQ_E -> {
                int equation = emit(premise(node, 0));
                apply(new FirstOrderEqualsE(equation, emit(premise(node, 1)), node.formula()));
            }
        }
        int line = proof.getSteps().size();
        if (!proof.getSteps().getLast().getStep().equals(node.formula())) {
            throw new IllegalStateException("The step " + line + " is not " + node.formula());
        }
        scopes.peek().put(node.formula(), line);
        return line;
    }

    private static Node premise(Node node, int index) {
        return node.premises().get(index);
    }

    /**
     * Opens a subproof with the node's assumption, reaches {@code body} as its last line and closes it with
     * {@code close}.
     */
    private void subproof(Node node, Node body, FirstOrderAction close) {
        apply(new FirstOrderAssume(node.assumption()));
        Map<FirstOrderOperation, Integer> scope = new HashMap<>();
        scope.put(node.assumption(), proof.getSteps().size());
        scopes.push(scope);
        last(emit(body));
        scopes.pop();
        apply(close);
    }

    /**
     * Makes line {@code line} the last one, copying it when it is not.
     */
    private void last(int line) {
        if (line != proof.getSteps().size()) {
            apply(new FirstOrderCopy(line));
        }
    }

    private void apply(FirstOrderAction action) {
        if (!action.isValid(proof)) {
            throw new IllegalStateException(action.getClass().getSimpleName() + " does not apply to\n" + proof);
        }
        action.apply(proof);
    }
}
