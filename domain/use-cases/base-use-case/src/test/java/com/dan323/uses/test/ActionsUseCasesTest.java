package com.dan323.uses.test;

import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.Difficulty;
import com.dan323.model.ExerciseDto;
import com.dan323.model.ProofDto;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.uses.ActionsUseCases;
import com.dan323.uses.Exercise;
import com.dan323.uses.InvalidActionException;
import com.dan323.uses.LogicalExercises;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.ProofParser;
import com.dan323.uses.Transformer;
import com.dan323.uses.UnknownLogicException;
import com.dan323.uses.internal.ActionsUseCaseConfiguration;
import com.dan323.uses.mock.Parsers;
import com.dan323.uses.mock.Transformers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.dan323.uses.mock.ActionGetters.actionsList;
import static com.dan323.uses.mock.Actions.actionAddOneStep;
import static com.dan323.uses.mock.Actions.invalid;
import static com.dan323.uses.mock.Proofs.genericProof;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionsUseCasesTest {

    private final ActionsUseCaseConfiguration actionsUseCaseConfiguration = new ActionsUseCaseConfiguration();

    private final WithParsers useCases = parsers
            -> transformers
            -> getActions
            -> actionsUseCaseConfiguration.useCases(getActions, transformers, parsers, List.of());

    @Test
    public void solveTest() {
        ActionsUseCases cases = useCases
                .withNoParsers()
                .withTransformers(Transformers.getTransformers())
                .withNoActions();
        var p1 = cases.solveProblem("l1");
        var p2 = cases.solveProblem("l2");
        assertEquals(genericProof("l1"), p1.perform(genericProof("l1")));
        assertEquals(genericProof("l2"), p2.perform(genericProof("l2")));
        assertThrows(IllegalArgumentException.class, () -> cases.solveProblem("l3"));
    }

    @Test
    public void actionsTest() {
        ActionsUseCases cases = useCases
                .withNoParsers()
                .withNoTransformers()
                .withActionGetters(actionsList());
        var p1 = cases.getActions("l1");
        var p2 = cases.getActions("l2");
        assertEquals(List.of("l1.A1", "l1.A2", "l1.A3"), p1.perform().stream().map(ActionDescriptorDto::name).toList());
        assertEquals(List.of("l2.A1", "l2.A2", "l2.A3"), p2.perform().stream().map(ActionDescriptorDto::name).toList());
        assertThrows(IllegalArgumentException.class, () -> cases.getActions("l3"));
    }

    @Test
    public void exercisesTest() {
        var catalog = new LogicalExercises() {
            @Override
            public String logic() {
                return "l1";
            }

            @Override
            public List<Exercise> exercises() {
                return List.of(new Exercise("e1", "First", List.of("P"), "P", Difficulty.EASY, "the solution"));
            }
        };
        var cases = actionsUseCaseConfiguration.useCases(actionsList(), List.of(), List.of(), List.of(catalog));
        assertEquals(List.of(new ExerciseDto("e1", "First", List.of("P"), "P", Difficulty.EASY)), cases.getExercises("l1").perform());
        assertEquals(List.of(), cases.getExercises("l2").perform());
        assertThrows(UnknownLogicException.class, () -> cases.getExercises("l3"));
    }

    @Test
    public void applierTest() {
        var cases = useCases
                .withNoParsers()
                .withTransformers(Transformers.getTransformers())
                .withNoActions();
        var result = cases.applyAction("l1").perform(actionAddOneStep(), genericProof("l1"));
        assertTrue(result.applied());
        assertEquals("", result.message());
        assertEquals(genericProof("l1").steps().size() + 1, result.proof().steps().size());
    }

    @Test
    public void rejectedActionSaysWhyTest() {
        var cases = useCases
                .withNoParsers()
                .withTransformers(Transformers.getTransformers())
                .withNoActions();
        var notApplicable = cases.applyAction("l2").perform(actionAddOneStep(), genericProof("l2"));
        assertFalse(notApplicable.applied());
        assertEquals("Rule Action1 cannot be applied", notApplicable.message());
        assertEquals(genericProof("l2"), notApplicable.proof());
    }

    @Test
    public void outOfRangeSourceIsRejectedTest() {
        var cases = useCases
                .withNoParsers()
                .withTransformers(Transformers.getTransformers())
                .withNoActions();
        var result = cases.applyAction("l1").perform(invalid(), genericProof("l1"));
        assertFalse(result.applied());
        assertEquals("Line 1 does not exist, the proof has 0 lines", result.message());
        assertEquals(genericProof("l1"), result.proof());
    }

    @Test
    public void unbuildableActionTest() {
        var throwing = new Transformer() {
            @Override
            public String logic() {
                return "l1";
            }

            @Override
            public Proof from(ProofDto proofDto) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Action from(ActionDto actionDto) {
                throw new IllegalArgumentException("No such action");
            }

            @Override
            public ProofDto fromProof(Proof proof) {
                throw new UnsupportedOperationException();
            }
        };
        var cases = useCases
                .withNoParsers()
                .withTransformers(List.of(throwing))
                .withNoActions();
        var applier = cases.applyAction("l1");
        var action = actionAddOneStep();
        var proof = genericProof("l1");
        var exception = assertThrows(InvalidActionException.class, () -> applier.perform(action, proof));
        assertEquals("Cannot build action 'Action1': No such action", exception.getMessage());
    }

    @Test
    public void buildFailureWithoutMessageDoesNotLeakNullTest() {
        for (var failure : List.of(new IllegalStateException(), new IllegalStateException("  "))) {
            var applier = applierFailingToBuild(failure);
            var action = actionAddOneStep();
            var proof = genericProof("l1");
            var exception = assertThrows(InvalidActionException.class, () -> applier.perform(action, proof));
            assertEquals("Cannot build action 'Action1': the expression could not be parsed", exception.getMessage());
            assertSame(failure, exception.getCause());
        }
    }

    @Test
    public void invalidActionIsNotWrappedAgainTest() {
        var failure = new InvalidActionException("Action1 needs an expression");
        var applier = applierFailingToBuild(failure);
        var action = actionAddOneStep();
        var proof = genericProof("l1");
        var exception = assertThrows(InvalidActionException.class, () -> applier.perform(action, proof));
        assertSame(failure, exception);
    }

    private ActionsUseCases.ApplyAction applierFailingToBuild(RuntimeException failure) {
        var throwing = new Transformer() {
            @Override
            public String logic() {
                return "l1";
            }

            @Override
            public Proof from(ProofDto proofDto) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Action from(ActionDto actionDto) {
                throw failure;
            }

            @Override
            public ProofDto fromProof(Proof proof) {
                throw new UnsupportedOperationException();
            }
        };
        return useCases
                .withNoParsers()
                .withTransformers(List.of(throwing))
                .withNoActions()
                .applyAction("l1");
    }

    @Test
    public void unknownLogicIsReportedConsistentlyTest() {
        var cases = useCases
                .withParsers(Parsers.parsers())
                .withTransformers(Transformers.getTransformers())
                .withActionGetters(actionsList());
        assertThrows(UnknownLogicException.class, () -> cases.getActions("l3"));
        assertThrows(UnknownLogicException.class, () -> cases.applyAction("l3"));
        assertThrows(UnknownLogicException.class, () -> cases.solveProblem("l3"));
        assertThrows(UnknownLogicException.class, () -> cases.parseToProof("l3"));
        assertEquals("Unknown logic 'l3'", assertThrows(UnknownLogicException.class, () -> cases.getActions("l3")).getMessage());
    }

    @Test
    public void parseProofTest() {
        var cases = useCases
                .withParsers(Parsers.parsers())
                .withTransformers(Transformers.getTransformers())
                .withNoActions();
        var proof = cases.parseToProof("l1").perform("Something");
        assertEquals(Parsers.expectedLength(), proof.steps().size());
    }

    @FunctionalInterface
    public interface WithParsers {

        WithTransformers withParsers(List<ProofParser> parsers);

        default WithTransformers withNoParsers() {
            return withParsers(List.of());
        }

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

        ActionsUseCases withActionGetters(List<LogicalGetActions> actions);

        default ActionsUseCases withNoActions() {
            return withActionGetters(List.of());
        }

    }

}
