package com.dan323.uses.firstorder;

import com.dan323.model.ActionCategory;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ParamKind;
import com.dan323.proof.firstorder.FirstOrderEqualsE;
import com.dan323.proof.firstorder.FirstOrderEqualsI;
import com.dan323.proof.firstorder.FirstOrderExistsE;
import com.dan323.proof.firstorder.FirstOrderExistsI;
import com.dan323.proof.firstorder.FirstOrderForallE;
import com.dan323.proof.firstorder.FirstOrderForallI;

import java.util.List;

import static com.dan323.model.ActionCategory.ELIMINATION;
import static com.dan323.model.ActionCategory.INTRODUCTION;
import static com.dan323.model.ActionCategory.OTHER;

/**
 * The actions of first-order logic: the rule names understood by {@code ParseFirstOrderAction.parseAction} (what a
 * client sends as {@code ActionDto.name}) with the inputs each one takes, in the order {@code parseAction} reads them,
 * and how to present the rule. The 14 rules shared with classical and modal logic have the modal names; the classical
 * ones ({@code COPY} for {@code Rep}, ...) are accepted too.
 * <p>
 * The constructor takes a label, symbol, category, description and a labelled input per parameter, so a rule cannot be
 * added without describing it. Each {@code symbol} is how the frontend shows the rule text of a proof step. A term
 * ({@link ParamKind#TERM}) is sent as {@code extraParameters.term}; the targets of {@code ∀I}, {@code ∃I} and
 * {@code =E} are formulas ({@link ParamKind#EXPRESSION}).
 */
public enum AvailableFirstOrderAction {
    ASSUME("Ass", "Assumption", "Ass", OTHER,
            "Assume A, opening a new subproof", formula("Assumption (A)")),
    OR_I1("|I1", "Or introduction (left)", "∨I", INTRODUCTION,
            "From A, derive A ∨ B for any B", line(Labels.LINE_WITH_A), formula("Right side (B)")),
    OR_I2("|I2", "Or introduction (right)", "∨I", INTRODUCTION,
            "From B, derive A ∨ B for any A", line("Line with B"), formula("Left side (A)")),
    OR_E("|E", "Or elimination", "∨E", ELIMINATION,
            "From A ∨ B, A → C and B → C, derive C",
            line("Disjunction (A ∨ B)"), line("Implication (A → C)"), line("Implication (B → C)")),
    AND_I("&I", "And introduction", "∧I", INTRODUCTION,
            "From A and B, derive A ∧ B", line(Labels.LINE_WITH_A), line("Line with B")),
    AND_E1("&E1", "And elimination (left)", "∧E", ELIMINATION,
            "From A ∧ B, derive A", line(Labels.CONJUNCTION)),
    AND_E2("&E2", "And elimination (right)", "∧E", ELIMINATION,
            "From A ∧ B, derive B", line(Labels.CONJUNCTION)),
    COPY("Rep", "Repetition", "Rep", OTHER,
            "Repeat a line that is still available", line("Line to repeat")),
    NOT_E("-E", "Double negation elimination", "¬E", ELIMINATION,
            "From ¬¬A, derive A", line("Double negation (¬¬A)")),
    NOT_I("-I", "Negation introduction", "¬I", INTRODUCTION,
            "Close the last assumption A, which led to ⊥, and derive ¬A"),
    DEDUCTION_THEOREM("->I", "Deduction theorem", "→I", INTRODUCTION,
            "Close the last assumption A, with B the last line, and derive A → B"),
    MODUS_PONENS("->E", "Modus ponens", "→E", ELIMINATION,
            "From A → B and A, derive B", line("Implication (A → B)"), line("Antecedent (A)")),
    FALSE_E("FE", "Falsum elimination", "⊥E", ELIMINATION,
            "From ⊥, derive any A", line("Falsum (⊥)"), formula("Formula to derive (A)")),
    FALSE_I("FI", "Falsum introduction", "⊥I", INTRODUCTION,
            "From A and ¬A, derive ⊥", line(Labels.LINE_WITH_A), line("Negation (¬A)")),
    FORALL_I(FirstOrderForallI.NAME, "For-all introduction", "∀I", INTRODUCTION,
            "From A[x:=a], with a name a that is free in no premise and no open assumption, derive ∀x.A",
            line("Line with A[x:=a]"), formula("Target (∀x.A)")),
    FORALL_E(FirstOrderForallE.NAME, "For-all elimination", "∀E", ELIMINATION,
            "From ∀x.A and a term t, derive A[x:=t]", line("Universal (∀x.A)"), term("Term (t)")),
    EXISTS_I(FirstOrderExistsI.NAME, "Exists introduction", "∃I", INTRODUCTION,
            "From A[x:=t] for some term t, derive ∃x.A", line("Line with A[x:=t]"), formula("Target (∃x.A)")),
    EXISTS_E(FirstOrderExistsE.NAME, "Exists elimination", "∃E", ELIMINATION,
            "From ∃x.A, close the last assumption A[x:=a], with a a fresh name and C the last line, and derive C",
            line("Existential (∃x.A)")),
    EQUALS_I(FirstOrderEqualsI.NAME, "Equality introduction", "=I", INTRODUCTION,
            "For a term t, derive t = t", term("Term (t)")),
    EQUALS_E(FirstOrderEqualsE.NAME, "Equality elimination", "=E", ELIMINATION,
            "From s = t and A, derive A with some occurrences of s replaced by t",
            line("Equation (s = t)"), line(Labels.LINE_WITH_A), formula("Target (A with s replaced by t)"));

    private final ActionDescriptorDto descriptor;

    /** Input labels that several actions share. */
    private static final class Labels {
        static final String LINE_WITH_A = "Line with A";
        static final String CONJUNCTION = "Conjunction (A ∧ B)";

        private Labels() {
        }
    }

    /** An input of an action: what kind of value it is, and how to call it. */
    private record Input(ParamKind kind, String label) {
    }

    private static Input line(String label) {
        return new Input(ParamKind.INT, label);
    }

    private static Input formula(String label) {
        return new Input(ParamKind.EXPRESSION, label);
    }

    private static Input term(String label) {
        return new Input(ParamKind.TERM, label);
    }

    AvailableFirstOrderAction(String ruleName, String label, String symbol, ActionCategory category, String description,
                              Input... inputs) {
        var inputList = List.of(inputs);
        this.descriptor = new ActionDescriptorDto(ruleName, inputList.stream().map(Input::kind).toList(), label, symbol,
                category, description, inputList.stream().map(Input::label).toList());
    }

    public ActionDescriptorDto descriptor() {
        return descriptor;
    }
}
