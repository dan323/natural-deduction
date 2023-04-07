package com.dan323.uses.classical.test;

import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.classical.ClassicalConfiguration;
import com.dan323.uses.classical.mock.ClassicalProof;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClassicalUseTest {

    @Test
    public void classicSolver() {
        var solver = (new ClassicalConfiguration()).classicalSolver();
        var proof = solver.perform(ClassicalProof.naturalDeductionNoAssms());
        assertTrue(proof.isDone());
        proof = solver.perform(ClassicalProof.naturalDeductionWithAssms());
        assertTrue(proof.isDone());
        proof = solver.perform(ClassicalProof.naturalDeductionNotProvable());
        assertFalse(proof.isDone());
    }

    @Test
    public void classicActions() {
        LogicalGetActions actions = (new ClassicalConfiguration()).classicalActions();
        assertEquals(14, actions.perform().size());
    }
}
