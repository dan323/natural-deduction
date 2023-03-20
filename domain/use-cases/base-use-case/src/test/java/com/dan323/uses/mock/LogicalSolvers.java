package com.dan323.uses.mock;

import com.dan323.proof.generic.proof.Proof;
import com.dan323.uses.LogicalSolver;

import java.util.List;

public final class LogicalSolvers {

    public static List<LogicalSolver> getLogicalSolvers() {
        return List.of(getLogicalSolver("l1"), getLogicalSolver("l2"));
    }

    private static LogicalSolver getLogicalSolver(String logic) {
        return new LogicalSolver() {
            @Override
            public String getLogicName() {
                return logic;
            }

            @Override
            public Proof perform(Proof proof) {
                return proof;
            }
        };
    }
}
