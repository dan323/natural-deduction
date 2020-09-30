package com.dan323.classical;

import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.proof.generic.Copy;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.Objects;

public final class ClassicCopy extends Copy<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> implements ClassicalAction {

    public ClassicCopy(int i) {
        super(i);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAppliedAt(), getClass());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassicCopy){
            ClassicCopy classicCopy = (ClassicCopy)obj;
            return getAppliedAt() == classicCopy.getAppliedAt();
        } else {
            return false;
        }
    }
}
