package com.dan323.proof.modal;

import com.dan323.expressions.modal.VariableModal;
import com.dan323.proof.modal.proof.LabeledExpression;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModalNaturalDeductionTest {

    @Test
    public void getGoalTest() {
        var mnd = new ModalNaturalDeduction<String>();
        var p = new VariableModal("P");
        var labp = new LabeledExpression<>("s0", p);
        mnd.initializeProof(List.of(), labp);
        assertEquals(labp, mnd.getGoal());
    }
}
