package com.dan323.classical.internal;

import com.dan323.classical.ClassicAssume;
import com.dan323.classical.ClassicCopy;
import com.dan323.classical.ClassicDeductionTheorem;
import com.dan323.classical.ClassicFI;
import com.dan323.classical.ClassicModusPonens;
import com.dan323.classical.ClassicNotI;
import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.classical.ConstantClassic;
import com.dan323.expressions.classical.ImplicationClassic;
import com.dan323.expressions.classical.NegationClassic;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.function.ToIntFunction;

/**
 * The steps that the automatic solvers of classical and intuitionistic logic write when they translate a sequent proof
 * into natural deduction. Both keep a context: the line (1-based) of each formula they may use, all of them valid and
 * in scope.
 */
final class ClassicalSteps {

    static final ClassicalLogicOperation FALSE = ConstantClassic.FALSE;

    private final NaturalDeduction proof;
    private final String solver;

    /**
     * @param proof  the proof the steps are written into
     * @param solver the name of the solver, for the message of an invalid step
     */
    ClassicalSteps(NaturalDeduction proof, String solver) {
        this.proof = proof;
        this.solver = solver;
    }

    static void checkNotInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException("The automatic solver was interrupted");
        }
    }

    /**
     * The antecedent and consequent of an implication, or of a negation read as an implication to {@code FALSE}.
     */
    record Arrow(ClassicalLogicOperation antecedent, ClassicalLogicOperation consequent) {
    }

    static Arrow arrow(ClassicalLogicOperation formula) {
        if (formula instanceof ImplicationClassic implication) {
            return new Arrow(implication.getLeft(), implication.getRight());
        }
        if (formula instanceof NegationClassic negation) {
            return new Arrow(negation.getElement(), FALSE);
        }
        return null;
    }

    /**
     * The line of {@code formula} in {@code context}, which must be there.
     */
    int line(Map<ClassicalLogicOperation, Integer> context, ClassicalLogicOperation formula) {
        Integer line = context.get(formula);
        if (line == null) {
            throw new IllegalStateException("The " + solver + " solver lost the line of " + formula);
        }
        return line;
    }

    /**
     * Proves the implication or negation {@code target} in a subproof: assumes its antecedent, runs {@code body} (which
     * gives the line of the consequent), repeats the consequent if it is not the last line, and discharges the
     * assumption with {@code ->I} or {@code -I}, as {@code target} is an implication or a negation.
     *
     * @return the line of {@code target}
     */
    int hypothetical(Map<ClassicalLogicOperation, Integer> context, ClassicalLogicOperation target,
                     ToIntFunction<Map<ClassicalLogicOperation, Integer>> body) {
        var antecedent = arrow(target).antecedent();
        var inner = new HashMap<>(context);
        inner.put(antecedent, apply(new ClassicAssume(antecedent)));
        int consequent = body.applyAsInt(inner);
        if (consequent != lastLine()) {
            apply(new ClassicCopy(consequent));
        }
        return apply(target instanceof NegationClassic ? new ClassicNotI() : new ClassicDeductionTheorem());
    }

    /**
     * Applies the implication or negation at line {@code arrowLine} to its antecedent at line {@code antecedentLine}:
     * {@code ->E} or {@code FI}.
     *
     * @return the line of the consequent
     */
    int applyArrow(int arrowLine, int antecedentLine) {
        if (proof.getSteps().get(arrowLine - 1).getStep() instanceof NegationClassic) {
            return apply(new ClassicFI(antecedentLine, arrowLine));
        }
        return apply(new ClassicModusPonens(arrowLine, antecedentLine));
    }

    int apply(ClassicalAction action) {
        if (!action.isValid(proof)) {
            throw new IllegalStateException("The " + solver + " solver built an invalid step: " + action.getAction());
        }
        action.apply(proof);
        return lastLine();
    }

    int lastLine() {
        return proof.getSteps().size();
    }
}
