package com.dan323.proof.modal.internal;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.base.UnaryOperation;
import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.Next;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.modal.Until;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.expressions.relation.StateTerm;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.nextuntil.ModalNextE;
import com.dan323.proof.modal.nextuntil.ModalNextI;
import com.dan323.proof.modal.nextuntil.ModalSuccessor;
import com.dan323.proof.modal.nextuntil.ModalUntilE;
import com.dan323.proof.modal.nextuntil.ModalUntilI1;
import com.dan323.proof.modal.nextuntil.ModalUntilI2;
import com.dan323.proof.modal.nextuntil.ModalUntilSometime;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The automatic solver of {@code modal-next-until}: the modal solver ({@link ModalAutomate}) plus the Next and Until
 * rules. Every rule it applies is one of that logic's rules, so its proofs replay like any other.
 *
 * <ul>
 *     <li>A goal {@code X A} in state {@code s} becomes the goal {@code A} in {@code s+1}, reached with {@code XI}.
 *     Nothing is lost: {@code X A} holds in {@code s} exactly when {@code A} holds in {@code s+1}.</li>
 *     <li>A goal {@code A U B} in {@code s} is reached with {@code UI} ({@code UI1}) from {@code B} in {@code s}, or
 *     ({@code UI2}) from {@code A} in {@code s} and {@code X (A U B)} in {@code s}, that is {@code A U B} in
 *     {@code s+1}. When neither is at hand it makes attempts, in this order: reach {@code B} in {@code s}; reach
 *     {@code A} in {@code s} and {@code A U B} in {@code s+1}. An attempt that gets stuck has every step it added
 *     removed, and the next one is tried; when both get stuck, so does the goal, and the attempt it belongs to.</li>
 *     <li>Eliminations: {@code XE} ({@code X A} in {@code s} gives {@code A} in {@code s+1}), {@code U<>} and
 *     {@code UE} on an {@code A U B}, and {@code Succ} ({@code s <= s+1}, so that {@code []E} and {@code <>I} reach
 *     {@code s+1}) when {@code s+1} is the state of a goal or of a step, the relation is not there yet and some step is
 *     a {@code []} or some goal a {@code <>}.</li>
 *     <li>An elimination rule, modal or not, is applied only when it adds a step that is not there yet in the same
 *     state (see {@link #isUsefulElimination}).</li>
 * </ul>
 * It does not use {@code Ind}: induction needs an invariant, which the solver cannot guess, so a goal that only
 * induction proves (such as {@code p, [] (p -> X p) ⊢ [] p}) is left unproved.
 *
 * <p>It always stops. No step or goal is ever more successors away from its base state
 * ({@code s0+3} is 3 away from {@code s0}) than there are {@code X} and {@code U} in the premises and the goal (or
 * one, if there are none); and the proof and the goals together are kept below {@link #STEPS_PER_SYMBOL} times the size of the premises and
 * the goal, a round that would go further counting as stuck. When it finds no proof, it leaves the proof with its
 * premises only.
 */
public final class ModalNextUntilAutomate extends ModalAutomate {

    /**
     * How many steps and goals the proof may have for each symbol of its premises and goal.
     */
    static final int STEPS_PER_SYMBOL = 20;

    /**
     * An attempt at an Until goal: the state to go back to when it fails, and the subgoals of the ways still to try.
     */
    private record Attempt(State before, List<List<Map.Entry<String, ModalOperation>>> remaining) {
    }

    private final Deque<Attempt> attempts = new ArrayDeque<>();
    private int maxSize;
    private int maxOffset;

    /**
     * A solver keeps its working state in fields: use one instance per proof to solve.
     */
    public ModalNextUntilAutomate() {
        // Nothing to set up: automate initializes the state
    }

    @Override
    protected void started() {
        attempts.clear();
        int symbols = size(proof().getGoal());
        int temporal = temporal(proof().getGoal());
        for (ModalOperation premise : proof().getAssms()) {
            symbols += size(premise);
            temporal += temporal(premise);
        }
        maxSize = STEPS_PER_SYMBOL * symbols;
        maxOffset = Math.max(1, temporal);
    }

    private static int size(LogicOperation formula) {
        if (formula instanceof UnaryOperation<?> unary) {
            return 1 + size(unary.getElement());
        } else if (formula instanceof BinaryOperation<?> binary) {
            return 1 + size(binary.getLeft()) + size(binary.getRight());
        }
        return 1;
    }

    /**
     * @return how many {@code X} and {@code U} the formula has
     */
    private static int temporal(LogicOperation formula) {
        int own = formula instanceof Next || formula instanceof Until ? 1 : 0;
        if (formula instanceof UnaryOperation<?> unary) {
            return own + temporal(unary.getElement());
        } else if (formula instanceof BinaryOperation<?> binary) {
            return own + temporal(binary.getLeft()) + temporal(binary.getRight());
        }
        return own;
    }

    @Override
    protected boolean withinBounds() {
        return proof().getSteps().size() + goalCount() <= maxSize;
    }

    /**
     * @return the successor of {@code state} when the solver may use it, empty otherwise
     */
    private Optional<String> successor(String state) {
        try {
            var term = StateTerm.parse(state);
            if (term.hasSuccessor() && term.offset() < maxOffset) {
                return Optional.of(term.successor().toString());
            }
        } catch (IllegalArgumentException e) {
            // Not a state term: it has no successor
        }
        return Optional.empty();
    }

    /**
     * {@code XI} for a goal {@code X A}, and {@code UI1} or {@code UI2} for a goal {@code A U B}, from the valid steps.
     */
    @Override
    protected Optional<AbstractModalAction> introRuleForOtherGoal(ModalOperation goal, String state) {
        Optional<AbstractModalAction> action = Optional.empty();
        if (goal instanceof Next next) {
            action = successor(state)
                    .map(succ -> validStep(next.getElement(), succ))
                    .filter(line -> line > 0)
                    .map(ModalNextI::new);
        } else if (goal instanceof Until until) {
            int right = validStep(until.getRight(), state);
            if (right > 0) {
                action = Optional.of(new ModalUntilI1(right, (ModalLogicalOperation) until.getLeft()));
            } else {
                int left = validStep(until.getLeft(), state);
                int next = validStep(new Next(until), state);
                if (left > 0 && next > 0) {
                    action = Optional.of(new ModalUntilI2(left, next));
                }
            }
        }
        return action.filter(act -> act.isValid(proof()));
    }

    /**
     * The 1-based line of a valid step with this formula in this state, or 0 if there is none.
     */
    private int validStep(ModalOperation formula, String state) {
        List<ProofStepModal> steps = proof().getSteps();
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).isValid() && steps.get(i).getStep().equals(formula) && state.equals(steps.get(i).getState())) {
                return i + 1;
            }
        }
        return 0;
    }

    private boolean hasValidStep(ModalOperation formula) {
        return proof().getSteps().stream().anyMatch(step -> step.isValid() && step.getStep().equals(formula));
    }

    /**
     * {@code XE} on {@code X A}; {@code U<>} and {@code UE} on {@code A U B}; else {@code Succ} on the line (see
     * {@link #successorRelation}).
     */
    @Override
    protected Optional<AbstractModalAction> elimRuleForOtherStep(int i) {
        var step = proof().getSteps().get(i);
        int line = i + 1;
        return (switch (step.getStep()) {
            case Next ignored when successor(step.getState()).isPresent() -> checkSingleAction(new ModalNextE(line));
            case Until ignored -> checkSingleAction(new ModalUntilSometime(line))
                    .or(() -> checkSingleAction(new ModalUntilE(line)));
            default -> Optional.<AbstractModalAction>empty();
        }).or(() -> successorRelation(step.getState(), line));
    }

    /**
     * {@code Succ} on a line in {@code s}, giving {@code s <= s+1}, when {@code s+1} is in use, the relation is not a
     * valid step yet and a relation can help: some valid step is a {@code []} (for {@code []E}) or some goal a
     * {@code <>} (for {@code <>I}).
     */
    private Optional<AbstractModalAction> successorRelation(String state, int line) {
        boolean relationsHelp = proof().getSteps().stream().anyMatch(step -> step.isValid() && step.getStep() instanceof Always)
                || goals().stream().anyMatch(goal -> goal.getValue() instanceof Sometime);
        if (!relationsHelp) {
            return Optional.empty();
        }
        return successor(state)
                .filter(succ -> isInUse(succ) && !hasValidStep(new LessEqual(state, succ)))
                .flatMap(succ -> checkSingleAction(new ModalSuccessor(line)));
    }

    /**
     * An elimination is only applied when it adds a valid step that is not there yet (in the same state): the
     * implication {@code A -> A} that {@code OrE1}/{@code OrE2} leave would otherwise give the newest {@code A} again
     * and again. The rule is applied to see what it adds, and those steps removed again (an elimination rule only
     * discharges assumptions of its own).
     */
    @Override
    protected boolean isUsefulElimination(AbstractModalAction act) {
        List<ProofStepModal> steps = proof().getSteps();
        int before = steps.size();
        act.apply(proof());
        boolean useful = false;
        for (int i = before; i < steps.size() && !useful; i++) {
            var added = steps.get(i);
            useful = added.isValid() && !isValidBefore(added, before);
        }
        while (steps.size() > before) {
            proof().removeLastStep();
        }
        return useful;
    }

    private boolean isValidBefore(ProofStepModal added, int before) {
        List<ProofStepModal> steps = proof().getSteps();
        for (int i = 0; i < before; i++) {
            var step = steps.get(i);
            if (step.isValid() && step.getStep().equals(added.getStep()) && Objects.equals(step.getState(), added.getState())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a goal or a valid step is in {@code state}, or a valid relation step names it.
     */
    private boolean isInUse(String state) {
        boolean inGoal = goals().stream().anyMatch(goal -> state.equals(goal.getKey()));
        return inGoal || proof().getSteps().stream()
                .filter(ProofStepModal::isValid)
                .anyMatch(step -> state.equals(step.getState()) || step.getStep() instanceof RelationOperation relation
                        && (state.equals(relation.getLeft()) || state.equals(relation.getRight())));
    }

    /**
     * {@code X A} in {@code s} becomes {@code A} in {@code s+1}; {@code A U B} starts its attempts. A goal beyond the
     * offset bound gets stuck.
     */
    @Override
    protected boolean updateOtherGoal(ModalLogicalOperation goal, String state) {
        if (goal instanceof Next next) {
            successor(state).ifPresent(succ -> pushGoal(succ, next.getElement()));
            return true;
        } else if (goal instanceof Until until) {
            List<List<Map.Entry<String, ModalOperation>>> alternatives = new ArrayList<>();
            alternatives.add(List.of(entry(state, until.getRight())));
            if (successor(state).isPresent()) {
                // Pushed in order, so A is reached first, then X (A U B)
                alternatives.add(List.of(entry(state, new Next(until)), entry(state, until.getLeft())));
            }
            start(state(), alternatives);
            return true;
        }
        return false;
    }

    private static Map.Entry<String, ModalOperation> entry(String state, ModalOperation formula) {
        return new AbstractMap.SimpleEntry<>(state, formula);
    }

    private void start(State before, List<List<Map.Entry<String, ModalOperation>>> alternatives) {
        attempts.push(new Attempt(before, alternatives.subList(1, alternatives.size())));
        alternatives.getFirst().forEach(goal -> pushGoal(goal.getKey(), goal.getValue()));
    }

    /**
     * The subgoals of an attempt were reached, so it succeeded: there is no going back on it anymore. Its subgoals
     * are the goals above the one it is about, which is the last goal when the attempt starts.
     */
    @Override
    protected void goalRemoved() {
        while (!attempts.isEmpty() && goalCount() <= attempts.peek().before().goals().size()) {
            attempts.pop();
        }
    }

    /**
     * The innermost attempt failed: remove its steps and try the next way to reach its goal. When every way failed,
     * the attempt that needed the goal fails too. With no attempt left the solver gives up and leaves the proof with
     * its premises.
     */
    @Override
    protected boolean stalled() {
        while (!attempts.isEmpty()) {
            Attempt failed = attempts.pop();
            restore(failed.before());
            if (!failed.remaining().isEmpty()) {
                start(failed.before(), failed.remaining());
                return true;
            }
        }
        if (!proof().isDone()) {
            proof().reset();
        }
        return false;
    }
}
