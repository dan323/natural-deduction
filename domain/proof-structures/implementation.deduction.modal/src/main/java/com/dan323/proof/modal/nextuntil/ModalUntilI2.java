package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.Next;
import com.dan323.expressions.modal.Until;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.List;
import java.util.Optional;

/**
 * Until introduction (later), {@code UI [i, j]}: from {@code A} (line {@code i}) and {@code X (A U B)} (line
 * {@code j}) in the same state, derive {@code A U B} in that state.
 */
public final class ModalUntilI2 extends NextUntilRule {

    public ModalUntilI2(int now, int next) {
        super(ParseModalNextUntilAction.UNTIL, List.of(now, next));
    }

    @Override
    Optional<Conclusion> conclusion(ModalNaturalDeduction pf) {
        int now = line(0);
        int next = line(1);
        var left = formula(pf, now);
        var until = formula(pf, next, Next.class)
                .map(Next::getElement)
                .filter(Until.class::isInstance)
                .map(Until.class::cast);
        if (left.isEmpty() || until.isEmpty() || !until.get().getLeft().equals(left.get())
                || !state(pf, now).equals(state(pf, next))) {
            return Optional.empty();
        }
        return Optional.of(new Conclusion(until.get(), state(pf, now)));
    }
}
