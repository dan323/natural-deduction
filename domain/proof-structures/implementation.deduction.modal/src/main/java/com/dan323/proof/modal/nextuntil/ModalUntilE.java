package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.ConjunctionModal;
import com.dan323.expressions.modal.DisjunctionModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.Next;
import com.dan323.expressions.modal.Until;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.List;
import java.util.Optional;

/**
 * Until elimination, {@code UE [i]}: from {@code A U B} in state {@code s}, derive its expansion
 * {@code B | (A & X (A U B))} in state {@code s}. Or elimination on it then reasons by cases: {@code B} holds now, or
 * {@code A} holds now and {@code A U B} holds in {@code s+1}.
 */
public final class ModalUntilE extends NextUntilRule {

    public ModalUntilE(int line) {
        super(ParseModalNextUntilAction.UNTIL_E, List.of(line));
    }

    @Override
    Optional<Conclusion> conclusion(ModalNaturalDeduction pf) {
        return formula(pf, line(0), Until.class).map(until -> new Conclusion(expansion(until), state(pf, line(0))));
    }

    /** @return {@code B | (A & X (A U B))} for {@code A U B} */
    public static DisjunctionModal expansion(Until until) {
        return new DisjunctionModal((ModalLogicalOperation) until.getRight(),
                new ConjunctionModal((ModalLogicalOperation) until.getLeft(), new Next(until)));
    }
}
