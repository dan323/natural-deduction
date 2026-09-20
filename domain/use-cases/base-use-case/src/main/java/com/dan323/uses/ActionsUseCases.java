package com.dan323.uses;

import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;

import java.util.List;

public interface ActionsUseCases {

    GetActions getActions(String logicName);

    ApplyAction applyAction(String logicName);

    Solve solveProblem(String logicName);

    ParseProof parseToProof(String logic);

    interface GetActions {
        List<ActionDescriptorDto> perform();
    }

    interface ParseProof {
        ProofDto perform(String proofAsString);
    }

    interface ApplyAction {
        ApplyResult perform(ActionDto action, ProofDto proof);
    }

    /**
     * Runs the automatic solver. Fails with {@link SolveTimeoutException} if it takes too long. A proof the solver
     * cannot finish is returned as far as it got, see {@link ProofDto#isDone()}.
     */
    interface Solve {
        ProofDto perform(ProofDto proof);
    }

    /**
     * Outcome of applying an action. When the action was not applicable, {@code proof} is the unchanged proof and
     * {@code message} says why.
     */
    record ApplyResult(ProofDto proof, boolean applied, String message) {

        public static ApplyResult applied(ProofDto proof) {
            return new ApplyResult(proof, true, "");
        }

        public static ApplyResult rejected(ProofDto proof, String message) {
            return new ApplyResult(proof, false, message);
        }
    }
}
