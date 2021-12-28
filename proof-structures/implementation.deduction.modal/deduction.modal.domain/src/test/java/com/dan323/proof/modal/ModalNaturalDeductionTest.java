package com.dan323.proof.modal;

import com.dan323.expressions.modal.VariableModal;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModalNaturalDeductionTest {

    @Test
    public void getGoalTest() {
        var mnd = new ModalNaturalDeduction<>("s0");
        var p = new VariableModal("P");
        mnd.initializeProof(List.of(), p);
        assertEquals(p, mnd.getGoal());
    }

    @Test
    public void getStateTest(){
        var mnd = new ModalNaturalDeduction<>("s0");
        assertEquals("s0", mnd.getState0());
    }
}
