package com.dan323.proof.generic;

import com.dan323.expresions.base.*;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author danco
 */
@ExtendWith(MockitoExtension.class)
public class RuleOrTest {

    @Mock
    public ProofTest.ProofStub pf;

    @Mock
    public List<ProofStep<LogicOperation>> list;

    @Mock
    public ProofStep<LogicOperation> pStep0;

    @Mock
    public ProofStep<LogicOperation> pStep1;

    @Mock
    public ProofReason proof;

    protected static <T extends BinaryOperation<LogicOperation>> T mockOperation(Class<T> operationType, LogicOperation l1, LogicOperation l2) {
        T operation = mock(operationType, Answers.CALLS_REAL_METHODS);
        lenient().doReturn(l1).when(operation).getLeft();
        lenient().doReturn(l2).when(operation).getRight();
        return operation;
    }

    protected static Disjunction<LogicOperation> mockOr(LogicOperation l1, LogicOperation l2) {
        return mockOperation(Disjunction.class, l1, l2);
    }

    protected static Implication<LogicOperation> mockImplication(LogicOperation l1, LogicOperation l2) {
        return mockOperation(Implication.class, l1, l2);
    }

    @Test
    public void basicTest() {
        OrEStub orE = new RuleOrTest.OrEStub(1, 2, 3);
        assertEquals(1, orE.getDisj());
        assertEquals(2, orE.get1());
        assertEquals(3, orE.get2());
        assertNotEquals(orE, new Object());
        assertEquals(orE, orE);

        OrIStub orI = new RuleOrTest.OrIStub(1, mock(LogicOperation.class));
        assertEquals(1,orI.getAt());
        assertNotEquals(orI,new Object());
        assertEquals(orI, orI);
    }

    @Test
    public void orEIsValidTest() {
        OrEStub orE = new RuleOrTest.OrEStub(1, 2, 2);

        assertEquals(new RuleOrTest.OrEStub(1, 2, 2), orE);
        Assertions.assertNotEquals(new RuleOrTest.OrEStub(1, 3, 2), orE);
        assertEquals(new RuleOrTest.OrEStub(1, 2, 2).hashCode(), orE.hashCode());

        doReturn(list).when(pf).getSteps();
        doReturn(1).when(list).size();
        doReturn(pStep0).when(list).get(eq(0));
        doReturn(false).when(pStep0).isValid();

        Assertions.assertFalse(orE.isValid(pf));

        OrEStub orE2 = new RuleOrTest.OrEStub(1, 2, 3);

        Variable<LogicOperation> variable = mock(Variable.class);
        doReturn(3).when(list).size();
        Disjunction<LogicOperation> disjunction = mockOr(variable, variable);
        Implication<LogicOperation> implication1 = mockImplication(variable, mock(LogicOperation.class));
        Implication<LogicOperation> implication2 = mockImplication(variable, mock(LogicOperation.class));
        ProofStep<LogicOperation> pStep2 = mock(ProofStep.class);
        doReturn(pStep2).when(list).get(eq(2));
        doReturn(pStep1).when(list).get(eq(1));
        doReturn(true).when(pStep0).isValid();
        doReturn(true).when(pStep1).isValid();
        doReturn(true).when(pStep2).isValid();
        doReturn(disjunction).when(pStep0).getStep();
        doReturn(implication1).when(pStep1).getStep();
        doReturn(implication2).when(pStep2).getStep();

        Assertions.assertFalse(orE2.isValid(pf));

        doReturn(2).when(list).size();

        Assertions.assertTrue(orE.isValid(pf));
    }

    @Test
    public void orIIsValidTest() {
        LogicOperation logicOperation = mock(LogicOperation.class);
        OrIStub orI = new RuleOrTest.OrIStub(2, logicOperation);

        assertEquals(new RuleOrTest.OrIStub(2, logicOperation), orI);
        assertEquals(new RuleOrTest.OrIStub(2, logicOperation).hashCode(), orI.hashCode());

        doReturn(list).when(pf).getSteps();

        Assertions.assertFalse(orI.isValid(pf));

        doReturn(1).when(list).size();

        Assertions.assertFalse(orI.isValid(pf));

        doReturn(2).when(list).size();
        doReturn(pStep1).when(list).get(eq(1));
        doReturn(false).when(pStep1).isValid();

        Assertions.assertFalse(orI.isValid(pf));

        doReturn(2).when(list).size();
        doReturn(pStep1).when(list).get(eq(1));
        doReturn(true).when(pStep1).isValid();

        Assertions.assertTrue(orI.isValid(pf));
    }

    @Test
    public void orEApplyTest() {
        List<ProofStep<LogicOperation>> record = new ArrayList<>();
        doReturn(list).when(pf).getSteps();
        Variable<LogicOperation> variable = mock(Variable.class);
        doReturn(2).when(list).size();
        Implication<LogicOperation> implication = mockImplication(variable, variable);
        doReturn("P").when(variable).toString();
        doReturn(pStep1).when(list).get(eq(1));
        doReturn(1).when(pStep1).getAssumptionLevel();
        doReturn(implication).when(pStep1).getStep();
        doAnswer(invocationOnMock -> record.add(invocationOnMock.getArgument(0))).when(list).add(any(ProofStep.class));

        OrEStub orE = new RuleOrTest.OrEStub(1, 2, 2);
        orE.applyStepSupplier(pf, ProofStep::new);

        assertEquals(new ProofReason("|E", List.of(1, 2, 2)), record.get(0).getProof());
        assertEquals(1, record.get(0).getAssumptionLevel());
        assertEquals("P", record.get(0).getStep().toString());
        Assertions.assertTrue(record.get(0).isValid());
    }

    @Test
    public void orIApplyTest() {
        List<ProofStep<LogicOperation>> record = new ArrayList<>();
        doReturn(list).when(pf).getSteps();
        doReturn(1).when(list).size();
        doReturn(pStep0).when(list).get(eq(0));
        doReturn(1).when(pStep0).getAssumptionLevel();
        Variable<LogicOperation> variable = mock(Variable.class, Answers.CALLS_REAL_METHODS);
        doReturn("P").when(variable).toString();
        doReturn(variable).when(pStep0).getStep();
        doAnswer(invocationOnMock -> record.add(invocationOnMock.getArgument(0))).when(list).add(any(ProofStep.class));

        OrIStub orI = new RuleOrTest.OrIStub(1, variable);
        orI.applyStepSupplier(pf, ProofStep::new);

        assertEquals(new ProofReason("|I", List.of(1)), record.get(0).getProof());
        assertEquals(1, record.get(0).getAssumptionLevel());
        assertEquals("P | P", record.get(0).getStep().toString());
        Assertions.assertTrue(record.get(0).isValid());
    }

    public static class OrIStub extends OrI<LogicOperation, ProofStep<LogicOperation>, ProofTest.ProofStub> {
        public OrIStub(int app, LogicOperation lo) {
            super(app, lo, RuleOrTest::mockOr);
        }

    }

    public static class OrEStub extends OrE<LogicOperation, ProofStep<LogicOperation>, ProofTest.ProofStub> {
        public OrEStub(int dis, int r1, int r2) {
            super(dis, r1, r2);
        }
    }
}
