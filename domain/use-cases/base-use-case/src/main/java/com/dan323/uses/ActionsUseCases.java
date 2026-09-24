package com.dan323.uses;

import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ExerciseDto;
import com.dan323.model.ProofDto;

import java.util.List;

public interface ActionsUseCases {

    GetActions getActions(String logicName);

    ApplyAction applyAction(String logicName);

    Solve solveProblem(String logicName);

    ParseProof parseToProof(String logic);

    GetExercises getExercises(String logicName);

    interface GetActions {
        List<ActionDescriptorDto> perform();
    }

    /**
     * The logic's exercise catalog, without the reference solutions. Empty for a known logic that has no catalog.
     */
    interface GetExercises {
        List<ExerciseDto> perform();
    }

    interface ParseProof {
        ProofDto perform(String proofAsString);
    }

    interface ApplyAction {
        ApplyResult perform(ActionDto action, ProofDto proof);
    }

    /**
     * Runs the automatic solver. Fails with {@link SolveTimeoutException} if it takes too long. A proof the solver
     * cannot finish is returned as far as it got, see {@link ProofDto#isDone()}. Fails with {@link NoSolverException}
     * for a logic without a solver of its own.
     */
    interface Solve {
        ProofDto perform(ProofDto proof);
    }

    /**
     * Outcome of applying an action. When the action was not applicable, {@code proof} is the replayed proof without
     * the action (serialized by the domain, so its assumption levels follow from the rules, whatever levels the request
     * sent) and {@code message} says why. {@code done} is the domain's verdict on {@code proof}, whether its goal is proved.
     */
    record ApplyResult(ProofDto proof, boolean applied, boolean done, String message) {

        public static ApplyResult applied(ProofDto proof, boolean done) {
            return new ApplyResult(proof, true, done, "");
        }

        public static ApplyResult rejected(ProofDto proof, boolean done, String message) {
            return new ApplyResult(proof, false, done, message);
        }
    }
}
