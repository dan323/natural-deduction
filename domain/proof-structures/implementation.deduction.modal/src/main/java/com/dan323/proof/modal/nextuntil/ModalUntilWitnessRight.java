package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.List;
import java.util.Optional;

/**
 * Until witness reaches {@code B}, {@code UB [i]}: from the {@code UW} step {@code s <= t} (line {@code i}) of
 * {@code A U B}, derive {@code B} in {@code t}.
 */
public final class ModalUntilWitnessRight extends NextUntilRule {

    public ModalUntilWitnessRight(int line) {
        super(ParseModalNextUntilAction.UNTIL_WITNESS_RIGHT, List.of(line));
    }

    @Override
    Optional<Conclusion> conclusion(ModalNaturalDeduction pf) {
        return ModalUntilWitness.witness(pf, line(0))
                .map(witness -> new Conclusion((ModalLogicalOperation) witness.until().getRight(), witness.state()));
    }
}
