package com.dan323.proof.classic;

import com.dan323.expresions.classical.DisjunctionClassic;
import com.dan323.expresions.classical.ImplicationClassic;
import com.dan323.expresions.classical.NegationClassic;
import com.dan323.expresions.classical.VariableClassic;
import com.dan323.proof.classical.proof.NaturalDeduction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NaturalDeductionTest {

    @Test
    public void automatePimpP() {
        var p = new VariableClassic("P");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new ImplicationClassic(p, p));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateNotNotPimpP() {
        var p = new VariableClassic("P");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new ImplicationClassic(new NegationClassic(new NegationClassic(p)), p));
        naturalDeduction.automate();
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
        assertTrue(naturalDeduction.isDone());
    }
}
