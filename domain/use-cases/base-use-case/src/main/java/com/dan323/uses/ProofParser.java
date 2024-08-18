package com.dan323.uses;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.List;

public interface ProofParser<P extends Proof<T, Q>, T extends LogicOperation, Q extends ProofStep<T>, A extends Action<T, Q, P>> {

    default P parseProof(String proof) {
        P nd = getNewProof();
        var actions = proof.lines().map(this::parseLine).toList();
        var assms = ProofParser.extractAssumptions(actions);
        var actualProof = ProofParser.skipInitAssumptions(actions);
        if (actions.getLast().getAssumptionLevel() == 0) {
            nd.initializeProof(assms, actions.getLast().getStep());
            nd.getSteps().addAll(actualProof);
            var parsedActions = (List<A>)nd.parse();
            nd.reset();
            int i = 0;
            for (A action : parsedActions) {
                if (i < nd.getAssms().size()) {
                    i++;
                } else if (action.isValid(nd)) {
                    action.apply(nd);
                } else {
                    throw new IllegalArgumentException("The proof is invalid!!");
                }
            }
            return nd;
        } else {
            throw new IllegalArgumentException("The proof is invalid!!");
        }
    }

    String logic();

    P getNewProof();

    Q parseLine(String line);

    private static <T extends LogicOperation, Q extends ProofStep<T>> List<T> extractAssumptions(List<Q> steps) {
        return steps.stream()
                .takeWhile(action -> action.getAssumptionLevel() == 0 && action.getProof().getNameProof().contains("Ass"))
                .map(ProofStep::getStep)
                .toList();
    }

    private static <T extends LogicOperation, Q extends ProofStep<T>> List<Q> skipInitAssumptions(List<Q> steps) {
        return steps.stream()
                .dropWhile(action -> action.getAssumptionLevel() == 0 && action.getProof().getNameProof().contains("Ass"))
                .toList();
    }
}
