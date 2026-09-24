package com.dan323.proof.modal.nextuntil;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.Next;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.List;
import java.util.Optional;

/**
 * Induction, {@code Ind [i, j]}: from {@code A} (line {@code i}) and {@code [] (A -> X A)} (line {@code j}) in the
 * same state {@code s}, derive {@code [] A} in state {@code s}. Every state after {@code s} is {@code s+k} for some
 * {@code k}, so {@code A} holds in all of them.
 */
public final class ModalInduction extends NextUntilRule {

    private final int base;
    private final int step;

    public ModalInduction(int base, int step) {
        super(ParseModalNextUntilAction.INDUCTION, List.of(base, step));
        this.base = base;
        this.step = step;
    }

    @Override
    Optional<Conclusion> conclusion(ModalNaturalDeduction pf) {
        var formula = formula(pf, base);
        var always = formula(pf, step, Always.class);
        if (formula.isEmpty() || always.isEmpty() || !state(pf, base).equals(state(pf, step))
                || !always.get().getElement().equals(new ImplicationModal(formula.get(), new Next(formula.get())))) {
            return Optional.empty();
        }
        return Optional.of(new Conclusion(new Always(formula.get()), state(pf, base)));
    }
}
