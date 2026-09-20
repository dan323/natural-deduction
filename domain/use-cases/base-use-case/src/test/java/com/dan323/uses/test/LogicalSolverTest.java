package com.dan323.uses.test;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.SolveTimeoutException;
import com.dan323.uses.SolverBusyException;
import com.dan323.uses.Transformer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.dan323.uses.mock.Proofs.genericProof;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogicalSolverTest {

    private static final Duration LONG = Duration.ofSeconds(30);
    private static final ProofDto SOLVED = new ProofDto(List.of(), "l", "solved");

    private interface Act extends Action<LogicOperation, ProofStep<LogicOperation>, TestProof> {
    }

    /** A proof whose solver is whatever the test says. */
    private static final class TestProof extends Proof<LogicOperation, ProofStep<LogicOperation>> {

        private final Runnable solver;

        TestProof(Runnable solver) {
            this.solver = solver;
        }

        @Override
        public List<Act> parse() {
            return List.of();
        }

        @Override
        protected ProofStep<LogicOperation> generateAssm(LogicOperation logicexpression) {
            return null;
        }

        @Override
        public void automate() {
            solver.run();
        }

        @Override
        public void initializeProof(List<LogicOperation> assms, LogicOperation goal) {
            // Nothing to set up
        }
    }

    private static Transformer<LogicOperation, ProofStep<LogicOperation>, TestProof, Act> transformer(Runnable solver) {
        return new Transformer<>() {
            @Override
            public String logic() {
                return "l";
            }

            @Override
            public TestProof from(ProofDto proofDto) {
                return new TestProof(solver);
            }

            @Override
            public Act from(ActionDto actionDto) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ProofDto fromProof(TestProof proof) {
                return SOLVED;
            }
        };
    }

    /** Tells the test it started, spins until interrupted, then tells the test it was stopped. */
    private static Runnable spinUntilInterrupted(CountDownLatch started, CountDownLatch stopped) {
        return () -> {
            started.countDown();
            while (!Thread.currentThread().isInterrupted()) {
                Thread.onSpinWait();
            }
            stopped.countDown();
        };
    }

    @Test
    void solvesWithinTheLimit() {
        var ran = new boolean[1];
        var solver = new LogicalSolver<>(transformer(() -> ran[0] = true), LONG);

        assertSame(SOLVED, solver.perform(genericProof("l")));
        assertTrue(ran[0]);
    }

    @Test
    void stopsTheSolverAndReportsATimeout() throws InterruptedException {
        var started = new CountDownLatch(1);
        var stopped = new CountDownLatch(1);
        var solver = new LogicalSolver<>(transformer(spinUntilInterrupted(started, stopped)), Duration.ofMillis(500));
        var proof = genericProof("l");

        var timeout = assertThrows(SolveTimeoutException.class, () -> solver.perform(proof));

        assertEquals("The solver did not finish within 500 ms, try solving part of the proof by hand first", timeout.getMessage());
        assertTrue(started.await(10, TimeUnit.SECONDS), "the solver never started");
        assertTrue(stopped.await(10, TimeUnit.SECONDS), "the solver thread was not interrupted");
    }

    @Test
    void timeoutMessageUsesSecondsForWholeSeconds() {
        assertEquals("The solver did not finish within 2 seconds, try solving part of the proof by hand first",
                new SolveTimeoutException(Duration.ofSeconds(2)).getMessage());
    }

    @Test
    void failuresOfTheSolverAreRethrown() {
        var failure = new IllegalStateException("boom");
        var solver = new LogicalSolver<>(transformer(() -> {
            throw failure;
        }), LONG);
        var proof = genericProof("l");

        assertSame(failure, assertThrows(IllegalStateException.class, () -> solver.perform(proof)));
    }

    @Test
    void anErrorInTheSolverIsWrapped() {
        var solver = new LogicalSolver<>(transformer(() -> {
            throw new AssertionError("fatal");
        }), LONG);
        var proof = genericProof("l");

        var thrown = assertThrows(IllegalStateException.class, () -> solver.perform(proof));

        assertEquals("fatal", thrown.getCause().getMessage());
    }

    @Test
    void aCallerInterruptedWhileWaitingStopsTheSolver() throws InterruptedException {
        var started = new CountDownLatch(1);
        var stopped = new CountDownLatch(1);
        var solver = new LogicalSolver<>(transformer(spinUntilInterrupted(started, stopped)), LONG);
        var proof = genericProof("l");
        var failure = new AtomicReference<Throwable>();
        var caller = new Thread(() -> {
            try {
                solver.perform(proof);
            } catch (RuntimeException e) {
                failure.set(e);
            }
        });

        caller.start();
        assertTrue(started.await(10, TimeUnit.SECONDS), "the solver never started");
        caller.interrupt();
        caller.join(10_000);

        assertFalse(caller.isAlive());
        assertInstanceOf(IllegalStateException.class, failure.get());
        assertTrue(stopped.await(10, TimeUnit.SECONDS), "the solver thread was not interrupted");
    }

    @Test
    void aSolveBeyondTheConcurrencyLimitIsTurnedAway() throws InterruptedException {
        var started = new CountDownLatch(1);
        var stopped = new CountDownLatch(1);
        var solver = new LogicalSolver<>(transformer(spinUntilInterrupted(started, stopped)), LONG, 1);
        var proof = genericProof("l");
        var first = new Thread(() -> {
            try {
                solver.perform(proof);
            } catch (IllegalStateException interrupted) {
                // the test interrupts the first solve at the end
            }
        });

        first.start();
        assertTrue(started.await(10, TimeUnit.SECONDS), "the first solve never started");
        try {
            assertThrows(SolverBusyException.class, () -> solver.perform(proof));
        } finally {
            first.interrupt();
            first.join(10_000);
        }
        assertTrue(stopped.await(10, TimeUnit.SECONDS), "the first solve was not stopped");
    }
}
