package com.dan323.proof.modal.internal;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.base.UnaryOperation;
import com.dan323.expressions.modal.Always;
import com.dan323.expressions.modal.ConjunctionModal;
import com.dan323.expressions.modal.ConstantModal;
import com.dan323.expressions.modal.DisjunctionModal;
import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.modal.NegationModal;
import com.dan323.expressions.modal.Next;
import com.dan323.expressions.modal.Sometime;
import com.dan323.expressions.modal.Until;
import com.dan323.expressions.relation.LessEqual;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.expressions.relation.StateTerm;
import com.dan323.proof.generic.RuleUtils;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.ModalAndE1;
import com.dan323.proof.modal.ModalAndE2;
import com.dan323.proof.modal.ModalAndI;
import com.dan323.proof.modal.ModalAssume;
import com.dan323.proof.modal.ModalBoxE;
import com.dan323.proof.modal.ModalBoxI;
import com.dan323.proof.modal.ModalCopy;
import com.dan323.proof.modal.ModalDeductionTheorem;
import com.dan323.proof.modal.ModalDiaE;
import com.dan323.proof.modal.ModalDiaI;
import com.dan323.proof.modal.ModalFI;
import com.dan323.proof.modal.ModalModusPonens;
import com.dan323.proof.modal.ModalNotE;
import com.dan323.proof.modal.ModalNotI;
import com.dan323.proof.modal.ModalOrI1;
import com.dan323.proof.modal.ModalOrI2;
import com.dan323.proof.modal.complex.DeMorgan;
import com.dan323.proof.modal.complex.ModalDeMorgan;
import com.dan323.proof.modal.complex.ModalOrE1;
import com.dan323.proof.modal.complex.ModalOrE2;
import com.dan323.proof.modal.nextuntil.ModalEqualState;
import com.dan323.proof.modal.nextuntil.ModalInduction;
import com.dan323.proof.modal.nextuntil.ModalLinearity;
import com.dan323.proof.modal.nextuntil.ModalNegatedUntil;
import com.dan323.proof.modal.nextuntil.ModalNextE;
import com.dan323.proof.modal.nextuntil.ModalNextI;
import com.dan323.proof.modal.nextuntil.ModalOrder;
import com.dan323.proof.modal.nextuntil.ModalUntilI1;
import com.dan323.proof.modal.nextuntil.ModalUntilI2;
import com.dan323.proof.modal.nextuntil.ModalUntilWitness;
import com.dan323.proof.modal.nextuntil.ModalUntilWitnessLeft;
import com.dan323.proof.modal.nextuntil.ModalUntilWitnessRight;
import com.dan323.proof.modal.nextuntil.ParseModalNextUntilAction;
import com.dan323.proof.modal.nextuntil.StateOrder;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;

/**
 * The automatic solver of {@code modal-next-until}: the proof search of Bolotov, Grigoriev and Shangin,
 * <i>Automated Natural Deduction for Propositional Linear-time Temporal Logic</i> (TIME 2007), written for the rules of
 * this logic. Time is linear: every state is a natural number and {@code s+1} is the next one.
 * <p>
 * It keeps the proof and a stack of goals, and repeats:
 * <ol>
 *     <li>If the top goal is in the proof (for {@code FALSE}: two lines that contradict each other, in the same state
 *     or in states the relations make equal, or relations that contradict each other), it is reached: the introduction
 *     rule it was set up for is applied ({@code ->I}, {@code -I}, {@code []I}, {@code XI}, {@code Lin}, {@code <>E},
 *     {@code Ind}) and it is removed.</li>
 *     <li>Else, if an introduction rule reaches it from the proof as it is, that rule is applied.</li>
 *     <li>Else an elimination rule that adds a new line is applied, in the order of the paper: the Boolean ones,
 *     De Morgan ({@code -(A | B)}, {@code -<>A}, {@code -U}), the witness of an Until ({@code UW}, {@code UB},
 *     {@code UA}), {@code XE}, {@code []E} (on every state known to be later) and, opening a subproof, {@code <>E}.</li>
 *     <li>Else new goals come from the structure of the top goal: its parts for {@code &}, an assumption and its
 *     conclusion for {@code ->}, {@code -} and {@code []}, the next state for {@code X}. A {@code |}, {@code <>} or
 *     {@code U} goal is first tried as one of its parts, only with the rules above; if that fails, and for every other
 *     goal, its negation is assumed and the goal becomes {@code FALSE}.</li>
 *     <li>A {@code FALSE} goal looks for the missing premise of an elimination rule among the lines: {@code A} for
 *     {@code -A} and {@code A -> B}, {@code -A} for {@code A | B}, {@code -B} for {@code A U B}. With none left, it
 *     reasons by cases on the order of two states ({@code Lin}: equal or earlier, then later), and then, when some
 *     {@code []} is in the proof, by induction on a formula of the proof ({@code Ind} with the goal
 *     {@code [] (A -> X A)}). The paper tries induction before the cases; the cases first find more proofs in the
 *     same time.</li>
 * </ol>
 * A line used as a source of goals, a pair of states split by cases or a formula tried by induction is marked, and
 * unmarked when the goal it gave is reached, as in the paper. Unlike the paper, the search goes back when a choice
 * leads nowhere: to the last choice whose goal is not reached yet, which is then left out. It gives up, leaving the
 * proof with its premises only, when nothing is left to try, after {@link #ROUNDS_PER_SYMBOL} rounds for each symbol of
 * the premises and the goal, or after {@link #TIME_LIMIT}; a branch that grows beyond {@link #STEPS_PER_SYMBOL} lines
 * for each symbol counts as leading nowhere. {@code XE}, and the states it reasons on, stop {@link #maxOffset} states
 * after a base: induction has to go further.
 */
public final class LinearTimeAutomate {

    /**
     * How many lines the proof may have for each symbol of its premises and goal.
     */
    static final int STEPS_PER_SYMBOL = 40;

    /**
     * How many rounds the search may take for each symbol of its premises and goal. Going back to a choice makes the
     * search exponential, so an unprovable goal would otherwise run until the solver's timeout.
     */
    static final int ROUNDS_PER_SYMBOL = 300;

    /**
     * How long the search may take before it gives up, below the solver timeout of the REST API (10 seconds by
     * default), so that a goal it cannot prove ends with no proof rather than a timeout.
     */
    static final Duration TIME_LIMIT = Duration.ofSeconds(5);

    private enum Closer {
        /** Nothing to apply: the goal below uses it. */
        NONE,
        /** A direct attempt at a part of the goal below; removed when the rules of step 3 cannot reach it. */
        TRY,
        IMPLICATION, NEGATION, REFUTATION, BOX, NEXT, LINEARITY, DIAMOND, INDUCTION
    }

    private static final class Goal {
        private final ModalOperation target;
        private final String state;
        private final Closer closer;
        /** The mark to remove once reached, or null. */
        private final String mark;
        /** DIAMOND: the {@code <>} line; INDUCTION: the line of the formula. */
        private final int source;
        private int tries;
        private boolean expanded;

        private Goal(ModalOperation target, String state, Closer closer, String mark, int source) {
            this.target = target;
            this.state = state;
            this.closer = closer;
            this.mark = mark;
            this.source = source;
        }

        private Goal copy() {
            var copy = new Goal(target, state, closer, mark, source);
            copy.tries = tries;
            copy.expanded = expanded;
            return copy;
        }

        /** A missing premise that a {@code FALSE} goal set up, which is not refuted in turn (as in the paper). */
        private boolean fromSource() {
            return mark != null && mark.startsWith(SOURCE);
        }

        private boolean isFalse() {
            return ConstantModal.FALSE.equals(target);
        }

        @Override
        public String toString() {
            return (state == null ? "" : state + ": ") + target + " (" + closer + ")";
        }
    }

    /**
     * The search as it was before a choice, to go back to when that choice leads nowhere.
     *
     * @param steps the number of lines of the proof
     * @param goals the goals
     * @param marks the marks
     * @param mark  the mark of the choice, which stays set after going back so that it is not made again
     */
    private record Checkpoint(int steps, List<Goal> goals, Set<String> marks, String mark) {
    }

    private static final String SOURCE = "source ";

    private ModalNaturalDeduction proof;
    private final List<Goal> goals = new ArrayList<>();
    private final Set<String> marks = new HashSet<>();
    private final Deque<Checkpoint> checkpoints = new ArrayDeque<>();
    private int maxSize;
    private int maxOffset;
    private int rounds;
    private long deadline;
    private StateOrder order;
    private int orderSize = -1;
    /** The first valid line of each formula in each state, for {@link #find}; rebuilt when the proof changes. */
    private final Map<Located, Integer> index = new HashMap<>();
    private final Map<ModalOperation, List<Integer>> byFormula = new HashMap<>();
    private int indexSize = -1;

    /**
     * A solver keeps its working state in fields: use one instance per proof to solve.
     */
    public LinearTimeAutomate() {
        // Nothing to set up: automate initializes the state
    }

    /**
     * Solve the proof from its premises, or leave it with its premises only.
     *
     * @param naturalDeduction the proof to solve
     * @throws CancellationException if the calling thread is interrupted
     */
    public void automate(ModalNaturalDeduction naturalDeduction) {
        proof = naturalDeduction;
        proof.reset();
        goals.clear();
        marks.clear();
        checkpoints.clear();
        orderSize = -1;
        indexSize = -1;
        int symbols = size(proof.getGoal());
        int temporal = temporal(proof.getGoal());
        for (ModalOperation premise : proof.getAssms()) {
            symbols += size(premise);
            temporal += temporal(premise);
        }
        maxSize = STEPS_PER_SYMBOL * symbols;
        maxOffset = Math.max(1, temporal);
        rounds = ROUNDS_PER_SYMBOL * symbols;
        deadline = System.nanoTime() + TIME_LIMIT.toNanos();
        goals.add(goal(proof.getGoal(), proof.getState0(), Closer.NONE, null, 0));
        if (!search()) {
            proof.reset();
        }
    }

    private static int size(LogicOperation formula) {
        if (formula instanceof UnaryOperation<?> unary) {
            return 1 + size(unary.getElement());
        } else if (formula instanceof BinaryOperation<?> binary) {
            return 1 + size(binary.getLeft()) + size(binary.getRight());
        }
        return 1;
    }

    /** @return how many {@code X} and {@code U} the formula has */
    private static int temporal(LogicOperation formula) {
        int own = formula instanceof Next || formula instanceof Until ? 1 : 0;
        if (formula instanceof UnaryOperation<?> unary) {
            return own + temporal(unary.getElement());
        } else if (formula instanceof BinaryOperation<?> binary) {
            return own + temporal(binary.getLeft()) + temporal(binary.getRight());
        }
        return own;
    }

    private static Goal goal(ModalOperation target, String state, Closer closer, String mark, int source) {
        boolean stateless = target instanceof RelationOperation || ConstantModal.FALSE.equals(target);
        return new Goal(target, stateless ? null : state, closer, mark, source);
    }

    private boolean search() {
        while (true) {
            checkNotInterrupted();
            if (--rounds < 0 || System.nanoTime() > deadline) {
                return false;
            }
            boolean stuck;
            if (proof.getSteps().size() > maxSize) {
                stuck = true;
            } else {
                Goal top = goals.getLast();
                if (reached(top)) {
                    goals.removeLast();
                    if (top.mark != null) {
                        marks.remove(top.mark);
                        checkpoints.removeIf(checkpoint -> checkpoint.mark().equals(top.mark));
                    }
                    stuck = !close(top);
                    if (!stuck && goals.isEmpty()) {
                        return proof.isDone();
                    }
                } else if (top.closer == Closer.TRY) {
                    if (!introduce(top) && !eliminate(false)) {
                        goals.removeLast();
                    }
                    stuck = false;
                } else {
                    stuck = !introduce(top) && !eliminate(true) && !expand(top);
                }
            }
            if (stuck && !backtrack()) {
                return false;
            }
        }
    }

    /** Remember the search as it is, before making the choice {@code mark}. */
    private void checkpoint(String mark) {
        checkpoints.push(new Checkpoint(last(), goals.stream().map(Goal::copy).toList(), new HashSet<>(marks), mark));
    }

    /**
     * Go back to the last choice that is still open and leave it out: the proof loses the lines added since (it is
     * rebuilt from its premises by replaying the lines it keeps, so a line that a later rule closed is valid again), and
     * the goals and marks are the ones of then.
     *
     * @return false when there is no choice to go back to
     */
    private boolean backtrack() {
        var checkpoint = checkpoints.poll();
        if (checkpoint == null) {
            return false;
        }
        List<AbstractModalAction> actions = proof.parse();
        proof.reset();
        for (int i = last(); i < checkpoint.steps(); i++) {
            actions.get(i).apply(proof);
        }
        goals.clear();
        checkpoint.goals().forEach(goal -> goals.add(goal.copy()));
        marks.clear();
        marks.addAll(checkpoint.marks());
        marks.add(checkpoint.mark());
        orderSize = -1;
        indexSize = -1;
        return true;
    }

    private static void checkNotInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException("The automatic solver was interrupted");
        }
    }

    // ---------------------------------------------------------------- the proof

    private List<ProofStepModal> steps() {
        return proof.getSteps();
    }

    private int last() {
        return steps().size();
    }

    /** @return the 1-based line of a valid step with this formula in this state, or 0 */
    private int find(ModalOperation formula, String state) {
        indexLines();
        return index.getOrDefault(new Located(formula, state), 0);
    }

    /** @return the 1-based lines of the valid steps with this formula, in any state */
    private List<Integer> linesOf(ModalOperation formula) {
        indexLines();
        return byFormula.getOrDefault(formula, List.of());
    }

    private void indexLines() {
        if (indexSize != last()) {
            index.clear();
            byFormula.clear();
            for (int i = steps().size() - 1; i >= 0; i--) {
                var step = steps().get(i);
                if (step.isValid()) {
                    index.put(new Located(step.getStep(), step.getState()), i + 1);
                    byFormula.computeIfAbsent(step.getStep(), f -> new ArrayList<>()).add(i + 1);
                }
            }
            indexSize = last();
        }
    }

    private record Located(ModalOperation formula, String state) {
    }

    /** @return the 1-based line of a valid relation step equal to {@code relation}, or 0 */
    private int findRelation(RelationOperation relation) {
        for (int i = 0; i < steps().size(); i++) {
            var step = steps().get(i);
            if (step.isValid() && step.getStep().equals(relation)) {
                return i + 1;
            }
        }
        return 0;
    }

    private boolean has(ModalOperation formula, String state) {
        return find(formula, state) > 0;
    }

    private boolean apply(AbstractModalAction action) {
        if (action.isValid(proof)) {
            action.apply(proof);
            return true;
        }
        return false;
    }

    private StateOrder order() {
        if (orderSize != last()) {
            Map<Integer, RelationOperation> relations = new LinkedHashMap<>();
            for (int i = 0; i < steps().size(); i++) {
                var step = steps().get(i);
                if (step.isValid() && step.getStep() instanceof RelationOperation relation) {
                    relations.put(i + 1, relation);
                }
            }
            order = new StateOrder(relations);
            orderSize = last();
        }
        return order;
    }

    private boolean entails(String smaller, String greater) {
        return order().holds(smaller, greater);
    }

    /** @return the line of {@code smaller <= greater}, derived with {@code Ord} if needed, or 0 when it does not follow */
    private int relationLine(String smaller, String greater) {
        var relation = new LessEqual(smaller, greater);
        int line = findRelation(relation);
        if (line == 0 && apply(new ModalOrder(relation))) {
            line = last();
        }
        return line;
    }

    /**
     * The states of the valid lines and of the goals, in the order they first appear, except those more than
     * {@link #maxOffset} successors after their base: {@code Lin} writes ever later ones ({@code s0+3 <= s1}), and
     * reasoning on them would go on for ever.
     */
    private List<String> states() {
        Set<String> states = new LinkedHashSet<>();
        for (var step : steps()) {
            if (step.isValid()) {
                if (step.getStep() instanceof RelationOperation relation) {
                    states.add(relation.getLeft());
                    states.add(relation.getRight());
                } else {
                    states.add(step.getState());
                }
            }
        }
        for (Goal goal : goals) {
            if (goal.state != null) {
                states.add(goal.state);
            }
        }
        states.removeIf(state -> term(state).filter(t -> t.offset() <= maxOffset).isEmpty());
        return new ArrayList<>(states);
    }

    private static Optional<StateTerm> term(String state) {
        try {
            return Optional.of(StateTerm.parse(state));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static Optional<String> successor(String state) {
        return term(state).filter(StateTerm::hasSuccessor).map(t -> t.successor().toString());
    }

    /** The first line of the innermost open subproof, or 0 at the top level. */
    private int innermost() {
        int level = RuleUtils.getLastAssumptionLevel(proof);
        return level == 0 ? 0 : last() - RuleUtils.getToLastAssumption(proof, level) + 1;
    }

    /** Assume a formula or a relation and return its line. */
    private int assume(ModalOperation assumption, String state) {
        if (assumption instanceof RelationOperation relation) {
            new ModalAssume(relation).apply(proof);
        } else {
            new ModalAssume((ModalLogicalOperation) assumption, state).apply(proof);
        }
        return last();
    }

    // ---------------------------------------------------------------- 1. reached goals

    private boolean reached(Goal goal) {
        if (goal.isFalse()) {
            return contradiction();
        } else if (goal.target instanceof RelationOperation relation) {
            return findRelation(relation) > 0;
        }
        return has(goal.target, goal.state);
    }

    /**
     * Whether {@code FALSE} is reached, writing it as a line if it is not one yet: a {@code FALSE} line, {@code A} and
     * {@code -A} in the same state (or in two states the relations make equal), or relations that contradict each
     * other.
     */
    private boolean contradiction() {
        if (has(ConstantModal.FALSE, proof.getState0())) {
            return true;
        }
        for (int i = 0; i < steps().size(); i++) {
            var step = steps().get(i);
            if (step.isValid() && step.getStep() instanceof NegationModal negation) {
                int positive = find(negation.getElement(), step.getState());
                if (positive > 0) {
                    return apply(new ModalFI(positive, i + 1));
                }
            }
        }
        if (order().hasEqualStates()) {
            for (int i = 0; i < steps().size(); i++) {
                var step = steps().get(i);
                if (step.isValid() && step.getStep() instanceof NegationModal negation && transferred(negation.getElement(), step.getState())) {
                    return apply(new ModalFI(last(), i + 1));
                }
            }
        }
        return order().contradiction().isPresent() && apply(new ModalOrder(ConstantModal.FALSE));
    }

    /** Derive {@code formula} in {@code state} with {@code Eq} from a line in a state equal to it, if there is one. */
    private boolean transferred(ModalOperation formula, String state) {
        for (int line : linesOf(formula)) {
            String other = steps().get(line - 1).getState();
            if (!state.equals(other) && entails(other, state) && entails(state, other)) {
                return apply(new ModalEqualState(line, state));
            }
        }
        return false;
    }

    /** Write the formula as the last line, repeating it if it is an earlier one. */
    private boolean lastIs(ModalOperation formula, String state) {
        var lastStep = steps().getLast();
        if (lastStep.isValid() && lastStep.getStep().equals(formula) && Objects.equals(state, lastStep.getState())) {
            return true;
        }
        int line = find(formula, state);
        return line > 0 && apply(new ModalCopy(line));
    }

    /** Apply what reaching {@code goal} was set up for. */
    private boolean close(Goal goal) {
        return switch (goal.closer) {
            case NONE, TRY -> true;
            case IMPLICATION -> lastIs(goal.target, goal.state) && apply(new ModalDeductionTheorem());
            case NEGATION -> lastIs(ConstantModal.FALSE, proof.getState0()) && apply(new ModalNotI());
            case REFUTATION -> lastIs(ConstantModal.FALSE, proof.getState0()) && apply(new ModalNotI()) && apply(new ModalNotE(last()));
            case BOX -> lastIs(goal.target, goal.state) && apply(new ModalBoxI());
            case NEXT -> apply(new ModalNextI(find(goal.target, goal.state)));
            case LINEARITY -> lastIs(ConstantModal.FALSE, proof.getState0()) && apply(new ModalLinearity());
            case DIAMOND -> lastIs(goal.target, goal.isFalse() ? proof.getState0() : goal.state) && apply(new ModalDiaE(goal.source));
            case INDUCTION -> apply(new ModalInduction(goal.source, find(goal.target, goal.state)));
        };
    }

    // ---------------------------------------------------------------- 2. introduction rules

    private boolean introduce(Goal goal) {
        if (goal.isFalse()) {
            return false;
        }
        if (goal.target instanceof LessEqual relation) {
            return entails(relation.getLeft(), relation.getRight()) && apply(new ModalOrder(relation));
        }
        if (goal.target instanceof RelationOperation) {
            return false;
        }
        String state = goal.state;
        boolean applied = switch (goal.target) {
            case ConjunctionModal conj -> {
                int left = find(conj.getLeft(), state);
                int right = find(conj.getRight(), state);
                yield left > 0 && right > 0 && apply(new ModalAndI(left, right));
            }
            case DisjunctionModal disj -> {
                int left = find(disj.getLeft(), state);
                int right = find(disj.getRight(), state);
                yield left > 0 && apply(new ModalOrI1(left, disj.getRight()))
                        || right > 0 && apply(new ModalOrI2(right, disj.getLeft()));
            }
            case Sometime sometime -> introduceSometime(sometime, state);
            case Until until -> {
                int right = find(until.getRight(), state);
                int left = find(until.getLeft(), state);
                int next = find(new Next(until), state);
                yield right > 0 && apply(new ModalUntilI1(right, (ModalLogicalOperation) until.getLeft()))
                        || left > 0 && next > 0 && apply(new ModalUntilI2(left, next));
            }
            case Next next -> successor(state).map(succ -> find(next.getElement(), succ))
                    .filter(line -> line > 0)
                    .map(line -> apply(new ModalNextI(line)))
                    .orElse(false);
            default -> false;
        };
        return applied || transferred(goal.target, state);
    }

    private boolean introduceSometime(Sometime sometime, String state) {
        for (int i = 0; i < steps().size(); i++) {
            var step = steps().get(i);
            if (step.isValid() && step.getStep().equals(sometime.getElement()) && entails(state, step.getState())) {
                int relation = relationLine(state, step.getState());
                if (relation > 0 && apply(new ModalDiaI(i + 1, relation))) {
                    return true;
                }
            }
        }
        return false;
    }

    // ---------------------------------------------------------------- 3. elimination rules

    /**
     * Apply one elimination rule that adds a line that is not there yet.
     *
     * @param subproofs whether it may open the subproof of {@code <>E}
     */
    private boolean eliminate(boolean subproofs) {
        return eliminateBoolean() || eliminateNegations() || eliminateUntil() || eliminateNext() || eliminateAlways()
                || subproofs && openSometime();
    }

    private boolean eliminateBoolean() {
        for (int i = 1; i <= last(); i++) {
            var step = steps().get(i - 1);
            if (!step.isValid()) {
                continue;
            }
            String state = step.getState();
            boolean applied = switch (step.getStep()) {
                case ConjunctionModal conj -> !has(conj.getLeft(), state) && apply(new ModalAndE1(i))
                        || !has(conj.getRight(), state) && apply(new ModalAndE2(i));
                case ImplicationModal imp -> {
                    int left = find(imp.getLeft(), state);
                    yield left > 0 && !has(imp.getRight(), state) && apply(new ModalModusPonens(i, left));
                }
                case NegationModal neg when neg.getElement() instanceof NegationModal inner ->
                        !has(inner.getElement(), state) && apply(new ModalNotE(i));
                case DisjunctionModal disj -> {
                    int notLeft = find(new NegationModal(disj.getLeft()), state);
                    int notRight = find(new NegationModal(disj.getRight()), state);
                    yield notLeft > 0 && !has(disj.getRight(), state) && apply(new ModalOrE1(i, notLeft))
                            || notRight > 0 && !has(disj.getLeft(), state) && apply(new ModalOrE2(i, notRight));
                }
                default -> false;
            };
            if (applied) {
                return true;
            }
        }
        return false;
    }

    private boolean eliminateNegations() {
        for (int i = 1; i <= last(); i++) {
            var step = steps().get(i - 1);
            if (!step.isValid() || !(step.getStep() instanceof NegationModal negation)) {
                continue;
            }
            String state = step.getState();
            boolean applied = switch (negation.getElement()) {
                case DisjunctionModal disj -> (!has(new NegationModal(disj.getLeft()), state)
                        || !has(new NegationModal(disj.getRight()), state)) && apply(new ModalDeMorgan(i));
                case Sometime sometime -> {
                    var element = sometime.getElement();
                    var always = new Always(element instanceof NegationModal inner ? inner.getElement() : new NegationModal(element));
                    yield !has(always, state) && apply(new DeMorgan(i));
                }
                case Until until -> !has(ModalNegatedUntil.expansion(until), state) && apply(new ModalNegatedUntil(i));
                default -> false;
            };
            if (applied) {
                return true;
            }
        }
        return false;
    }

    /**
     * The witness of an {@code A U B} line in state {@code s} when {@code -B} holds in {@code s}, as the paper's Until
     * elimination does: {@code UW} and {@code UB} give a state {@code t} with {@code B}, and {@code -B} in {@code s}
     * makes it later than {@code s} ({@code s+1 <= t}: assume {@code t <= s}, then {@code B} is in {@code s} too, and
     * {@code Lin}). Without {@code -B}, taking witnesses would not stop: {@code [] (q U q)} gives {@code q U q} in the
     * witness, which has a witness of its own, and so on. Then {@code UA} on every state known to be from {@code s}
     * on and before a witness.
     */
    private boolean eliminateUntil() {
        for (int i = 1; i <= last(); i++) {
            var step = steps().get(i - 1);
            if (step.isValid() && step.getStep() instanceof Until until) {
                int notRight = find(new NegationModal((ModalLogicalOperation) until.getRight()), step.getState());
                if (notRight > 0 && marks.add("UW " + i) && apply(new ModalUntilWitness(i, null))) {
                    witnessLater(step.getState(), notRight);
                    return true;
                }
            }
        }
        for (int w = 1; w <= last(); w++) {
            var step = steps().get(w - 1);
            if (!step.isValid() || !ParseModalNextUntilAction.UNTIL_WITNESS.equals(step.getProof().getNameProof())
                    || !(step.getStep() instanceof LessEqual witness)) {
                continue;
            }
            var until = ModalUntilWitness.witness(proof, w).map(ModalUntilWitness.Witness::until);
            if (until.isEmpty()) {
                continue;
            }
            for (String state : states()) {
                var next = successor(state);
                if (next.isPresent() && !has(until.get().getLeft(), state) && entails(witness.getLeft(), state)
                        && entails(next.get(), witness.getRight()) && apply(new ModalUntilWitnessLeft(w, state))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@code XE}, but not beyond {@link #maxOffset} successors of a base: otherwise {@code [] (p -> X p)} would give
     * {@code p} in {@code s0+1}, {@code s0+2}, ... for ever. Beyond that, induction has to do it.
     */
    /** After {@code UW}: {@code UB}, then {@code s+1 <= t} from {@code -B} in {@code s} (line {@code notRight}). */
    private void witnessLater(String state, int notRight) {
        int witnessLine = last();
        String witness = ((LessEqual) steps().get(witnessLine - 1).getStep()).getRight();
        if (!apply(new ModalUntilWitnessRight(witnessLine))) {
            return;
        }
        int right = last();
        assume(new LessEqual(witness, state), null);
        boolean closed = apply(new ModalEqualState(right, state)) && apply(new ModalFI(last(), notRight))
                && apply(new ModalLinearity());
        if (!closed) {
            throw new IllegalStateException("The witness of line " + witnessLine + " is not later than " + state);
        }
    }

    private boolean eliminateNext() {
        for (int i = 1; i <= last(); i++) {
            var step = steps().get(i - 1);
            if (step.isValid() && step.getStep() instanceof Next next) {
                var succ = term(step.getState()).filter(t -> t.hasSuccessor() && t.offset() < maxOffset).map(t -> t.successor().toString());
                if (succ.isPresent() && !has(next.getElement(), succ.get()) && apply(new ModalNextE(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** {@code []E} on every state known to be from the state of the {@code []} on. */
    private boolean eliminateAlways() {
        List<String> states = states();
        for (int i = 1; i <= last(); i++) {
            var step = steps().get(i - 1);
            if (!step.isValid() || !(step.getStep() instanceof Always always)) {
                continue;
            }
            for (String state : states) {
                if (!has(always.getElement(), state) && entails(step.getState(), state)) {
                    int relation = relationLine(step.getState(), state);
                    if (relation > 0 && apply(new ModalBoxE(i, relation))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * {@code <>E} on a {@code <> A} line: assume {@code s <= t} and {@code A} in a new state {@code t}, and set the top
     * goal again inside, to reach it there and close the subproof with {@code <>E}.
     */
    private boolean openSometime() {
        Goal top = goals.getLast();
        for (int i = 1; i <= last(); i++) {
            var step = steps().get(i - 1);
            String mark = "<>E " + i;
            if (step.isValid() && step.getStep() instanceof Sometime sometime && !marks.contains(mark)) {
                String state = proof.newState();
                marks.add(mark);
                assume(new LessEqual(step.getState(), state), null);
                assume(sometime.getElement(), state);
                goals.add(goal(top.target, top.state, Closer.DIAMOND, mark, i));
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------- 4. goals from the structure of the goal

    private boolean expand(Goal goal) {
        if (goal.isFalse()) {
            return sources();
        }
        if (goal.target instanceof LessEqual relation) {
            // Refute that it fails: v+1 <= u; Lin then gives u+1 <= v+1, and Ord u <= v
            var succ = successor(relation.getRight());
            if (goal.expanded || succ.isEmpty()) {
                return false;
            }
            goal.expanded = true;
            assume(new LessEqual(succ.get(), relation.getLeft()), null);
            goals.add(goal(ConstantModal.FALSE, null, Closer.LINEARITY, null, 0));
            return true;
        }
        if (goal.target instanceof RelationOperation) {
            return false;
        }
        String state = goal.state;
        return switch (goal.target) {
            case ConjunctionModal conj -> once(goal, () -> {
                goals.add(goal(conj.getRight(), state, Closer.NONE, null, 0));
                goals.add(goal(conj.getLeft(), state, Closer.NONE, null, 0));
            });
            case ImplicationModal imp -> once(goal, () -> {
                assume(imp.getLeft(), state);
                goals.add(goal(imp.getRight(), state, Closer.IMPLICATION, null, 0));
            });
            case NegationModal neg -> once(goal, () -> {
                assume(neg.getElement(), state);
                goals.add(goal(ConstantModal.FALSE, null, Closer.NEGATION, null, 0));
            });
            case Always always -> once(goal, () -> {
                String next = proof.newState();
                assume(new LessEqual(state, next), null);
                goals.add(goal(always.getElement(), next, Closer.BOX, null, 0));
            });
            case Next next -> successor(state).map(succ -> once(goal,
                    () -> goals.add(goal(next.getElement(), succ, Closer.NEXT, null, 0)))).orElse(false);
            case DisjunctionModal disj -> tryPart(goal, List.of(disj.getLeft(), disj.getRight()));
            case Sometime sometime -> tryPart(goal, List.of(sometime.getElement()));
            case Until until -> tryPart(goal, List.of(until.getRight()));
            default -> refute(goal);
        };
    }

    private boolean once(Goal goal, Runnable subgoals) {
        if (goal.expanded) {
            return false;
        }
        goal.expanded = true;
        subgoals.run();
        return true;
    }

    /** Try the next part of the goal as a direct attempt, and refute the goal once every part was tried. */
    private boolean tryPart(Goal goal, List<ModalOperation> parts) {
        if (goal.tries < parts.size()) {
            goals.add(goal(parts.get(goal.tries), goal.state, Closer.TRY, null, 0));
            goal.tries++;
            return true;
        }
        return refute(goal);
    }

    /** Assume the negation of the goal and aim for {@code FALSE}; a missing premise is not refuted. */
    private boolean refute(Goal goal) {
        if (goal.fromSource()) {
            return false;
        }
        return once(goal, () -> {
            assume(new NegationModal((ModalLogicalOperation) goal.target), goal.state);
            goals.add(goal(ConstantModal.FALSE, null, Closer.REFUTATION, null, 0));
        });
    }

    // ---------------------------------------------------------------- 5. sources of FALSE

    private boolean sources() {
        return missingPremise() || splitStates() || induction();
    }

    /** A goal that would let an elimination rule apply to a line: {@code A} for {@code -A} and {@code A -> B}, ... */
    private boolean missingPremise() {
        for (int i = 1; i <= last(); i++) {
            var step = steps().get(i - 1);
            if (!step.isValid()) {
                continue;
            }
            String state = step.getState();
            ModalOperation missing = switch (step.getStep()) {
                case NegationModal neg -> neg.getElement();
                case DisjunctionModal disj -> has(disj.getLeft(), state) || has(disj.getRight(), state)
                        ? null : new NegationModal(disj.getLeft());
                case ImplicationModal imp -> has(imp.getRight(), state) ? null : imp.getLeft();
                case Until until -> has(until.getRight(), state) ? null : new NegationModal((ModalLogicalOperation) until.getRight());
                default -> null;
            };
            String mark = SOURCE + i;
            if (missing != null && !has(missing, state) && !marks.contains(mark)) {
                checkpoint(mark);
                marks.add(mark);
                goals.add(goal(missing, state, Closer.NONE, mark, 0));
                return true;
            }
        }
        return false;
    }

    /**
     * Reason by cases on the order of two states with a common earlier state whose order is not known: assume
     * {@code u <= v} (or, when that is known, {@code v <= u}: the states are equal) and aim for {@code FALSE}; {@code Lin}
     * then gives the other case.
     */
    private boolean splitStates() {
        List<String> states = states();
        for (int a = 0; a < states.size(); a++) {
            for (int b = a + 1; b < states.size(); b++) {
                String u = states.get(a);
                String v = states.get(b);
                var assumption = caseOf(states, u, v);
                if (assumption.isPresent()) {
                    String mark = "Lin " + assumption.get();
                    if (!marks.contains(mark)) {
                        checkpoint(mark);
                        marks.add(mark);
                        assume(assumption.get(), null);
                        goals.add(goal(ConstantModal.FALSE, null, Closer.LINEARITY, mark, 0));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** The case to assume for {@code u} and {@code v}, empty when their order is known or they have no common earlier state. */
    private Optional<LessEqual> caseOf(List<String> states, String u, String v) {
        var su = successor(u);
        var sv = successor(v);
        if (su.isEmpty() || sv.isEmpty() || entails(su.get(), v) || entails(sv.get(), u) || entails(u, v) && entails(v, u)) {
            return Optional.empty();
        }
        boolean uv = entails(u, v);
        boolean vu = entails(v, u);
        if (uv) {
            return Optional.of(new LessEqual(v, u));
        } else if (vu) {
            return Optional.of(new LessEqual(u, v));
        }
        boolean common = states.stream().anyMatch(w -> entails(w, u) && entails(w, v));
        return common ? Optional.of(new LessEqual(u, v)) : Optional.empty();
    }

    /**
     * Induction on a formula {@code A} of the proof, in its state {@code s}: aim for {@code [] (A -> X A)} in
     * {@code s}, which {@code Ind} turns into {@code [] A}. As in the paper, only when some {@code []} is in the proof.
     */
    private boolean induction() {
        boolean always = steps().stream().anyMatch(step -> step.isValid() && step.getStep() instanceof Always);
        if (!always || goals.stream().anyMatch(goal -> goal.closer == Closer.INDUCTION)) {
            return false;
        }
        for (int i = 1; i <= last(); i++) {
            var step = steps().get(i - 1);
            if (step.isValid() && step.getStep() instanceof ModalLogicalOperation formula && !(formula instanceof Always)
                    && !ConstantModal.FALSE.equals(formula)) {
                String mark = "Ind " + step.getState() + ": " + formula;
                var target = new Always(new ImplicationModal(formula, new Next(formula)));
                if (!has(new Always(formula), step.getState()) && !marks.contains(mark)) {
                    checkpoint(mark);
                    marks.add(mark);
                    goals.add(goal(target, step.getState(), Closer.INDUCTION, mark, i));
                    return true;
                }
            }
        }
        return false;
    }
}
