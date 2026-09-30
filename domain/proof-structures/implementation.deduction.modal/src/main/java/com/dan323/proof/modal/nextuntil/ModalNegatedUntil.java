package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ConjunctionModal;
import com.dan323.expressions.modal.DisjunctionModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.expressions.modal.Until;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.List;
import java.util.Optional;

/**
 * Negated Until, {@code -U [i]}: from {@code -(A U B)} in state {@code s} (line {@code i}), derive
 * {@code ([] (- B)) | ((- B) U ((- A) & (- B)))} in state {@code s}: either {@code B} never holds, or {@code A} fails
 * before {@code B} ever holds.
 */
public final class ModalNegatedUntil extends NextUntilRule {

    public ModalNegatedUntil(int line) {
        super(ParseModalNextUntilAction.NEGATED_UNTIL, List.of(line));
    }

    @Override
    Optional<Conclusion> conclusion(ModalNaturalDeduction pf) {
        return formula(pf, line(0), NegationModal.class)
                .filter(negation -> negation.getElement() instanceof Until)
                .map(negation -> new Conclusion(expansion((Until) negation.getElement()), state(pf, line(0))));
    }

    /** @return {@code ([] (- B)) | ((- B) U ((- A) & (- B)))} for {@code A U B} */
    public static DisjunctionModal expansion(Until until) {
        var notA = new NegationModal((ModalLogicalOperation) until.getLeft());
        var notB = new NegationModal((ModalLogicalOperation) until.getRight());
        return new DisjunctionModal(new Always(notB), new Until(notB, new ConjunctionModal(notA, notB)));
    }
}
