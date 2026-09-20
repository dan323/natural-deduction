package com.dan323.uses;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.ArrayList;
import java.util.List;

public interface ProofParser<P extends Proof<T, Q>, T extends LogicOperation, Q extends ProofStep<T>, A extends Action<T, Q, P>> {

    default P parseProof(String proof) {
        var actions = parseLines(proof);
        if (actions.isEmpty()) {
            throw new InvalidProofException("The proof is empty");
        }
        try {
            return replay(actions);
        } catch (InvalidProofException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new InvalidProofException("The proof is invalid", e);
        }
    }

    private P replay(List<Q> actions) {
        P nd = getNewProof();
        var assms = ProofParser.extractAssumptions(actions);
        var actualProof = ProofParser.skipInitAssumptions(actions);
        if (actions.getLast().getAssumptionLevel() != 0) {
            throw new InvalidProofException("The proof is invalid: it does not end at the top level");
        }
        nd.initializeProof(assms, actions.getLast().getStep());
        nd.getSteps().addAll(actualProof);
        var parsedActions = (List<A>) nd.parse();
        nd.reset();
        int i = 0;
        for (A action : parsedActions) {
            if (i < nd.getAssms().size()) {
                i++;
            } else if (action.isValid(nd)) {
                action.apply(nd);
            } else {
                throw new InvalidProofException("The proof is invalid: step " + (nd.getSteps().size() + 1) + " does not follow");
            }
        }
        return nd;
    }

    private List<Q> parseLines(String proof) {
        var steps = new ArrayList<Q>();
        int lineNumber = 0;
        for (String line : (Iterable<String>) proof.lines()::iterator) {
            lineNumber++;
            try {
                steps.add(parseLine(line));
            } catch (RuntimeException e) {
                throw new InvalidProofException("Line " + lineNumber + " is not valid: " + describe(e), e);
            }
        }
        return steps;
    }

    private static String describe(RuntimeException e) {
        return e instanceof IllegalArgumentException && e.getMessage() != null && !e.getMessage().isBlank() ? e.getMessage() : "unrecognized format";
    }

    String logic();

    P getNewProof();

    /**
     * Parses one line of a proof file. Implementations report bad lines with a runtime exception, preferably an
     * {@link InvalidProofException}; {@link #parseProof} adds the line number.
     */
    Q parseLine(String line);

    /**
     * The pieces of a proof file line: 3 spaces of indent per assumption level, the expression, an 11-space gap and
     * the rule.
     */
    record ProofLine(int assmsLevel, String expression, String rule) {

        private static final String GAP = " ".repeat(11);

        public static ProofLine split(String line) {
            var startExpression = line.stripLeading();
            if (startExpression.isEmpty()) {
                throw new InvalidProofException("the line is blank");
            }
            int indent = line.length() - startExpression.length();
            if (indent % 3 != 0 || !line.startsWith(" ".repeat(indent))) {
                throw new InvalidProofException("the indentation must be groups of 3 spaces");
            }
            var firstGap = startExpression.indexOf(GAP);
            if (firstGap < 1) {
                throw new InvalidProofException("expected an expression, " + GAP.length() + " spaces and a rule");
            }
            var rule = startExpression.substring(startExpression.lastIndexOf("  ") + 2);
            if (rule.isBlank()) {
                throw new InvalidProofException("the rule is missing");
            }
            return new ProofLine(indent / 3, startExpression.substring(0, firstGap), rule);
        }
    }

    private static <T extends LogicOperation, Q extends ProofStep<T>> List<T> extractAssumptions(List<Q> steps) {
        return steps.stream()
                .takeWhile(action -> action.getAssumptionLevel() == 0 && action.getProof().getNameProof().contains("Ass"))
                .map(ProofStep::getStep)
                .toList();
    }

    private static <T extends LogicOperation, Q extends ProofStep<T>> List<Q> skipInitAssumptions(List<Q> steps) {
        return steps.stream()
                .dropWhile(action -> action.getAssumptionLevel() == 0 && action.getProof().getNameProof().contains("Ass"))
                .toList();
    }
}
