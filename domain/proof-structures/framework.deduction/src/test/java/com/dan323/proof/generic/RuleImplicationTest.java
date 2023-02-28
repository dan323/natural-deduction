package com.dan323.proof.generic;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.base.Implication;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.base.Variable;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.Mockito.*;

/**
 * @author danco
 */
@ExtendWith(MockitoExtension.class)
public class RuleImplicationTest {

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

    protected static Implication<LogicOperation> mockImplication(LogicOperation l1, LogicOperation l2) {
        return mockOperation(Implication.class, l1, l2);
    }

    @Test
    public void deductionThIsValidTest() {
        DeductionTheoremStub ded = new DeductionTheoremStub();

        Assertions.assertEquals(new DeductionTheoremStub(), ded);
        Assertions.assertEquals(new DeductionTheoremStub().hashCode(), ded.hashCode());

        doReturn(list).when(pf).getSteps();
        doReturn(true).when(list).isEmpty();

        Assertions.assertFalse(ded.isValid(pf));

        doReturn(2).when(list).size();
        doReturn(false).when(list).isEmpty();
        doReturn(pStep0).when(list).get(ArgumentMatchers.intThat(i -> 0 <= i && i <= 1));

        Assertions.assertFalse(ded.isValid(pf));

        doReturn(1).when(pStep0).getAssumptionLevel();
        doReturn(proof).when(pStep0).getProof();
        doReturn("Ass").when(proof).getNameProof();
        Assertions.assertTrue(ded.isValid(pf));
    }

    @Test
    public void deductionThApplyTest() {
        List<ProofStep<LogicOperation>> record = new ArrayList<>();
        doReturn(list).when(pf).getSteps();
        doReturn(2).when(list).size();
        doReturn(pStep0).when(list).get(intThat(i -> 0 <= i && i < 2));
        doReturn(1).when(pStep0).getAssumptionLevel();
        Variable variable = mock(Variable.class, Answers.CALLS_REAL_METHODS);
        doReturn("P").when(variable).toString();
        doReturn(variable).when(pStep0).getStep();
        doAnswer(invocationOnMock -> record.add(invocationOnMock.getArgument(0))).when(list).add(any(ProofStep.class));

        DeductionTheoremStub ded = new DeductionTheoremStub();
        ded.applyStepSupplier(pf, ProofStep::new);

        Assertions.assertEquals(new ProofReason("->I", List.of(1, 2)), record.get(0).getProof());
        Assertions.assertEquals(0, record.get(0).getAssumptionLevel());
        Assertions.assertEquals("P -> P", record.get(0).getStep().toString());
        Assertions.assertTrue(record.get(0).isValid());
    }

    @Test
    public void modusPonensValidTest() {
        ModusPonensStub ded = new ModusPonensStub(1, 2);

        Assertions.assertEquals(new ModusPonensStub(1, 2), ded);
        Assertions.assertEquals(new ModusPonensStub(1, 2).hashCode(), ded.hashCode());

        doReturn(list).when(pf).getSteps();

        Assertions.assertFalse(ded.isValid(pf));

        doReturn(2).when(list).size();
        doReturn(pStep0).when(list).get(eq(0));
        doReturn(false).when(pStep0).isValid();

        Assertions.assertFalse(ded.isValid(pf));

        var P = mock(Variable.class);
        var imp = mockImplication(P, P);
        doReturn(pStep1).when(list).get(eq(1));
        doReturn(true).when(pStep0).isValid();
        doReturn(true).when(pStep1).isValid();
        doReturn(P).when(pStep1).getStep();
        doReturn(imp).when(pStep0).getStep();

        Assertions.assertTrue(ded.isValid(pf));
    }

    @Test
    public void modusPonensApplyTest() {
        List<ProofStep<LogicOperation>> record = new ArrayList<>();
        doReturn(list).when(pf).getSteps();
        doReturn(2).when(list).size();
        doReturn(pStep0).when(list).get(eq(0));
        doReturn(pStep1).when(list).get(eq(1));
        Variable variable = mock(Variable.class, Answers.CALLS_REAL_METHODS);
        doReturn("P").when(variable).toString();
        doReturn(mockImplication(variable, variable)).when(pStep0).getStep();
        doAnswer(invocationOnMock -> record.add(invocationOnMock.getArgument(0))).when(list).add(any(ProofStep.class));

        ModusPonensStub ded = new ModusPonensStub(1, 2);
        ded.applyStepSupplier(pf, ProofStep::new);

        Assertions.assertEquals(new ProofReason("->E", List.of(1, 2)), record.get(0).getProof());
        Assertions.assertEquals(0, record.get(0).getAssumptionLevel());
        Assertions.assertEquals("P", record.get(0).getStep().toString());
        Assertions.assertTrue(record.get(0).isValid());
    }

    @Test
    public void equalsFalse() {
        Assertions.assertNotEquals(new ModusPonensStub(1, 2), new DeductionTheoremStub());
    }

    public static class DeductionTheoremStub extends DeductionTheorem<LogicOperation, ProofStep<LogicOperation>, ProofTest.ProofStub> {
        public DeductionTheoremStub() {
            super(RuleImplicationTest::mockImplication);
        }
    }

    public static class ModusPonensStub extends ModusPonens<LogicOperation, ProofStep<LogicOperation>, ProofTest.ProofStub> {
        public ModusPonensStub(int i1, int i2) {
            super(i1, i2);
        }
    }

}
