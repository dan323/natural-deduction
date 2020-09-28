package com.dan323.service.classical;

import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.model.Action;
import com.dan323.model.Proof;
import com.dan323.model.Step;
import com.dan323.service.ProofService;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dan323.classical.proof.ParseClassicalAction.*;

@Service("classicalService")
public class ClassicalService implements ProofService<ClassicalService.Nothing, String> {

    @Override
    public List<String> initPossibleActions() {
        Reflections reflections = new Reflections("com.dan323.classical");
        return reflections.getSubTypesOf(ClassicalAction.class).stream()
                .filter(clazz -> !clazz.getName().contains("complex"))
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
    }

    @Override
    public String getLogicName() {
        return "classical";
    }

    private NaturalDeduction mapProof(Proof<Nothing> proof) {
        NaturalDeduction nd = new NaturalDeduction();
        List<ClassicalLogicOperation> assmsLst = new ArrayList<>();
        boolean assms = true;
        for (Step<Nothing> step : proof.getSteps()) {
            if (assms && step.getAssmsLevel() == 0) {
                assmsLst.add(parseExpression(step.getExpression()));
            } else {
                if (assms) {
                    assms = false;
                    nd.initializeProof(assmsLst, parseExpression(proof.getGoal()));
                }
                parseWithReason(nd, parseExpression(step.getExpression()), parseReason(step.getRuleString())).apply(nd);
            }
        }
        return nd;
    }

    @Override
    public Proof<Nothing> applyAction(Proof<Nothing> proof, Action<String> action){
        NaturalDeduction nd = mapProof(proof);
        ClassicalAction actionInt = parseAction(action.getName(), action.getSources(), parseExpression(action.getExtraInformation()));
        if (actionInt.isValid(nd)) {
            actionInt.apply(nd);
        }
        return proof;
    }

    protected static class Nothing implements Serializable {
    }
}