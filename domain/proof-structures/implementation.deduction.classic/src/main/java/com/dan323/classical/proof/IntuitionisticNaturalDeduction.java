package com.dan323.classical.proof;

import com.dan323.classical.internal.IntuitionisticAutomate;

/**
 * A proof of intuitionistic logic: a classical proof whose automatic solver never uses double negation elimination
 * ({@code -E}). It only differs from {@link NaturalDeduction} in {@link #automate()}, which runs
 * {@link IntuitionisticAutomate}: a complete search that finishes the proof exactly when its goal follows from its
 * premises intuitionistically, and otherwise leaves the proof with its premises.
 */
public final class IntuitionisticNaturalDeduction extends NaturalDeduction {

    @Override
    public void automate() {
        new IntuitionisticAutomate().automate(this);
    }
}
