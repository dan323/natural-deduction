package com.dan323.uses.classical.test;

import com.dan323.uses.classical.ClassicGetActions;
import com.dan323.uses.classical.ClassicSolver;
import com.dan323.uses.classical.mock.ClassicalProof;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClassicalUseTest {

    @Test
    public void classicSolver() {
        ClassicSolver solver = new ClassicSolver();
        var proof = solver.perform(ClassicalProof.naturalDeductionNoAssms());
        assertTrue(proof.isDone());
        proof = solver.perform(ClassicalProof.naturalDeductionWithAssms());
        assertTrue(proof.isDone());
        proof = solver.perform(ClassicalProof.naturalDeductionNotProvable());
        assertFalse(proof.isDone());
    }

    @Test
    public void classicActions() {
        ClassicGetActions actions = new ClassicGetActions();
        assertEquals(14, actions.perform().size());
    }
}
