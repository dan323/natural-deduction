package com.dan323.proof.classic;

import com.dan323.expressions.classical.*;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NaturalDeductionTest {

    @Test
    public void automatePimpP() {
        var p = new VariableClassic("P");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new ImplicationClassic(p, p));
        naturalDeduction.automate();
        System.out.println(naturalDeduction);
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateNotNotPimpP() {
        var p = new VariableClassic("P");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new ImplicationClassic(new NegationClassic(new NegationClassic(p)), p));
        naturalDeduction.automate();
        System.out.println(naturalDeduction);
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automatePimpNotNotP() {
        var p = new VariableClassic("P");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new ImplicationClassic(p, new NegationClassic(new NegationClassic(p))));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateExcludedMiddle() {
        var p = new VariableClassic("P");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new DisjunctionClassic(p, new NegationClassic(p)));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void setAssmsTest() {
        var p = new VariableClassic("P");
        var q = new VariableClassic("Q");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(new ImplicationClassic(p, q), p), q);
        naturalDeduction.automate();
        System.out.println(naturalDeduction);
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateBasicFact() {
        var p = new VariableClassic("P");
        var q = new VariableClassic("Q");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new ImplicationClassic(p, new ImplicationClassic(q, p)));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateFalseImpAnything() {
        var p = new VariableClassic("P");
        var q = new VariableClassic("Q");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(p, new NegationClassic(p)), q);
        naturalDeduction.automate();
        System.out.println(naturalDeduction);
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateAND() {
        var p = new VariableClassic("P");
        var q = new VariableClassic("Q");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(p, q), new ConjunctionClassic(p, q));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateImpossible() {
        var p = new VariableClassic("P");
        var q = new VariableClassic("Q");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(p), new ConjunctionClassic(p, q));
        naturalDeduction.automate();
        assertFalse(naturalDeduction.isDone());
    }
}
