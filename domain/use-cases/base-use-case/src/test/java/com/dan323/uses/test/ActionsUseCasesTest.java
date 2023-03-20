package com.dan323.uses.test;

import com.dan323.uses.ActionsUseCases;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.internal.ActionsUseCaseConfiguration;
import com.dan323.uses.mock.LogicalSolvers;
import com.dan323.uses.mock.Proofs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.dan323.uses.mock.ActionGetters.actionsList;
import static com.dan323.uses.mock.Actions.actionAddOneStep;
import static com.dan323.uses.mock.Actions.invalid;
import static com.dan323.uses.mock.Proofs.genericProof;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ActionsUseCasesTest {

    private final ActionsUseCaseConfiguration actionsUseCaseConfiguration = new ActionsUseCaseConfiguration();

    private final WithActionGetter useCases = getActions -> solvers -> actionsUseCaseConfiguration.useCases(getActions, solvers);

    @Test
    public void solveTest() {
        ActionsUseCases cases = useCases.withNoActions().withSolvers(LogicalSolvers.getLogicalSolvers());
        var p1 = cases.solveProblem("l1");
        var p2 = cases.solveProblem("l2");
        assertEquals(genericProof(),p1.perform(genericProof()));
        assertEquals(genericProof(),p2.perform(genericProof()));
        assertThrows(IllegalArgumentException.class, () -> cases.solveProblem("l3"));
    }

    @Test
    public void actionsTest() {
        ActionsUseCases cases = useCases.withActionGetters(actionsList()).withoutSolvers();
        var p1 = cases.getActions("l1");
        var p2 = cases.getActions("l2");
        assertEquals(List.of("l1.A1", "l1.A2", "l1.A3"), p1.perform());
        assertEquals(List.of("l2.A1", "l2.A2", "l2.A3"), p2.perform());
        assertThrows(IllegalArgumentException.class, () -> cases.getActions("l3"));
    }

    @Test
    public void applierTest() {
        var cases = useCases.withNoActions().withoutSolvers();
        var p1 = cases.applyAction("l1");
        var p = p1.perform(actionAddOneStep(),genericProof());
        assertEquals(genericProof().getSteps().size()+1, p.getSteps().size());
        p = p1.perform(invalid(),genericProof());
        assertEquals(genericProof(), p);
    }

    @FunctionalInterface
    public interface WithActionGetter {

        WithSolvers withActionGetters(List<LogicalGetActions> actions);

        default WithSolvers withNoActions() {
            return withActionGetters(List.of());
        }

    }

    @FunctionalInterface
    public interface WithSolvers {

        ActionsUseCases withSolvers(List<LogicalSolver> solvers);

        default ActionsUseCases withoutSolvers() {
            return withSolvers(List.of());
        }
    }


}
