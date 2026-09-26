package com.dan323.uses.modal;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ParseModalAction;
import com.dan323.proof.modal.proof.ProofStepModal;
import com.dan323.uses.ActionExpression;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes modal proofs. A logic that extends modal logic (see {@code ModalNextUntilProofTransformer})
 * overrides the protected methods that read its formulas, rules, actions and states.
 */
public class ModalProofTransformer implements Transformer<ModalOperation, ProofStepModal, ModalNaturalDeduction, AbstractModalAction> {

    /** The key of a step's or an action's state in {@code extraParameters}. */
    private static final String STATE = "state";
    private static final List<ActionDescriptorDto> ACTIONS = new ModalGetActions().perform();

    @Override
    public String logic() {
        return "modal";
    }

    /** The actions of this logic, to know which ones take an expression. */
    protected List<ActionDescriptorDto> actions() {
        return ACTIONS;
    }

    /** An empty proof of this logic that starts in state {@code s0}. */
    protected ModalNaturalDeduction newProof() {
        return new ModalNaturalDeduction("s0");
    }

    protected ModalOperation parseExpression(String expression) {
        return ParseModalAction.parseExpression(expression);
    }

    protected ProofReason parseReason(String rule) {
        return ParseModalAction.parseReason(rule);
    }

    /**
     * @param state a state as the client sends it ({@code extraParameters.state}), or null
     * @return the state as the proof keeps it; in modal logic, {@code state} itself
     */
    protected String parseState(String state) {
        return state;
    }

    protected AbstractModalAction parseWithReason(ModalNaturalDeduction proof, ModalOperation step, ProofReason reason, String state) {
        return ParseModalAction.parseWithReason(proof, step, reason, state);
    }

    protected AbstractModalAction parseAction(String name, List<Integer> sources, ModalOperation expression, String state) {
        return ParseModalAction.parseAction(name, sources, expression, state);
    }

    @Override
    public ModalNaturalDeduction from(ProofDto proof) {
        try {
            return replayProof(proof);
        } catch (InvalidProofException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new InvalidProofException("The proof could not be read, check its expressions and rules", e);
        }
    }

    private ModalNaturalDeduction replayProof(ProofDto proof) {
        ModalNaturalDeduction nd = newProof();
        List<ModalOperation> assmsLst = new ArrayList<>();
        boolean assms = true;
        int line = 0;
        for (StepDto step : proof.steps()) {
            line++;
            if (assms && step.assmsLevel() == 0 && step.rule().equals("Ass")) {
                assmsLst.add(initialAssumption(nd, step, line));
            } else {
                if (assms) {
                    assms = false;
                    nd.initializeProof(assmsLst, parseExpression(proof.goal()));
                }
                replay(nd, step, line);
            }
        }
        if (assms) {
            nd.initializeProof(assmsLst, parseExpression(proof.goal()));
        }
        return nd;
    }

    private ModalOperation initialAssumption(ModalNaturalDeduction nd, StepDto step, int line) {
        var operation = parseExpression(step.expression());
        if (operation instanceof ModalLogicalOperation && !nd.getState0().equals(initialState(step.extraParameters().get(STATE), line))) {
            throw new InvalidProofException("Line " + line + " is not valid: the assumptions are not in a valid state");
        }
        return operation;
    }

    private String initialState(String state, int line) {
        try {
            return parseState(state);
        } catch (RuntimeException e) {
            throw new InvalidProofException("Line " + line + " is not valid: " + e.getMessage(), e);
        }
    }

    private void replay(ModalNaturalDeduction nd, StepDto step, int line) {
        AbstractModalAction action;
        try {
            action = parseWithReason(nd, parseExpression(step.expression()), parseReason(step.rule()), parseState(step.extraParameters().get(STATE)));
        } catch (RuntimeException e) {
            throw new InvalidProofException("Line " + line + " is not valid: cannot read '" + step.expression() + "' justified by '" + step.rule() + "'", e);
        }
        if (!action.isValid(nd)) {
            throw new InvalidProofException("Line " + line + " does not follow: '" + step.rule() + "' cannot justify '" + step.expression() + "'");
        }
        action.apply(nd);
    }

    public AbstractModalAction from(ActionDto action) {
        // Under its modal rule name (the descriptor name), also when it was sent under its classical one (COPY for Rep).
        action = new ActionDto(ParseModalAction.ruleName(action.name()), action.sources(), action.extraParameters());
        var expression = ActionExpression.of(action, actions());
        return parseAction(action.name(), action.sources(), expression.map(this::parseExpression).orElse(null), parseState(action.extraParameters().get(STATE)));
    }

    @Override
    public ProofDto fromProof(ModalNaturalDeduction proof) {
        var logic = logic();
        var goal = proof.getGoal().toString();
        var steps = new ArrayList<StepDto>();
        for (var step : proof.getSteps()) {
            steps.add(new StepDto(step.getStep().toString(), step.getProof().toString(), step.getAssumptionLevel(), step.getState() != null ? Map.of(STATE, step.getState()) : Map.of()));
        }
        return new ProofDto(steps.stream().toList(), logic, goal);

    }
}
