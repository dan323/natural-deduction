package com.dan323.uses;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.model.ProofDto;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs the automatic solver ({@link Proof#automate()}) of a logic on a proof.
 * <p>
 * The solve is triggered by users, so it is limited twice. It is limited by time: it runs on a separate daemon thread
 * and, when the limit passes, that thread is interrupted (the solvers check the interrupt flag between their steps)
 * and the call fails with a {@link SolveTimeoutException}. And it is limited by concurrency: at most
 * {@code maxConcurrentSolves} solves run at once and the next one fails at once with a {@link SolverBusyException}
 * instead of piling up threads.
 */
public class LogicalSolver<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>, A extends Action<T, Q, P>> implements ActionsUseCases.Solve {

    public static final int DEFAULT_MAX_CONCURRENT_SOLVES = Math.max(2, Runtime.getRuntime().availableProcessors());

    private static final AtomicInteger THREAD_COUNT = new AtomicInteger();

    private final Transformer<T, Q, P, A> transformer;
    private final Duration timeout;
    private final ThreadPoolExecutor executor;

    public LogicalSolver(Transformer<T, Q, P, A> transformer, Duration timeout) {
        this(transformer, timeout, DEFAULT_MAX_CONCURRENT_SOLVES);
    }

    public LogicalSolver(Transformer<T, Q, P, A> transformer, Duration timeout, int maxConcurrentSolves) {
        this.transformer = transformer;
        this.timeout = timeout;
        // No queue: a solve either gets a thread now or is turned away. Idle threads end after a while.
        this.executor = new ThreadPoolExecutor(0, maxConcurrentSolves, 30, TimeUnit.SECONDS, new SynchronousQueue<>(), task -> {
            var thread = new Thread(task, "solver-" + THREAD_COUNT.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public ProofDto perform(ProofDto proof) {
        var naturalDeduction = transformer.from(proof);
        Future<?> running;
        try {
            running = executor.submit(naturalDeduction::automate);
        } catch (RejectedExecutionException e) {
            throw new SolverBusyException();
        }
        await(running);
        return transformer.fromProof(naturalDeduction);
    }

    private void await(Future<?> running) {
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
    }
}
