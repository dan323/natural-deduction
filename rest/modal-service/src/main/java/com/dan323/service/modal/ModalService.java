package com.dan323.service.modal;

import com.dan323.expresions.modal.ModalOperation;
import com.dan323.model.Action;
import com.dan323.model.Proof;
import com.dan323.model.Step;
import com.dan323.proof.modal.AbstractModalAction;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.service.ProofService;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.dan323.proof.modal.proof.ParseModalAction.*;

@Service("modalService")
public class ModalService implements ProofService<String, String> {

    @Override
    public List<String> initPossibleActions() {
        var reflections = new Reflections("com.dan323.proof.modal");
        return reflections.getSubTypesOf(AbstractModalAction.class).stream()
                .map(Class::getSimpleName)
                .filter(st -> !st.contains("Action"))
                .collect(Collectors.toList());
    }

    @Override
    public String getLogicName() {
        return "modal";
    }

    @Override
    public Proof<String> applyAction(Proof<String> proof, Action<String> action) {
        return null;
    }

    private ModalNaturalDeduction<String> mapProof(Proof<String> p) {
        String state0 = p.getSteps().isEmpty() ? "s0" : p.getSteps().get(0).getExtraInformation();
        ModalNaturalDeduction<String> nd = new ModalNaturalDeduction<>(state0);
        List<ModalOperation> assmsLst = new ArrayList<>();
        boolean assms = true;
        for (Step<String> step : p.getSteps()) {
            if (assms && step.getAssmsLevel() == 0) {
                assmsLst.add(parseExpression(step.getExpression()));
            } else {
                if (assms) {
                    assms = false;
                    nd.initializeProof(assmsLst, parseExpression(p.getGoal()));
                }
                parseWithReason(nd, parseExpression(step.getExpression()), parseReason(step.getRuleString()), step.getExtraInformation()).apply(nd);
            }
        }
        return nd;
    }
}
