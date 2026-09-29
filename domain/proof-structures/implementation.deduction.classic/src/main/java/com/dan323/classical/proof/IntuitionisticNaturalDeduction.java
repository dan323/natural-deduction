package com.dan323.classical.proof;

import com.dan323.classical.internal.IntuitionisticAutomate;

/**
 * A proof of intuitionistic logic: a classical proof whose automatic solver never uses double negation elimination
 * ({@code -E}). It only differs from {@link NaturalDeduction} in {@link #automate()}, which runs
 * {@link IntuitionisticAutomate}: the classical goal-directed solver without {@code -E} and without proof by
 * contradiction, which tries case splits, {@code ->E} backwards and ex falso instead. It may not find a proof of a goal that follows from the premises intuitionistically; when it finds
 * none it leaves the proof with its premises.
 */
public final class IntuitionisticNaturalDeduction extends NaturalDeduction {

    @Override
    public void automate() {
        new IntuitionisticAutomate().automate(this);
    }
}
