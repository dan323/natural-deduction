package com.dan323.uses.classical.mock;

import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.ImplicationClassic;
import com.dan323.expressions.classical.VariableClassic;

import java.util.List;

public class ClassicalProof {

    public static NaturalDeduction naturalDeductionNoAssms() {
        NaturalDeduction naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new ImplicationClassic(new VariableClassic("P"), new VariableClassic("P")));
        return naturalDeduction;
    }

    public static NaturalDeduction naturalDeductionWithAssms() {
        NaturalDeduction naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(new VariableClassic("P")), new VariableClassic("P"));
        return naturalDeduction;
    }

    public static NaturalDeduction naturalDeductionNotProvable() {
        NaturalDeduction naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(new VariableClassic("P")), new VariableClassic("Q"));
        return naturalDeduction;
    }
}
