package com.dan323.uses.modal.nextuntil;

import com.dan323.model.ActionCategory;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.proof.modal.nextuntil.ParseModalNextUntilAction;
import com.dan323.uses.modal.ActionInput;

import static com.dan323.model.ActionCategory.ELIMINATION;
import static com.dan323.model.ActionCategory.INTRODUCTION;
import static com.dan323.model.ActionCategory.OTHER;
import static com.dan323.uses.modal.ActionInput.formula;
import static com.dan323.uses.modal.ActionInput.line;

/**
 * The actions that {@code modal-next-until} adds to those of modal logic ({@code AvailableModalAction}): the names that
 * {@link ParseModalNextUntilAction#parseAction} reads, described as {@code AvailableModalAction} describes its own.
 * Each {@code symbol} is how the frontend shows the rule text of the step it adds ({@code UI} for both Until
 * introductions). {@code s+1} is the state after {@code s}.
 */
public enum AvailableNextUntilAction {
    NEXT_I(ParseModalNextUntilAction.NEXT_I, "Next introduction", "XI", INTRODUCTION,
            "From A in state s+1, derive X A in state s", line("Line with A (in state s+1)")),
    NEXT_E(ParseModalNextUntilAction.NEXT_E, "Next elimination", "XE", ELIMINATION,
            "From X A in state s, derive A in state s+1", line("Next (X A in state s)")),
    SUCCESSOR(ParseModalNextUntilAction.SUCCESSOR, "Successor", "Succ", OTHER,
            "From any line in state s, derive s <= s+1", line("Line in state s")),
    UNTIL_I1(ParseModalNextUntilAction.UNTIL_I1, "Until introduction (now)", "UI", INTRODUCTION,
            "From B in state s, derive A U B in state s for any A", line("Line with B"), formula("Left side (A)")),
    UNTIL_I2(ParseModalNextUntilAction.UNTIL_I2, "Until introduction (later)", "UI", INTRODUCTION,
            "From A and X (A U B) in the same state, derive A U B", line("Line with A"), line("Next (X (A U B))")),
    UNTIL_E(ParseModalNextUntilAction.UNTIL_E, "Until elimination", "UE", ELIMINATION,
            "From A U B in state s, derive B ∨ (A ∧ X (A U B)) in state s", line("Until (A U B)")),
    UNTIL_SOMETIME(ParseModalNextUntilAction.UNTIL_SOMETIME, "Until reaches its goal", "U◇", ELIMINATION,
            "From A U B in state s, derive ◇B in state s", line("Until (A U B)")),
    INDUCTION(ParseModalNextUntilAction.INDUCTION, "Induction", "Ind", INTRODUCTION,
            "From A and □(A → X A) in the same state, derive □A", line("Line with A"), line("Step (□(A → X A))"));

    private final ActionDescriptorDto descriptor;

    AvailableNextUntilAction(String ruleName, String label, String symbol, ActionCategory category, String description,
                             ActionInput... inputs) {
        this.descriptor = ActionInput.describe(ruleName, label, symbol, category, description, inputs);
    }

    public ActionDescriptorDto descriptor() {
        return descriptor;
    }
}
