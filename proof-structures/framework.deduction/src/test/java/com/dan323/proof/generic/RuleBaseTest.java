package com.dan323.proof.generic;

import com.dan323.expresions.base.LogicOperation;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.generic.proof.ProofTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RuleBaseTest {

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

    @Test
    public void assmsIsValid() {
        AssumeStub assms = new AssumeStub(mock(LogicOperation.class));
        assertTrue(assms.isValid(pf));
    }

    @Test
    public void copyIsValid() {
        CopyStub copy = new CopyStub(1);

        assertEquals(1, copy.getAppliedAt());

        doReturn(list).when(pf).getSteps();
        doReturn(1).when(list).size();
        doReturn(pStep0).when(list).get(eq(0));
        doReturn(false).when(pStep0).isValid();

        assertFalse(copy.isValid(pf));

        doReturn(true).when(pStep0).isValid();

        assertTrue(copy.isValid(pf));
    }

    @Test
    public void assmsApplyTest() {
        List<ProofStep<LogicOperation>> record = new ArrayList<>();
        LogicOperation log = mock(LogicOperation.class);
        AssumeStub assms = new AssumeStub(log);
        doAnswer(invocationOnMock -> record.add(invocationOnMock.getArgument(0))).when(list).add(any(ProofStep.class));
        doReturn(list).when(pf).getSteps();
        doReturn(true).when(list).isEmpty();
        assms.applyStepSupplier(pf, ProofStep::new);

        assertEquals(1, record.get(0).getAssumptionLevel());
        assertEquals("Ass", record.get(0).getProof().toString());
        assertEquals(log, record.get(0).getStep());

        record.clear();
        doReturn(false).when(list).isEmpty();
        doReturn(2).when(list).size();
        doReturn(pStep0).when(list).get(eq(1));
        doReturn(2).when(pStep0).getAssumptionLevel();
        assms.applyStepSupplier(pf, ProofStep::new);

        assertEquals(3, record.get(0).getAssumptionLevel());
        assertEquals("Ass", record.get(0).getProof().toString());
        assertEquals(log, record.get(0).getStep());
    }

    @Test
    public void copyApplyTest() {
        List<ProofStep<LogicOperation>> record = new ArrayList<>();
        doAnswer(invocationOnMock -> record.add(invocationOnMock.getArgument(0))).when(list).add(any(ProofStep.class));
        CopyStub copy = new CopyStub(1);
        doReturn(list).when(pf).getSteps();
        doReturn(pStep0).when(list).get(0);
        doReturn(mock(LogicOperation.class)).when(pStep0).getStep();
        doReturn(pStep1).when(list).get(1);
        doReturn(2).when(list).size();
        doReturn(3).when(pStep1).getAssumptionLevel();
        copy.applyStepSupplier(pf, ProofStep::new);

        assertEquals(3, record.get(0).getAssumptionLevel());
        assertEquals(pStep0.getStep(), record.get(0).getStep());
        assertEquals("Rep [1]", record.get(0).getProof().toString());
    }

    public static class AssumeStub extends Assume<LogicOperation, ProofStep<LogicOperation>, ProofTest.ProofStub> {
        public AssumeStub(LogicOperation clo) {
            super(clo);
        }
    }

    public static class CopyStub extends Copy<LogicOperation, ProofStep<LogicOperation>, ProofTest.ProofStub> {
        public CopyStub(int clo) {
            super(clo);
        }
    }

}
