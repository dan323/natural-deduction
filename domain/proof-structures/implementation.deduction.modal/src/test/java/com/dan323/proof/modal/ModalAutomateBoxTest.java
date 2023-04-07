package com.dan323.proof.modal;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModalAutomateBoxTest {

    @Test
    public void automateAlwaysPimpP() {
        var p = new VariableModal("P");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new Always(new ImplicationModal(p, p)));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateReflexionFrame() {
        var p = new VariableModal("P");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(new Always(p)), p);
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }

    @Test
    public void automateAlwaysImplication() {
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(new Always(new ImplicationModal(p, q))), new ImplicationModal(new Always(p), new Always(q)));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }


    @Test
    public void automateTransitiveFrame() {
        var p = new VariableModal("P");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(new Always(p)), new Always(new Always(p)));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }
}
