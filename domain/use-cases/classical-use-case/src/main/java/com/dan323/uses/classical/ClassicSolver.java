package com.dan323.uses.classical;

import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.LogicalSolver;

public class ClassicSolver implements LogicalSolver<ClassicalLogicOperation, ProofStep<ClassicalLogicOperation>, NaturalDeduction> {

    ClassicSolver(){
    }

    @Override
    public NaturalDeduction perform(NaturalDeduction naturalDeduction) {
        naturalDeduction.automate();
        return naturalDeduction;
    }

    @Override
    public String getLogicName() {
        return "classical";
    }
}
