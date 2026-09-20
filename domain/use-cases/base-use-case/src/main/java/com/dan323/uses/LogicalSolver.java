package com.dan323.uses;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.model.ProofDto;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs the automatic solver ({@link Proof#automate()}) of a logic on a proof.
 * <p>
 * The solve is triggered by users, so it is limited by time: it runs on a separate daemon thread and, when the limit
 * passes, that thread is interrupted (the solvers check the interrupt flag between their steps) and the call fails
 * with a {@link SolveTimeoutException}.
 */
public class LogicalSolver<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>, A extends Action<T, Q, P>> implements ActionsUseCases.Solve {

    private static final AtomicInteger THREAD_COUNT = new AtomicInteger();
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(task -> {
        var thread = new Thread(task, "solver-" + THREAD_COUNT.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    });

    private final Transformer<T, Q, P, A> transformer;
    private final Duration timeout;

    public LogicalSolver(Transformer<T, Q, P, A> transformer, Duration timeout) {
        this.transformer = transformer;
        this.timeout = timeout;
    }

    @Override
    public ProofDto perform(ProofDto proof) {
        var naturalDeduction = transformer.from(proof);
        var running = EXECUTOR.submit(naturalDeduction::automate);
        try {
            running.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            running.cancel(true);
            throw new SolveTimeoutException(timeout);
        } catch (InterruptedException e) {
            running.cancel(true);
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while solving the proof", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException failure) {
                throw failure;
            }
            throw new IllegalStateException("The solver failed", e.getCause());
        }
        return transformer.fromProof(naturalDeduction);
    }
}
