package com.dan323.proof.generic;

import com.dan323.expresions.base.*;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RuleFalseTest {

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
    public void fIIsValidTest() {
        FIStub fI = new RuleFalseTest.FIStub(1, 2);

        assertEquals(1, fI.getPos());
        assertEquals(2, fI.getNeg());
        assertEquals(new RuleFalseTest.FIStub(1, 2), fI);
        assertNotEquals(new RuleFalseTest.FIStub(1, 3), fI);
        assertEquals(new RuleFalseTest.FIStub(1, 2).hashCode(), fI.hashCode());

        doReturn(list).when(pf).getSteps();
        doReturn(2).when(list).size();
        doReturn(pStep0).when(list).get(eq(0));
        doReturn(pStep1).when(list).get(eq(1));
        doReturn(false).when(pStep1).isValid();

        assertFalse(fI.isValid(pf));

        doReturn(true).when(pStep1).isValid();
        doReturn(false).when(pStep0).isValid();

        assertFalse(fI.isValid(pf));

        doReturn(true).when(pStep0).isValid();
        doReturn(mock(Variable.class)).when(pStep1).getStep();

        assertFalse(fI.isValid(pf));

        Variable<LogicOperation> var = new Variable<>("P") {
        };
        Negation<LogicOperation> notVar = mockNot(var);
        doReturn(notVar).when(pStep1).getStep();
        doReturn(var).when(pStep0).getStep();

        assertTrue(fI.isValid(pf));
    }

    @Test
    public void equalsTest(){
        Variable<LogicOperation> var = mock(Variable.class);
        FEStub fE = new RuleFalseTest.FEStub(var, 1);

        assertEquals(fE, fE);
        assertNotEquals(fE, new RuleFalseTest.FEStub(var, 2));
        assertEquals(new RuleFalseTest.FEStub(var, 1), fE);
        assertNotEquals(fE, new Object());
        assertEquals(new RuleFalseTest.FEStub(var, 1).hashCode(), fE.hashCode());
    }

    @Test
    public void fEIsValidTest() {
        Variable<LogicOperation> var = mock(Variable.class);
        FEStub fE = new RuleFalseTest.FEStub(var, 1);

        doReturn(list).when(pf).getSteps();
        doReturn(1).when(list).size();
        doReturn(pStep0).when(list).get(eq(0));
        doReturn(false).when(pStep0).isValid();

        assertFalse(fE.isValid(pf));

        doReturn(true).when(pStep0).isValid();
        Variable<LogicOperation> variable = mock(Variable.class);
        Negation<LogicOperation> negation = mockNot(mockNot(variable));
        doReturn(negation).when(pStep0).getStep();

        assertFalse(fE.isValid(pf));

        Constant<LogicOperation> constant = mock(Constant.class);
        doReturn(constant).when(pStep0).getStep();
        doReturn(false).when(constant).isFalsehood();

        assertFalse(fE.isValid(pf));

        doReturn(true).when(constant).isFalsehood();

        assertTrue(fE.isValid(pf));

    }

    @Test
    public void fEApplyTest() {
        List<ProofStep<LogicOperation>> record = new ArrayList<>();
        doReturn(list).when(pf).getSteps();
        doReturn(1).when(list).size();
        doReturn(pStep0).when(list).get(eq(0));
        doReturn(1).when(pStep0).getAssumptionLevel();
        doAnswer(invocationOnMock -> record.add(invocationOnMock.getArgument(0))).when(list).add(any(ProofStep.class));
        Variable<LogicOperation> variable = mock(Variable.class, Answers.CALLS_REAL_METHODS);
        doReturn("P").when(variable).toString();

        FEStub notE = new RuleFalseTest.FEStub(variable, 1);
        notE.applyStepSupplier(pf, ProofStep::new);

        assertEquals(new ProofReason("FE", List.of(1)), record.get(0).getProof());
        assertEquals(1, record.get(0).getAssumptionLevel());
        assertEquals("P", record.get(0).getStep().toString());
        assertTrue(record.get(0).isValid());
    }

    @Test
    public void fIApplyTest() {
        List<ProofStep<LogicOperation>> record = new ArrayList<>();
        doReturn(list).when(pf).getSteps();
        doReturn(2).when(list).size();
        doReturn(pStep1).when(list).get(eq(1));
        doReturn(1).when(pStep1).getAssumptionLevel();
        doAnswer(invocationOnMock -> record.add(invocationOnMock.getArgument(0))).when(list).add(any(ProofStep.class));

        FIStub notI = new RuleFalseTest.FIStub(1, 2);
        notI.applyStepSupplier(pf, ProofStep::new);

        assertEquals(new ProofReason("FI", List.of(1, 2)), record.get(0).getProof());
        assertEquals(1, record.get(0).getAssumptionLevel());
        assertTrue(((Constant<?>) record.get(0).getStep()).isFalsehood());
        assertTrue(record.get(0).isValid());
    }

    public static <T extends LogicOperation> Constant<T> getFalse() {
        return () -> true;
    }

    public static class FIStub extends FI<LogicOperation, ProofStep<LogicOperation>, ProofTest.ProofStub> {
        public FIStub(int a, int b) {
            super(a, b, RuleFalseTest::getFalse);
        }
    }

    public static class FEStub extends FE<LogicOperation, ProofStep<LogicOperation>, ProofTest.ProofStub> {
        public FEStub(LogicOperation op, int i) {
            super(op, i);
        }
    }

}
