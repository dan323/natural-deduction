package com.dan323.model;

import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.classical.proof.ParseClassicalAction;
import com.dan323.expressions.ModalLogicParser;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ParseModalAction;
import com.dan323.proof.modal.proof.ProofStepModal;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author danco
 */
public class Proof<T extends Serializable> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1524625345L;
    private final List<Step<T>> steps;
    private String logic;
    private String goal;

    public Proof() {
        steps = new ArrayList<>();
    }

    public void addStep(Step<T> step) {
        steps.add(step);
    }

    public List<Step<T>> getSteps() {
        return steps;
    }

    public String getLogic() {
        return logic;
    }

    public void setLogic(String logic) {
        this.logic = logic;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public com.dan323.proof.generic.proof.Proof toDomain(String logic) {
        if ("classical".equals(logic)) {
            return mapClassical(this);
        } else if ("modal".equals(logic)) {
            return mapModal(this);
        }
        throw new IllegalArgumentException();
    }

    private com.dan323.proof.generic.proof.Proof mapClassical(Proof<T> proof) {
        NaturalDeduction nd = new NaturalDeduction();
        List<ClassicalLogicOperation> assmsLst = new ArrayList<>();
        boolean assms = true;
        for (Step<T> step : proof.getSteps()) {
            if (assms && step.getAssmsLevel() == 0) {
                assmsLst.add(ParseClassicalAction.parseExpression(step.getExpression()));
            } else {
                if (assms) {
                    assms = false;
                    nd.initializeProof(assmsLst, ParseClassicalAction.parseExpression(proof.getGoal()));
                }
                ParseClassicalAction.parseWithReason(nd, ParseClassicalAction.parseExpression(step.getExpression()), ParseClassicalAction.parseReason(step.getRuleString())).apply(nd);
            }
        }
        return nd;
    }


    private com.dan323.proof.generic.proof.Proof mapModal(Proof<T> proof) {
        ModalNaturalDeduction nd = new ModalNaturalDeduction("s0");
        List<ModalOperation> assmsLst = new ArrayList<>();
        boolean assms = true;
        for (Step<T> step : proof.getSteps()) {
            if (assms && step.getAssmsLevel() == 0) {
                assmsLst.add(ParseModalAction.parseExpression(step.getExpression()));
            } else {
                if (assms) {
                    assms = false;
                    nd.initializeProof(assmsLst, ParseModalAction.parseExpression(proof.getGoal()));
                }
                var parser = new ModalLogicParser();
                ParseModalAction.parseWithReason(nd, ParseModalAction.parseExpression(step.getExpression()), ParseModalAction.parseReason(step.getRuleString()), step.getExtraInformation().getState()).apply(nd);
            }
        }
        return nd;
    }

    public static <T extends Serializable> Proof<T> from(com.dan323.proof.generic.proof.Proof proof) {
        if (proof instanceof NaturalDeduction) {
            var pr = new Proof<>();
            pr.setLogic("classical");
            pr.setGoal(proof.getGoal().toString());
            for (Object obj : proof.getSteps()) {
                var step = (ProofStep) obj;
                pr.addStep(new Step<>(step.getStep().toString(), step.getProof().toString(), step.getAssumptionLevel()));
            }
        } else if (proof instanceof ModalNaturalDeduction) {
            var pr = new Proof<>();
            pr.setLogic("modal");
            pr.setGoal(proof.getGoal().toString());
            for (Object obj : proof.getSteps()) {
                var step = (ProofStepModal) obj;
                var stepDto = new Step<>(step.getStep().toString(), step.getProof().toString(), step.getAssumptionLevel());
                stepDto.setExtraInformation(new Extra("",step.getState()));
                pr.addStep(stepDto);
            }
        }
        throw new IllegalArgumentException();
    }
}
