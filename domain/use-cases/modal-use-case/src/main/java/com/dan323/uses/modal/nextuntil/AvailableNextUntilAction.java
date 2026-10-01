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
import static com.dan323.uses.modal.ActionInput.state;

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
            "From A and X (A U B) in the same state, derive A U B", line(Labels.LINE_WITH_A), line("Next (X (A U B))")),
    UNTIL_E(ParseModalNextUntilAction.UNTIL_E, "Until elimination", "UE", ELIMINATION,
            "From A U B in state s, derive B ∨ (A ∧ X (A U B)) in state s", line(Labels.UNTIL)),
    UNTIL_SOMETIME(ParseModalNextUntilAction.UNTIL_SOMETIME, "Until reaches its goal", "U◇", ELIMINATION,
            "From A U B in state s, derive ◇B in state s", line(Labels.UNTIL)),
    INDUCTION(ParseModalNextUntilAction.INDUCTION, "Induction", "Ind", INTRODUCTION,
            "From A and □(A → X A) in the same state, derive □A", line(Labels.LINE_WITH_A), line("Step (□(A → X A))")),
    NEGATED_UNTIL(ParseModalNextUntilAction.NEGATED_UNTIL, "Negated Until", "¬U", ELIMINATION,
            "From ¬(A U B) in state s, derive □¬B ∨ (¬B U (¬A ∧ ¬B)) in state s", line("Negated Until (¬(A U B))")),
    UNTIL_WITNESS(ParseModalNextUntilAction.UNTIL_WITNESS, "Until witness", "UW", ELIMINATION,
            "From A U B in state s, derive s <= t for a new state t: the state where B is reached",
            line(Labels.UNTIL), state("New state (t)")),
    UNTIL_WITNESS_RIGHT(ParseModalNextUntilAction.UNTIL_WITNESS_RIGHT, "Until witness reaches B", "UB", ELIMINATION,
            "From the UW line s <= t of A U B, derive B in state t", line(Labels.WITNESS)),
    UNTIL_WITNESS_LEFT(ParseModalNextUntilAction.UNTIL_WITNESS_LEFT, "Until holds before its witness", "UA", ELIMINATION,
            "From the UW line s <= t of A U B, derive A in a state k when the relations give s <= k and k+1 <= t",
            line(Labels.WITNESS), state("State k")),
    ORDER(ParseModalNextUntilAction.ORDER, "Order", "Ord", OTHER,
            "Derive u <= v when the relations give it in linear time (s <= s+1, s <= t gives s+1 <= t+1, ...), or FALSE when they contradict each other",
            formula("Relation (u <= v) or FALSE")),
    EQUAL(ParseModalNextUntilAction.EQUAL, "Equal states", "Eq", OTHER,
            "From A in state u, derive A in state v when the relations give u <= v and v <= u", line(Labels.LINE_WITH_A), state("State v")),
    LINEARITY(ParseModalNextUntilAction.LINEARITY, "Linearity", "Lin", OTHER,
            "Close the subproof that assumes u <= v and ends in FALSE, and derive v+1 <= u: time is a line");

    /** Input labels that several actions share. */
    private static final class Labels {
        private static final String LINE_WITH_A = "Line with A";
        private static final String UNTIL = "Until (A U B)";
        private static final String WITNESS = "Witness (UW line)";
    }

    private final ActionDescriptorDto descriptor;

    AvailableNextUntilAction(String ruleName, String label, String symbol, ActionCategory category, String description,
                             ActionInput... inputs) {
        this.descriptor = ActionInput.describe(ruleName, label, symbol, category, description, inputs);
    }

    public ActionDescriptorDto descriptor() {
        return descriptor;
    }
}
