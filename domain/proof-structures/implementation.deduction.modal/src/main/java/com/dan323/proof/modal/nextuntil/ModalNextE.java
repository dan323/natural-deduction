package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.Next;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.List;
import java.util.Optional;

/**
 * Next elimination, {@code XE [i]}: from {@code X A} in state {@code s}, derive {@code A} in state {@code s+1}.
 */
public final class ModalNextE extends NextUntilRule {

    private final int line;

    public ModalNextE(int line) {
        super(ParseModalNextUntilAction.NEXT_E, List.of(line));
        this.line = line;
    }

    @Override
    Optional<Conclusion> conclusion(ModalNaturalDeduction pf) {
        var term = term(pf, line);
        var next = formula(pf, line, Next.class);
        if (term.isEmpty() || next.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Conclusion(next.get().getElement(), term.get().successor().toString()));
    }
}
