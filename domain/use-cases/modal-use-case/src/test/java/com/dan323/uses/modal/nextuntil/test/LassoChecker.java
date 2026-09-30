package com.dan323.uses.modal.nextuntil.test;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ConjunctionModal;
import com.dan323.expressions.modal.ConstantModal;
import com.dan323.expressions.modal.DisjunctionModal;
import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.expressions.modal.Next;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.modal.Until;
import com.dan323.expressions.modal.VariableModal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Looks for a countermodel of a {@code modal-next-until} formula among the small models of linear time: a lasso, a
 * finite prefix of states followed by a loop back to one of them. Every satisfiable formula of linear time has such a
 * model, though possibly a longer one than this checker tries, so "no countermodel found" only means "probably valid",
 * while a countermodel found is a real one.
 */
final class LassoChecker {

    private final List<String> variables;
    private final int maxLength;

    /**
     * @param variables the propositional variables the formulas use
     * @param maxLength the longest lasso tried
     */
    LassoChecker(List<String> variables, int maxLength) {
        this.variables = List.copyOf(variables);
        this.maxLength = maxLength;
    }

    /** @return a countermodel of {@code premises ⊢ goal} at position 0, described as text, if one is found */
    Optional<String> countermodel(List<LogicOperation> premises, LogicOperation goal) {
        for (int length = 1; length <= maxLength; length++) {
            int valuations = 1 << (variables.size() * length);
            for (int loop = 0; loop < length; loop++) {
                for (int valuation = 0; valuation < valuations; valuation++) {
                    var model = new Lasso(length, loop, valuation);
                    if (premises.stream().allMatch(premise -> model.holds(premise, 0)) && !model.holds(goal, 0)) {
                        return Optional.of(model.toString());
                    }
                }
            }
        }
        return Optional.empty();
    }

    private final class Lasso {
        private final int length;
        private final int loop;
        private final int valuation;

        private Lasso(int length, int loop, int valuation) {
            this.length = length;
            this.loop = loop;
            this.valuation = valuation;
        }

        private int next(int position) {
            return position + 1 < length ? position + 1 : loop;
        }

        /** The positions from {@code position} on, each once, in the order time reaches them. */
        private List<Integer> future(int position) {
            List<Integer> positions = new ArrayList<>();
            int current = position;
            while (!positions.contains(current)) {
                positions.add(current);
                current = next(current);
            }
            return positions;
        }

        private boolean variable(String name, int position) {
            int index = variables.indexOf(name);
            return (valuation >> (position * variables.size() + index) & 1) == 1;
        }

        boolean holds(LogicOperation formula, int position) {
            return switch (formula) {
                case VariableModal variable -> variable(variable.toString(), position);
                case ConstantModal constant -> constant.getValue();
                case NegationModal negation -> !holds(negation.getElement(), position);
                case ConjunctionModal conj -> holds(conj.getLeft(), position) && holds(conj.getRight(), position);
                case DisjunctionModal disj -> holds(disj.getLeft(), position) || holds(disj.getRight(), position);
                case ImplicationModal imp -> !holds(imp.getLeft(), position) || holds(imp.getRight(), position);
                case Next next -> holds(next.getElement(), next(position));
                case Always always -> future(position).stream().allMatch(p -> holds(always.getElement(), p));
                case Sometime sometime -> future(position).stream().anyMatch(p -> holds(sometime.getElement(), p));
                case Until until -> {
                    for (int p : future(position)) {
                        if (holds(until.getRight(), p)) {
                            yield true;
                        }
                        if (!holds(until.getLeft(), p)) {
                            yield false;
                        }
                    }
                    yield false;
                }
                default -> throw new IllegalArgumentException("Not a formula of linear time: " + formula);
            };
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int position = 0; position < length; position++) {
                sb.append(position == loop ? "(" : "").append('{');
                List<String> trueOnes = new ArrayList<>();
                for (String variable : variables) {
                    if (variable(variable, position)) {
                        trueOnes.add(variable);
                    }
                }
                sb.append(String.join(",", trueOnes)).append('}');
            }
            return sb.append(")^ω").toString();
        }
    }
}
