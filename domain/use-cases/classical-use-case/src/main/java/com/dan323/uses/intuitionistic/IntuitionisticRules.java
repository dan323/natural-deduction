package com.dan323.uses.intuitionistic;

import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.AvailableAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.uses.InvalidActionException;
import com.dan323.uses.InvalidProofException;

import java.util.List;

/**
 * The rule filter of intuitionistic logic: its proofs are classical proofs that only use the rules for which
 * {@link AvailableAction#isIntuitionistic()} holds, i.e. every rule but double negation elimination.
 */
final class IntuitionisticRules {

    static final String LOGIC = "intuitionistic";

    private IntuitionisticRules() {
    }

    /**
     * Rejects an action name that is a classical rule without being an intuitionistic one. Other names are left to
     * the classical parsing, which rejects the unknown ones.
     */
    static void checkActionName(String name) {
        for (AvailableAction action : AvailableAction.values()) {
            if (action.name().equals(name) && !action.isIntuitionistic()) {
                throw new InvalidActionException("Rule " + name + " is not a rule of intuitionistic logic");
            }
        }
    }

    /**
     * Rejects a (classically valid) proof that has a step justified by a classical-only rule, naming its first such
     * line (1-based).
     */
    static void checkProof(NaturalDeduction proof) {
        List<ClassicalAction> actions = proof.parse();
        for (int i = 0; i < actions.size(); i++) {
            var action = actions.get(i).getAction();
            if (!action.isIntuitionistic()) {
                throw new InvalidProofException("Line " + (i + 1) + " is not valid: '" + proof.getSteps().get(i).getProof()
                        + "' is not a rule of intuitionistic logic");
            }
        }
    }
}
