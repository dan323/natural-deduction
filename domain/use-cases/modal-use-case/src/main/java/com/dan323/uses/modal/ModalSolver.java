package com.dan323.uses.modal;

import com.dan323.model.ProofDto;
import com.dan323.uses.LogicalSolver;

public class ModalSolver implements LogicalSolver {
    @Override
    public ProofDto perform(ProofDto proof) {
        throw new UnsupportedOperationException("This is not yet implemented");
    }

    @Override
    public String getLogicName() {
        return "modal";
    }
}
