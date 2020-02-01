package com.dan323.proof.generic.proof;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.Action;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ProofTest {

    @Test
    public void setterGetterTest() {
        Proof<LogicOperation, ProofStep<LogicOperation>> proof = new ProofStub();
        List<LogicOperation> assms = List.of(mock(LogicOperation.class));
        LogicOperation goal = mock(LogicOperation.class);
        proof.initializeProof(assms, goal);

        assertEquals(assms, proof.getAssms());
        assertEquals(goal, proof.getGoal());
    }

    @Test
    public void removeStepsTest() {
        Proof<LogicOperation, ProofStep<LogicOperation>> proof = new ProofStub();

        ProofStep<LogicOperation> p1 = mock(ProofStep.class);
        ProofStep<LogicOperation> p2 = mock(ProofStep.class);
        ProofStep<LogicOperation> p3 = mock(ProofStep.class);
        proof.getSteps().add(p1);
        proof.getSteps().add(p2);
        proof.getSteps().add(p3);

        assertEquals(3, proof.getSteps().size());
        proof.removeLastStep();
        assertEquals(2, proof.getSteps().size());
        assertTrue(proof.getSteps().contains(p1));
        assertTrue(proof.getSteps().contains(p2));
        assertFalse(proof.getSteps().contains(p3));
        proof.initializeProofSteps();
        assertEquals(0, proof.getSteps().size());
    }

    @Test
    public void isDoneTest() {
        Proof<LogicOperation, ProofStep<LogicOperation>> pr = new ProofStub();

        assertFalse(pr.isDone());

        List<LogicOperation> assms = List.of(mock(LogicOperation.class));
        LogicOperation goal = mock(LogicOperation.class);
        pr.initializeProof(assms, goal);
        pr.getSteps().add(new ProofStep<>(1, mock(LogicOperation.class), mock(ProofReason.class)));

        assertFalse(pr.isDone());

        ProofStep<LogicOperation> proofStep = mock(ProofStep.class);
        doReturn(false).when(proofStep).isValid();
        pr.getSteps().add(proofStep);

        assertFalse(pr.isDone());

        pr.getSteps().add(new ProofStep<>(1, goal, mock(ProofReason.class)));

        assertFalse(pr.isDone());

        pr.getSteps().add(new ProofStep<>(0, goal, mock(ProofReason.class)));

        assertTrue(pr.isDone());
    }

    public static class ProofStub extends Proof<LogicOperation, ProofStep<LogicOperation>> {

        @Override
        public boolean isValid(Action<LogicOperation, ProofStep<LogicOperation>> act) {
            return false;
        }

        @Override
        public void initializeProof(List<LogicOperation> assms, LogicOperation goal) {
            setAssms(assms);
            setGoal(goal);
        }
    }

}
