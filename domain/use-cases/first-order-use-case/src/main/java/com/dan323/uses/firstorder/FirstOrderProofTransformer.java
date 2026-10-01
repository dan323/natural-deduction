package com.dan323.uses.firstorder;

import com.dan323.expressions.firstorder.FirstOrderOperation;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ParamKind;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.proof.firstorder.FirstOrderAction;
import com.dan323.proof.firstorder.proof.FirstOrderNaturalDeduction;
import com.dan323.proof.firstorder.proof.ParseFirstOrderAction;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.ActionExpression;
import com.dan323.uses.InvalidActionException;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads and writes first-order proofs. Steps have no {@code extraParameters}. An action's term ({@code ∀E},
 * {@code =I}) is {@code extraParameters.term}, its formula ({@code Ass}, a disjunct, a target) is
 * {@code extraParameters.expression}.
 * <p>
 * The solver is {@link FirstOrderNaturalDeduction#automate()}, a best-effort proof search: when it finds no proof, the
 * proof is left with its premises.
 */
public class FirstOrderProofTransformer implements Transformer<FirstOrderOperation, ProofStep<FirstOrderOperation>, FirstOrderNaturalDeduction, FirstOrderAction> {

    /** The key of an action's term in {@code extraParameters}. */
    public static final String TERM = "term";
    private static final List<ActionDescriptorDto> ACTIONS = new FirstOrderGetActions().perform();

    @Override
    public String logic() {
        return FirstOrderConfiguration.LOGIC;
    }

    @Override
    public boolean hasSolver() {
        return true;
    }

    @Override
    public FirstOrderNaturalDeduction from(ProofDto proof) {
        try {
            return replayProof(proof);
        } catch (InvalidProofException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new InvalidProofException("The proof could not be read, check its expressions and rules", e);
        }
    }

    private static FirstOrderNaturalDeduction replayProof(ProofDto proof) {
        var nd = new FirstOrderNaturalDeduction();
        List<FirstOrderOperation> assmsLst = new ArrayList<>();
        boolean assms = true;
        int line = 0;
        for (StepDto step : proof.steps()) {
            line++;
            if (assms && step.assmsLevel() == 0 && step.rule().equals("Ass")) {
                assmsLst.add(ParseFirstOrderAction.parseExpression(step.expression()));
            } else {
                if (assms) {
                    assms = false;
                    nd.initializeProof(assmsLst, ParseFirstOrderAction.parseExpression(proof.goal()));
                }
                replay(nd, step, line);
            }
        }
        if (assms) {
            nd.initializeProof(assmsLst, ParseFirstOrderAction.parseExpression(proof.goal()));
        }
        return nd;
    }

    private static void replay(FirstOrderNaturalDeduction nd, StepDto step, int line) {
        FirstOrderAction action;
        try {
            action = ParseFirstOrderAction.parseWithReason(nd, ParseFirstOrderAction.parseExpression(step.expression()),
                    ParseFirstOrderAction.parseReason(step.rule()));
        } catch (RuntimeException e) {
            throw new InvalidProofException("Line " + line + " is not valid: cannot read '" + step.expression() + "' justified by '" + step.rule() + "'", e);
        }
        if (!action.isValid(nd)) {
            throw new InvalidProofException("Line " + line + " does not follow: '" + step.rule() + "' cannot justify '" + step.expression() + "'");
        }
        action.apply(nd);
    }

    @Override
    public FirstOrderAction from(ActionDto action) {
        // Under its first-order rule name (the descriptor name), also when it was sent under its classical one.
        var name = ParseFirstOrderAction.ruleName(action.name());
        var expression = ActionExpression.of(action, name, ACTIONS).map(ParseFirstOrderAction::parseExpression).orElse(null);
        var term = term(action, name).map(ParseFirstOrderAction::parseTerm).orElse(null);
        return ParseFirstOrderAction.parseAction(name, action.sources(), expression, term);
    }

    /**
     * @return the text of the action's term, empty when there is none and the action does not need it
     * @throws InvalidActionException when the action takes a term and there is none, or it is blank
     */
    private static Optional<String> term(ActionDto action, String name) {
        var term = Optional.ofNullable(action.extraParameters().get(TERM)).filter(text -> !text.isBlank());
        boolean needsTerm = ACTIONS.stream()
                .anyMatch(descriptor -> descriptor.name().equals(name) && descriptor.params().contains(ParamKind.TERM));
        if (term.isEmpty() && needsTerm) {
            throw new InvalidActionException(action.name() + " needs a term");
        }
        return term;
    }

    @Override
    public ProofDto fromProof(FirstOrderNaturalDeduction proof) {
        var steps = new ArrayList<StepDto>();
        for (var step : proof.getSteps()) {
            steps.add(new StepDto(step.getStep().toString(), step.getProof().toString(), step.getAssumptionLevel(), Map.of()));
        }
        return new ProofDto(List.copyOf(steps), logic(), proof.getGoal().toString());
    }
}
