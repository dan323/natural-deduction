package com.dan323.uses.modal;

import com.dan323.model.ActionCategory;
import com.dan323.model.ActionDescriptorDto;

import static com.dan323.model.ActionCategory.ELIMINATION;
import static com.dan323.model.ActionCategory.INTRODUCTION;
import static com.dan323.model.ActionCategory.OTHER;
import static com.dan323.uses.modal.ActionInput.formula;
import static com.dan323.uses.modal.ActionInput.line;
import static com.dan323.uses.modal.ActionInput.state;

/**
 * The actions of modal logic: the rule names understood by {@code ParseModalAction.parseAction} (what a client sends
 * as {@code ActionDto.name}) with the inputs each one takes, in the order {@code parseAction} reads them, and how to
 * present the rule. {@code ParseModalAction} is the source of truth for the names, and a test checks that every entry
 * here builds an action.
 * <p>
 * The constructor takes a label, symbol, category, description and a labelled input per parameter, so a rule cannot be
 * added without describing it. Each {@code symbol} is how the frontend shows the rule text of a proof step (e.g.
 * {@code []I} is shown as {@code □I}), so that a rule has one name in the list of actions and in the proof. States are
 * written as in the formulas: {@code s0 <= s1} says that {@code s1} is reachable from {@code s0}.
 */
public enum AvailableModalAction {
    ASSUME("Ass", "Assumption", "Ass", OTHER,
            "Assume A in state s, or a relation such as s0 <= s1 (which takes no state), opening a new subproof",
            formula("Assumption (A or s0 <= s1)"), state("State of A (e.g. s1)")),
    OR_I1("|I1", "Or introduction (left)", "∨I", INTRODUCTION,
            "From A, derive A ∨ B for any B, in the same state", line("Line with A"), formula("Right side (B)")),
    OR_I2("|I2", "Or introduction (right)", "∨I", INTRODUCTION,
            "From B, derive A ∨ B for any A, in the same state", line("Line with B"), formula("Left side (A)")),
    OR_E("|E", "Or elimination", "∨E", ELIMINATION,
            "From A ∨ B, A → C and B → C, all in the same state, derive C",
            line("Disjunction (A ∨ B)"), line("Implication (A → C)"), line("Implication (B → C)")),
    AND_I("&I", "And introduction", "∧I", INTRODUCTION,
            "From A and B in the same state, derive A ∧ B", line("Line with A"), line("Line with B")),
    AND_E1("&E1", "And elimination (left)", "∧E", ELIMINATION,
            "From A ∧ B, derive A", line("Conjunction (A ∧ B)")),
    AND_E2("&E2", "And elimination (right)", "∧E", ELIMINATION,
            "From A ∧ B, derive B", line("Conjunction (A ∧ B)")),
    COPY("Rep", "Repetition", "Rep", OTHER,
            "Repeat a line that is still available", line("Line to repeat")),
    NOT_E("-E", "Double negation elimination", "¬E", ELIMINATION,
            "From ¬¬A, derive A", line("Double negation (¬¬A)")),
    NOT_I("-I", "Negation introduction", "¬I", INTRODUCTION,
            "Close the last assumption A, which led to ⊥, and derive ¬A"),
    DEDUCTION_THEOREM("->I", "Deduction theorem", "→I", INTRODUCTION,
            "Close the last assumption A, with B the last line, and derive A → B"),
    MODUS_PONENS("->E", "Modus ponens", "→E", ELIMINATION,
            "From A → B and A in the same state, derive B", line("Implication (A → B)"), line("Antecedent (A)")),
    FALSE_E("FE", "Falsum elimination", "⊥E", ELIMINATION,
            "From ⊥, derive any A in any state s",
            line("Falsum (⊥)"), formula("Formula to derive (A)"), state("State of A (e.g. s1)")),
    FALSE_I("FI", "Falsum introduction", "⊥I", INTRODUCTION,
            "From A and ¬A in the same state, derive ⊥", line("Line with A"), line("Negation (¬A)")),
    BOX_I("[]I", "Box introduction", "□I", INTRODUCTION,
            "Close the last assumption s <= t, with t a fresh state and A in state t the last line, and derive □A in state s"),
    BOX_E("[]E", "Box elimination", "□E", ELIMINATION,
            "From □A in state s and s <= t, derive A in state t",
            line("Necessity (□A in state s)"), line("Relation (s <= t)")),
    DIA_I("<>I", "Diamond introduction", "◇I", INTRODUCTION,
            "From A in state t and s <= t, derive ◇A in state s",
            line("Line with A (in state t)"), line("Relation (s <= t)")),
    DIA_E("<>E", "Diamond elimination", "◇E", ELIMINATION,
            "From ◇A in state s, close the last two assumptions s <= t and A in state t, with t a fresh state, and derive the last line C",
            line("Possibility (◇A in state s)")),
    REFLEXIVE("Refl", "Reflexivity", "Refl", OTHER,
            "From any line in state s, derive s <= s", line("Line in state s")),
    TRANSITIVE("Trans", "Transitivity", "Trans", OTHER,
            "From s <= t and t <= u, derive s <= u", line("Relation (s <= t)"), line("Relation (t <= u)"));

    private final ActionDescriptorDto descriptor;

    AvailableModalAction(String ruleName, String label, String symbol, ActionCategory category, String description,
                         ActionInput... inputs) {
        this.descriptor = ActionInput.describe(ruleName, label, symbol, category, description, inputs);
    }

    public ActionDescriptorDto descriptor() {
        return descriptor;
    }
}
