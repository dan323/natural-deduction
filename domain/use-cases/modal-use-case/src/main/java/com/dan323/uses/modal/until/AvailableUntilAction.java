package com.dan323.uses.modal.until;

import com.dan323.model.ActionCategory;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.proof.modal.until.ParseModalUntilAction;
import com.dan323.uses.modal.ActionInput;

import static com.dan323.model.ActionCategory.ELIMINATION;
import static com.dan323.model.ActionCategory.INTRODUCTION;
import static com.dan323.uses.modal.ActionInput.formula;
import static com.dan323.uses.modal.ActionInput.line;

/**
 * The actions that modal logic with Until adds to those of modal logic ({@code AvailableModalAction}): the names that
 * {@link ParseModalUntilAction#parseAction} reads, described as {@code AvailableModalAction} describes its own. Each
 * {@code symbol} is the rule text of the step it adds ({@code UI} or {@code UE}).
 */
public enum AvailableUntilAction {
    UNTIL_I1(ParseModalUntilAction.UNTIL_I1, "Until introduction (now)", "UI", INTRODUCTION,
            "From B in state s, derive A U B in state s for any A", line("Line with B"), formula("Left side (A)")),
    UNTIL_I2(ParseModalUntilAction.UNTIL_I2, "Until introduction (later)", "UI", INTRODUCTION,
            "From □A and ◇B in the same state, derive A U B", line("Necessity (□A)"), line("Possibility (◇B)")),
    UNTIL_E1(ParseModalUntilAction.UNTIL_E1, "Until elimination (eventually)", "UE", ELIMINATION,
            "From A U B, derive ◇B in the same state", line("Until (A U B)")),
    UNTIL_E2(ParseModalUntilAction.UNTIL_E2, "Until elimination (not yet)", "UE", ELIMINATION,
            "From A U B and ¬B in the same state, derive A", line("Until (A U B)"), line("Negation (¬B)"));

    private final ActionDescriptorDto descriptor;

    AvailableUntilAction(String ruleName, String label, String symbol, ActionCategory category, String description,
                         ActionInput... inputs) {
        this.descriptor = ActionInput.describe(ruleName, label, symbol, category, description, inputs);
    }

    public ActionDescriptorDto descriptor() {
        return descriptor;
    }
}
