package com.dan323.proof.generic;

import com.dan323.expressions.base.*;
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

import static org.mockito.Mockito.*;

/**
 * @author danco
 */
@ExtendWith(MockitoExtension.class)
public class RuleNotTest {

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

    protected static <T extends UnaryOperation<LogicOperation>> T mockOperation(Class<T> operationType, LogicOperation l1) {
        T operation = mock(operationType, Answers.CALLS_REAL_METHODS);
        lenient().doReturn(l1).when(operation).getElement();
        return operation;
    }

    protected static Negation<LogicOperation> mockNot(LogicOperation l1) {
        return mockOperation(Negation.class, l1);
    }

    @Test
    public void notIIsValidTest() {
        NotIStub notI = new NotIStub();

        Assertions.assertEquals(new NotIStub(), notI);
        Assertions.assertEquals(new NotIStub().hashCode(), notI.hashCode());

        doReturn(list).when(pf).getSteps();
        doReturn(true).when(list).isEmpty();

        Assertions.assertFalse(notI.isValid(pf));

        doReturn(2).when(list).size();
        doReturn(false).when(list).isEmpty();
        doReturn(pStep0).when(list).get(eq(0));
        doReturn(pStep1).when(list).get(eq(1));
        doReturn(0).when(pStep1).getAssumptionLevel();

        Assertions.assertFalse(notI.isValid(pf));

        Constant last = mock(Constant.class);
        doReturn(false).when(last).isFalsehood();
        doReturn(1).when(pStep1).getAssumptionLevel();
        doReturn(1).when(pStep0).getAssumptionLevel();
        doReturn(null).when(pStep1).getStep();

        Assertions.assertFalse(notI.isValid(pf));

        doReturn(last).when(pStep1).getStep();

        Assertions.assertFalse(notI.isValid(pf));

        doReturn(true).when(last).isFalsehood();
        doReturn(proof).when(pStep0).getProof();
        doReturn("Ass").when(proof).getNameProof();

        Assertions.assertTrue(notI.isValid(pf));
    }

    @Test
    public void notEIsValidTest() {
        NotEStub notE = new NotEStub(1);

        Assertions.assertEquals(1, notE.getNeg());

        Assertions.assertEquals(new NotEStub(1), notE);
        Assertions.assertNotEquals(new NotEStub(1) {
        }, notE);
        Assertions.assertEquals(new NotEStub(1).hashCode(), notE.hashCode());

        doReturn(list).when(pf).getSteps();
        doReturn(1).when(list).size();
        doReturn(pStep0).when(list).get(eq(0));
        doReturn(false).when(pStep0).isValid();

        Assertions.assertFalse(notE.isValid(pf));

        doReturn(true).when(pStep0).isValid();
        Variable variable = mock(Variable.class);
        Negation<LogicOperation> negation = mockNot(mockNot(variable));
        doReturn(negation).when(pStep0).getStep();

        Assertions.assertTrue(notE.isValid(pf));
    }

    @Test
    public void notEApplyTest() {
        List<ProofStep<LogicOperation>> record = new ArrayList<>();
        doReturn(list).when(pf).getSteps();
        doReturn(1).when(list).size();
        doReturn(pStep0).when(list).get(eq(0));
        doReturn(0).when(pStep0).getAssumptionLevel();
        Variable variable = mock(Variable.class, Answers.CALLS_REAL_METHODS);
        doReturn("P").when(variable).toString();
        Negation<LogicOperation> negation = mockNot(mockNot(variable));
        doReturn(negation).when(pStep0).getStep();
        doAnswer(invocationOnMock -> record.add(invocationOnMock.getArgument(0))).when(list).add(any(ProofStep.class));

        NotEStub notE = new NotEStub(1);
        notE.applyStepSupplier(pf, ProofStep::new);

        Assertions.assertEquals(new ProofReason("-E", List.of(1)), record.get(0).getProof());
        Assertions.assertEquals(0, record.get(0).getAssumptionLevel());
        Assertions.assertEquals("P", record.get(0).getStep().toString());
        Assertions.assertTrue(record.get(0).isValid());
    }

    @Test
    public void notIApplyTest() {
        List<ProofStep<LogicOperation>> record = new ArrayList<>();
        doReturn(list).when(pf).getSteps();
        doReturn(2).when(list).size();
        doReturn(pStep0).when(list).get(eq(0));
        doReturn(pStep1).when(list).get(eq(1));
        doReturn(1).when(pStep1).getAssumptionLevel();
        doReturn(1).when(pStep0).getAssumptionLevel();
        Variable variable = mock(Variable.class, Answers.CALLS_REAL_METHODS);
        doReturn("P").when(variable).toString();
        doReturn(variable).when(pStep0).getStep();
        doAnswer(invocationOnMock -> record.add(invocationOnMock.getArgument(0))).when(list).add(any(ProofStep.class));

        NotIStub notI = new NotIStub();
        notI.applyStepSupplier(pf, ProofStep::new);

        Assertions.assertEquals(new ProofReason("-I", List.of(1, 2)), record.get(0).getProof());
        Assertions.assertEquals(0, record.get(0).getAssumptionLevel());
        Assertions.assertEquals("- P", record.get(0).getStep().toString());
        Assertions.assertTrue(record.get(0).isValid());
    }

    public static class NotIStub extends NotI<LogicOperation, ProofStep<LogicOperation>, ProofTest.ProofStub> {
        public NotIStub() {
            super(RuleNotTest::mockNot);
        }
    }

    public static class NotEStub extends NotE<LogicOperation, ProofStep<LogicOperation>, ProofTest.ProofStub> {
        public NotEStub(int i) {
            super(i);
        }
    }

}
