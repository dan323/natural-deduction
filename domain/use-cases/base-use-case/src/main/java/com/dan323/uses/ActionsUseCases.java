package com.dan323.uses;

import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;

import java.util.List;

public interface ActionsUseCases {

    GetActions getActions(String logicName);

    ApplyAction applyAction(String logicName);

    Solve solveProblem(String logicName);

    ParseProof parseToProof(String logic);

    interface GetActions {
        List<String> perform();
    }

    interface ParseProof {
        ProofDto perform(String proofAsString);
    }

    interface ApplyAction {
        ProofDto perform(ActionDto action, ProofDto proof);
    }

    interface Solve {
        ProofDto perform(ProofDto proof);
    }
}
