package com.dan323.uses.test;

import com.dan323.uses.ActionsUseCases;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.Transformer;
import com.dan323.uses.internal.ActionsUseCaseConfiguration;
import com.dan323.uses.mock.LogicalSolvers;
import com.dan323.uses.mock.Transformers;
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

    private final WithTransformers useCases = transformers -> getActions -> solvers -> actionsUseCaseConfiguration.useCases(getActions, solvers, transformers);

    @Test
    public void solveTest() {
        ActionsUseCases cases = useCases
                .withNoTransformers()
                .withNoActions()
                .withSolvers(LogicalSolvers.getLogicalSolvers());
        var p1 = cases.solveProblem("l1");
        var p2 = cases.solveProblem("l2");
        assertEquals(genericProof("l1"), p1.perform(genericProof("l1")));
        assertEquals(genericProof("l2"), p2.perform(genericProof("l2")));
        assertThrows(IllegalArgumentException.class, () -> cases.solveProblem("l3"));
    }

    @Test
    public void actionsTest() {
        ActionsUseCases cases = useCases
                .withNoTransformers()
                .withActionGetters(actionsList())
                .withoutSolvers();
        var p1 = cases.getActions("l1");
        var p2 = cases.getActions("l2");
        assertEquals(List.of("l1.A1", "l1.A2", "l1.A3"), p1.perform());
        assertEquals(List.of("l2.A1", "l2.A2", "l2.A3"), p2.perform());
        assertThrows(IllegalArgumentException.class, () -> cases.getActions("l3"));
    }

    @Test
    public void applierTest() {
        var cases = useCases
                .withTransformers(Transformers.getTransformers())
                .withNoActions()
                .withoutSolvers();
        var p = cases.applyAction("l1").perform(actionAddOneStep(), genericProof("l1"));
        assertEquals(genericProof("l1").steps().size() + 1, p.steps().size());
        p = cases.applyAction("l2").perform(invalid(), genericProof("l2"));
        assertEquals(genericProof("l2"), p);
    }

    @FunctionalInterface
    public interface WithTransformers {

        WithActionGetter withTransformers(List<Transformer> actions);

        default WithActionGetter withNoTransformers() {
            return withTransformers(List.of());
        }

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
