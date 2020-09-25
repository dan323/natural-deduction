package com.dan323.proof.generic;

import com.dan323.expresions.base.BinaryOperation;
import com.dan323.expresions.base.Conjunction;
import com.dan323.expresions.base.LogicOperation;
import com.dan323.expresions.base.Variable;
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

import static org.mockito.ArgumentMatchers.intThat;
import static org.mockito.Mockito.*;

/**
 * @author danco
 */
@ExtendWith(MockitoExtension.class)
public class RuleConjunctionTest {

    @Mock
    public ProofTest.ProofStub pf;

    @Mock
    public List<ProofStep<LogicOperation>> list;

    @Mock
    public ProofStep<LogicOperation> pStep;

    protected static <T extends BinaryOperation<LogicOperation>> T mockOperation(Class<T> operationType, LogicOperation l1, LogicOperation l2) {
        T operation = mock(operationType, Answers.CALLS_REAL_METHODS);
        doReturn(l1).when(operation).getLeft();
        doReturn(l2).when(operation).getRight();
        return operation;
    }

    protected static Conjunction<LogicOperation> mockConjunction(LogicOperation l1, LogicOperation l2) {
        return mockOperation(Conjunction.class, l1, l2);
    }

    @Test
    public void andIValidityTest() {
        AndIStub andI = new AndIStub(1, 2);

        Assertions.assertEquals(new AndIStub(1, 2), andI);
        Assertions.assertEquals(new AndIStub(1, 2).hashCode(), andI.hashCode());
        Assertions.assertEquals(1, andI.get1());
        Assertions.assertEquals(2, andI.get2());

        doReturn(list).when(pf).getSteps();
        doReturn(pStep).when(list).get(intThat(i -> 0 <= i && i <= 1));
        doReturn(true).when(pStep).isValid();
        doReturn(2).when(list).size();
        Assertions.assertTrue(andI.isValid(pf));
    }

    @Test
    public void andRuleNotEquals() {
        Assertions.assertNotEquals(new AndIStub(1, 2), new AndIStub(1, 3));
        Assertions.assertNotEquals(new AndIStub(1, 2), new AndIStub(2, 2));
        Assertions.assertNotEquals(new AndEStub(1), new AndEStub(2));
        Assertions.assertNotEquals(new AndIStub(1, 2), new AndEStub(2));
        Assertions.assertNotEquals(new AndEStub(2), new AndIStub(1, 2));
    }

    @Test
    public void andEValidityTest() {
        AndEStub andE = new AndEStub(1);

        Assertions.assertEquals(new AndEStub(1), andE);
        Assertions.assertEquals(new AndEStub(1).hashCode(), andE.hashCode());
        Assertions.assertEquals(1, andE.getAppliedAt());

        doReturn(list).when(pf).getSteps();
        doReturn(pStep).when(list).get(intThat(i -> 0 == i));
        doReturn(1).when(list).size();
        doReturn(mock(Conjunction.class)).when(pStep).getStep();
        doReturn(true).when(pStep).isValid();
        Assertions.assertTrue(andE.isValid(pf));
    }

    @Test
    public void andIApply() {
        List<ProofStep<LogicOperation>> record = new ArrayList<>();
        doReturn(list).when(pf).getSteps();
        doReturn(2).when(list).size();
        doReturn(false).when(list).isEmpty();
        doReturn(pStep).when(list).get(intThat(i -> 0 <= i && i < 2));
        doReturn(1).when(pStep).getAssumptionLevel();
        Variable variable = mock(Variable.class, Answers.CALLS_REAL_METHODS);
        doReturn("P").when(variable).toString();
        doReturn(variable).when(pStep).getStep();
        doAnswer(invocationOnMock ->
                record.add(invocationOnMock.getArgument(0))
        ).when(list).add(any(ProofStep.class));

        AndIStub andI = new AndIStub(1, 2);
        andI.applyStepSupplier(pf, ProofStep::new);

        Assertions.assertEquals("P & P", record.get(0).getStep().toString());
        Assertions.assertEquals(1, record.get(0).getAssumptionLevel());
        Assertions.assertEquals("&I [1, 2]", record.get(0).getProof().toString());
        Assertions.assertEquals("   P & P           &I [1, 2]", record.get(0).toString());
    }

    @Test
    public void andEApply() {
        List<ProofStep<LogicOperation>> record = new ArrayList<>();
        doReturn(list).when(pf).getSteps();
        doReturn(1).when(list).size();
        doReturn(false).when(list).isEmpty();
        doReturn(pStep).when(list).get(intThat(i -> i == 0));
        doReturn(1).when(pStep).getAssumptionLevel();
        Variable variable = mock(Variable.class, Answers.CALLS_REAL_METHODS);
        doReturn("P").when(variable).toString();
        doReturn(mockConjunction(variable, variable)).when(pStep).getStep();
        doAnswer(invocationOnMock -> record.add(invocationOnMock.getArgument(0))).when(list).add(any(ProofStep.class));

        AndEStub andE = new AndEStub(1);
        andE.applyStepSupplier(pf, ProofStep::new);

        Assertions.assertEquals("P", record.get(0).getStep().toString());
        Assertions.assertEquals(1, record.get(0).getAssumptionLevel());
        Assertions.assertEquals("&E [1]", record.get(0).getProof().toString());
        Assertions.assertEquals("   P           &E [1]", record.get(0).toString());

        AndEStub2 andE2 = new AndEStub2(1);
        andE2.applyStepSupplier(pf, ProofStep::new);

        Assertions.assertEquals("P", record.get(1).getStep().toString());
        Assertions.assertEquals(1, record.get(1).getAssumptionLevel());
        Assertions.assertEquals("&E [1]", record.get(1).getProof().toString());
        Assertions.assertEquals("   P           &E [1]", record.get(1).toString());
    }

    public static class AndEStub extends AndE<LogicOperation, ProofStep<LogicOperation>, ProofTest.ProofStub> {
        public AndEStub(int app) {
            super(app, Conjunction<LogicOperation>::getLeft);
        }
    }

    public static class AndEStub2 extends AndE<LogicOperation, ProofStep<LogicOperation>, ProofTest.ProofStub> {
        public AndEStub2(int app) {
            super(app, Conjunction<LogicOperation>::getRight);
        }
    }

    public static class AndIStub extends AndI<LogicOperation, ProofStep<LogicOperation>, ProofTest.ProofStub> {
        public AndIStub(int app1, int app2) {
            super(app1, app2, RuleConjunctionTest::mockConjunction);
        }
    }

}
