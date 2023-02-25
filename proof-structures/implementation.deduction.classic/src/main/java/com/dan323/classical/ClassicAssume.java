package com.dan323.classical;

import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.Assume;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.Objects;

public final class ClassicAssume extends Assume<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicAssume(ClassicalLogicOperation clo) {
        super(clo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), log);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassicAssume ass) {
            return Objects.equals(log, ass.log);
        } else {
            return false;
        }
    }
}
