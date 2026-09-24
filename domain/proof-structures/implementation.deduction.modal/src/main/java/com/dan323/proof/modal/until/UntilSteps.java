package com.dan323.proof.modal.until;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStepSupplier;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.List;

/**
 * What the Until rules share: they read formulas (not relations) of available lines that are all in one state, and
 * derive a formula in that same state.
 */
final class UntilSteps {

    static final String UNTIL_I = "UI";
    static final String UNTIL_E = "UE";

    private UntilSteps() {
    }

    /**
     * @return whether line {@code line} (1-based) exists, is still available and holds a formula of type {@code type}
     */
    static boolean isFormula(ModalNaturalDeduction pf, int line, Class<? extends ModalLogicalOperation> type) {
        return RuleUtils.isValidIndexAndProp(pf, line) && type.isInstance(pf.getSteps().get(line - 1).getStep());
    }

    static <T extends ModalLogicalOperation> T formula(ModalNaturalDeduction pf, int line, Class<T> type) {
        return type.cast(pf.getSteps().get(line - 1).getStep());
    }

    static boolean sameState(ModalNaturalDeduction pf, int line1, int line2) {
        return pf.getSteps().get(line1 - 1).getState().equals(pf.getSteps().get(line2 - 1).getState());
    }

    /**
     * Adds {@code conclusion}, justified by {@code rule} on {@code lines}, at the current assumption level.
     */
    static void add(ModalNaturalDeduction pf, ProofStepSupplier<ModalOperation, ProofStepModal> supplier,
                    ModalLogicalOperation conclusion, String rule, List<Integer> lines) {
        pf.getSteps().add(supplier.generateProofStep(RuleUtils.getLastAssumptionLevel(pf), conclusion,
                new ProofReason(rule, List.of(), lines)));
    }

    /**
     * @return a supplier of steps in the state of line {@code line}
     */
    static ProofStepSupplier<ModalOperation, ProofStepModal> inStateOf(ModalNaturalDeduction pf, int line) {
        String state = pf.getSteps().get(line - 1).getState();
        return (assLevel, log, reason) -> new ProofStepModal(state, assLevel, (ModalLogicalOperation) log, reason);
    }
}
