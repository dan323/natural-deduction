package com.dan323.proof.modal.nextuntil;

import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.Optional;
import java.util.SortedSet;
import java.util.function.Function;

/**
 * The lines a rule found it may use, kept from {@code isValid} for the {@code apply} that follows it: finding them
 * reads every relation of the proof and searches for paths ({@link StateOrder}), so it is done once for both. It is
 * only reused for the same proof with as many lines as when it was found, and only once.
 */
final class LastSupport {

    private ModalNaturalDeduction proof;
    private int size;
    private Optional<SortedSet<Integer>> support;

    /** @return the support on {@code pf}, computed now, which the next {@link #take} on the same proof reuses */
    Optional<SortedSet<Integer>> find(ModalNaturalDeduction pf, Function<ModalNaturalDeduction, Optional<SortedSet<Integer>>> compute) {
        support = compute.apply(pf);
        proof = pf;
        size = pf.getSteps().size();
        return support;
    }

    /** @return the support found by the last {@link #find} if it was on this proof as it is now, else computed now */
    Optional<SortedSet<Integer>> take(ModalNaturalDeduction pf, Function<ModalNaturalDeduction, Optional<SortedSet<Integer>>> compute) {
        var kept = proof == pf && size == pf.getSteps().size() ? support : compute.apply(pf);
        proof = null;
        support = null;
        return kept;
    }
}
