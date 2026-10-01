package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The relations a rule of {@code modal-next-until} may reason with: the ones on the lines it cites, or, when it cites
 * none yet (a client or the solver applies it), every valid relation of the proof.
 */
final class Relations {

    private Relations() {
    }

    /**
     * @param lines the 1-based lines the rule cites, or null for every valid relation
     * @return the relations by line, empty when a cited line is not a valid relation
     */
    static Optional<StateOrder> order(ModalNaturalDeduction pf, List<Integer> lines) {
        Map<Integer, RelationOperation> relations = new LinkedHashMap<>();
        if (lines == null) {
            for (int i = 0; i < pf.getSteps().size(); i++) {
                var step = pf.getSteps().get(i);
                if (step.isValid() && step.getStep() instanceof RelationOperation relation) {
                    relations.put(i + 1, relation);
                }
            }
        } else {
            for (int line : lines) {
                if (!RuleUtils.isValidIndexAndProp(pf, line) || !(pf.getSteps().get(line - 1).getStep() instanceof RelationOperation relation)) {
                    return Optional.empty();
                }
                relations.put(line, relation);
            }
        }
        return Optional.of(new StateOrder(relations));
    }
}
