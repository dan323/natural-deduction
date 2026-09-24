package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.Next;
import com.dan323.expressions.relation.StateTerm;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.List;
import java.util.Optional;

/**
 * Next elimination, {@code XE [i]}: from {@code X A} in state {@code s}, derive {@code A} in state {@code s+1}.
 */
public final class ModalNextE extends NextUntilRule {

    public ModalNextE(int line) {
        super(ParseModalNextUntilAction.NEXT_E, List.of(line));
    }

    @Override
    Optional<Conclusion> conclusion(ModalNaturalDeduction pf) {
        // s0+2147483647 is a state, but its successor cannot be written.
        var term = term(pf, line(0)).filter(StateTerm::hasSuccessor);
        var next = formula(pf, line(0), Next.class);
        if (term.isEmpty() || next.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Conclusion(next.get().getElement(), term.get().successor().toString()));
    }
}
