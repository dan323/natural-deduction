package com.dan323.uses.classical;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.model.ActionCategory;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ParamKind;
import com.dan323.uses.LogicalGetActions;

import java.util.Arrays;
import java.util.List;

import static com.dan323.model.ActionCategory.ELIMINATION;
import static com.dan323.model.ActionCategory.INTRODUCTION;
import static com.dan323.model.ActionCategory.OTHER;
import static com.dan323.model.ParamKind.EXPRESSION;
import static com.dan323.model.ParamKind.INT;

/**
 * The actions of classical logic: one per {@link AvailableAction}, built once. The parameters mirror the constructor of
 * the action, see {@code ParseClassicalAction.parseAction}, and their labels follow the same order. Each {@code symbol}
 * is how the frontend shows the rule text of a proof step (e.g. {@code ->E} is shown as {@code →E}), so that a rule
 * has one name in the list of actions and in the proof.
 */
public class ClassicGetActions implements LogicalGetActions {

    private static final String LINE_WITH_A = "Line with A";

    private final List<ActionDescriptorDto> actions = Arrays.stream(AvailableAction.values())
            .map(ClassicGetActions::describe)
            .toList();

    @Override
    public List<ActionDescriptorDto> perform() {
        return actions;
    }

    @Override
    public String getLogicName() {
        return "classical";
    }

    /** An input of an action: what kind of value it is, and how to call it. */
    private record Input(ParamKind kind, String label) {
    }

    private static Input line(String label) {
        return new Input(INT, label);
    }

    private static Input formula(String label) {
        return new Input(EXPRESSION, label);
    }

    private static ActionDescriptorDto rule(AvailableAction action, String label, String symbol, ActionCategory category,
                                            String description, Input... inputs) {
        var inputList = List.of(inputs);
        return new ActionDescriptorDto(action.name(), inputList.stream().map(Input::kind).toList(), label, symbol, category,
                description, inputList.stream().map(Input::label).toList());
    }

    // No default branch: adding an AvailableAction does not compile until it is described here.
    private static ActionDescriptorDto describe(AvailableAction action) {
        return switch (action) {
            case ASSUME -> rule(action, "Assumption", "Ass", OTHER,
                    "Assume A, opening a new subproof", formula("Assumption (A)"));
            case ORI1 -> rule(action, "Or introduction (left)", "∨I", INTRODUCTION,
                    "From A, derive A ∨ B for any B", line(LINE_WITH_A), formula("Right side (B)"));
            case ORI2 -> rule(action, "Or introduction (right)", "∨I", INTRODUCTION,
                    "From B, derive A ∨ B for any A", line("Line with B"), formula("Left side (A)"));
            case ORE -> rule(action, "Or elimination", "∨E", ELIMINATION,
                    "From A ∨ B, A → C and B → C, derive C",
                    line("Disjunction (A ∨ B)"), line("Implication (A → C)"), line("Implication (B → C)"));
            case ANDI -> rule(action, "And introduction", "∧I", INTRODUCTION,
                    "From A and B, derive A ∧ B", line(LINE_WITH_A), line("Line with B"));
            case ANDE1 -> rule(action, "And elimination (left)", "∧E", ELIMINATION,
                    "From A ∧ B, derive A", line("Conjunction (A ∧ B)"));
            case ANDE2 -> rule(action, "And elimination (right)", "∧E", ELIMINATION,
                    "From A ∧ B, derive B", line("Conjunction (A ∧ B)"));
            case COPY -> rule(action, "Repetition", "Rep", OTHER,
                    "Repeat a line that is still available", line("Line to repeat"));
            case NOTE -> rule(action, "Double negation elimination", "¬E", ELIMINATION,
                    "From ¬¬A, derive A", line("Double negation (¬¬A)"));
            case NOTI -> rule(action, "Negation introduction", "¬I", INTRODUCTION,
                    "Close the last assumption A, which led to ⊥, and derive ¬A");
            case DT -> rule(action, "Deduction theorem", "→I", INTRODUCTION,
                    "Close the last assumption A, with B the last line, and derive A → B");
            case MP -> rule(action, "Modus ponens", "→E", ELIMINATION,
                    "From A → B and A, derive B", line("Implication (A → B)"), line("Antecedent (A)"));
            case FE -> rule(action, "Falsum elimination", "⊥E", ELIMINATION,
                    "From ⊥, derive any A", line("Falsum (⊥)"), formula("Formula to derive (A)"));
            case FI -> rule(action, "Falsum introduction", "⊥I", INTRODUCTION,
                    "From A and ¬A, derive ⊥", line(LINE_WITH_A), line("Negation (¬A)"));
        };
    }
}
