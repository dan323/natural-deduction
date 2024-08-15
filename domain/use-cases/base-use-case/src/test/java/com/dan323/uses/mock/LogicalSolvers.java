package com.dan323.uses.mock;

import com.dan323.model.ProofDto;
import com.dan323.uses.LogicalSolver;

import java.util.List;

public final class LogicalSolvers {

    public static List<LogicalSolver> getLogicalSolvers() {
        return List.of(getLogicalSolver("l1"), getLogicalSolver("l2"));
    }

    private static LogicalSolver getLogicalSolver(String logic) {
        return new LogicalSolver() {
            @Override
            public ProofDto perform(ProofDto proof) {
                return proof;
            }

            @Override
            public String getLogicName() {
                return logic;
            }

        };
    }
}
