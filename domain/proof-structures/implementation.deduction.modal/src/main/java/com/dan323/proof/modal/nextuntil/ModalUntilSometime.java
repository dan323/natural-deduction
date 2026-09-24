package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.modal.Until;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.List;
import java.util.Optional;

/**
 * Until reaches its goal, {@code U<> [i]}: from {@code A U B} in state {@code s}, derive {@code <> B} in state
 * {@code s}. The expansion alone ({@code UE}) would also hold if {@code A} held forever and {@code B} never did.
 */
public final class ModalUntilSometime extends NextUntilRule {

    public ModalUntilSometime(int line) {
        super(ParseModalNextUntilAction.UNTIL_SOMETIME, List.of(line));
    }

    @Override
    Optional<Conclusion> conclusion(ModalNaturalDeduction pf) {
        return formula(pf, line(0), Until.class)
                .map(until -> new Conclusion(new Sometime((ModalLogicalOperation) until.getRight()), state(pf, line(0))));
    }
}
