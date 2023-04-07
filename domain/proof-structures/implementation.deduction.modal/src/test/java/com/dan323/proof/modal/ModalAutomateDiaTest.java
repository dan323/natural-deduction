package com.dan323.proof.modal;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModalAutomateDiaTest {

    @Test
    public void alwaysImpSometime() {
        var p = new VariableModal("P");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(new Always(p)), new Sometime(p));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }


    @Test
    public void sometimesAlwImpAlwSometime() {
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        var naturalDeduction = new ModalNaturalDeduction();
        naturalDeduction.initializeProof(List.of(new Sometime(p), new Always(new ImplicationModal(p,q))), new Sometime(q));
        naturalDeduction.automate();
        assertTrue(naturalDeduction.isDone());
    }
}
