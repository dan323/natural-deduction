package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.Next;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.List;
import java.util.Optional;

/**
 * Next introduction, {@code XI [i]}: from {@code A} in state {@code s+1}, derive {@code X A} in state {@code s}. The
 * state of line {@code i} must be written as a successor ({@code s0+1}, not {@code s1}).
 */
public final class ModalNextI extends NextUntilRule {

    private final int line;

    public ModalNextI(int line) {
        super(ParseModalNextUntilAction.NEXT_I, List.of(line));
        this.line = line;
    }

    @Override
    Optional<Conclusion> conclusion(ModalNaturalDeduction pf) {
        var term = term(pf, line).filter(t -> !t.isBase());
        var formula = formula(pf, line);
        if (term.isEmpty() || formula.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Conclusion(new Next(formula.get()), term.get().predecessor().toString()));
    }
}
