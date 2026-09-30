package com.dan323.uses.modal.nextuntil.test;

import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ConjunctionModal;
import com.dan323.expressions.modal.DisjunctionModal;
import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.expressions.modal.Next;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.modal.Until;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.model.ProofDto;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.modal.nextuntil.ModalNextUntilProofTransformer;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The solver of {@code modal-next-until} against the semantics of linear time, on random formulas over {@code p} and
 * {@code q}: it never proves a formula that has a countermodel ({@link LassoChecker} finds one), and it proves every
 * formula of the sample that has none. The sample is fixed (a seeded {@link Random}), so a change of the solver that
 * loses one of these proofs shows up here.
 */
class LinearTimeSolverSemanticsTest {

    private static final int FORMULAS = 300;
    private static final long SEED = 1;

    private final ModalNextUntilProofTransformer transformer = new ModalNextUntilProofTransformer();
    private final LogicalSolver<?, ?, ?, ?> solver = new LogicalSolver<>(transformer, Duration.ofSeconds(10));
    private final LassoChecker checker = new LassoChecker(List.of("p", "q"), 5);

    private static ModalLogicalOperation random(Random random, int depth) {
        if (depth == 0 || random.nextInt(4) == 0) {
            return new VariableModal(random.nextBoolean() ? "p" : "q");
        }
        return switch (random.nextInt(8)) {
            case 0 -> new NegationModal(random(random, depth - 1));
            case 1 -> new ConjunctionModal(random(random, depth - 1), random(random, depth - 1));
            case 2 -> new DisjunctionModal(random(random, depth - 1), random(random, depth - 1));
            case 3 -> new ImplicationModal(random(random, depth - 1), random(random, depth - 1));
            case 4 -> new Next(random(random, depth - 1));
            case 5 -> new Until(random(random, depth - 1), random(random, depth - 1));
            case 6 -> new Always(random(random, depth - 1));
            default -> new Sometime(random(random, depth - 1));
        };
    }

    @Test
    void theSolverProvesTheValidFormulasAndOnlyThem() {
        var random = new Random(SEED);
        List<String> unsound = new ArrayList<>();
        List<String> missed = new ArrayList<>();
        int valid = 0;
        for (int n = 0; n < FORMULAS; n++) {
            // Implications are more often valid than formulas made at random
            ModalLogicalOperation goal = random.nextBoolean() ? random(random, 3)
                    : new ImplicationModal(random(random, 2), random(random, 2));
            var countermodel = checker.countermodel(List.of(), goal);
            ProofDto solved = solver.perform(new ProofDto(List.of(), "modal-next-until", goal.toString()));
            boolean proved = solved.isDone() && transformer.from(solved).isDone();
            if (countermodel.isPresent() && proved) {
                unsound.add(goal + " (false in " + countermodel.get() + ")");
            } else if (countermodel.isEmpty()) {
                valid++;
                if (!proved) {
                    missed.add(goal.toString());
                }
            }
        }
        assertEquals(List.of(), unsound, "proved, but not valid");
        assertEquals(List.of(), missed, "valid, but not proved");
        assertTrue(valid >= 25, "the sample has valid formulas: " + valid);
    }
}
