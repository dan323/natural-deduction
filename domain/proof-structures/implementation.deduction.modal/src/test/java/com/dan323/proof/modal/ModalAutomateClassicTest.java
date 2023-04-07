package com.dan323.proof.modal;

import com.dan323.expressions.modal.*;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModalAutomateClassicTest {

    @Test
    public void automatePimpP() {
        var p = new VariableModal("P");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new ImplicationModal(p, p));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateNotNotPimpP() {
        var p = new VariableModal("P");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new ImplicationModal(new NegationModal(new NegationModal(p)), p));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automatePimpNotNotP() {
        var p = new VariableModal("P");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new ImplicationModal(p, new NegationModal(new NegationModal(p))));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateExcludedMiddle() {
        var p = new VariableModal("P");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new DisjunctionModal(p, new NegationModal(p)));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void setAssmsTest() {
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(new ImplicationModal(p, q), p), q);
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateBasicFact() {
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new ImplicationModal(p, new ImplicationModal(q, p)));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateFalseImpAnything() {
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(p, new NegationModal(p)), q);
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateAND() {
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(p, q), new ConjunctionModal(p, q));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateORE() {
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        var r = new VariableModal("R");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(new DisjunctionModal(p, q), new ImplicationModal(p,r), new ImplicationModal(q,r)), r);
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateImpossible() {
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(p), new ConjunctionModal(p, q));
        naturalDeduction.automate();
        assertFalse(naturalDeduction.isDone());
    }
    
}
