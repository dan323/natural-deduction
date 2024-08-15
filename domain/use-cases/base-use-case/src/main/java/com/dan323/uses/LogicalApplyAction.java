package com.dan323.uses;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public class LogicalApplyAction<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T,Q>, A extends Action<T,Q,P>> implements ActionsUseCases.ApplyAction {

    private final Transformer<T,Q,P,A> logic;

    public LogicalApplyAction(Transformer<T,Q,P,A> transformer){
        this.logic = transformer;
    }

    @Override
    public ProofDto perform(ActionDto action, ProofDto proof) {
        var act = logic.from(action);
        var pr = logic.from(proof);
        if (act.isValid(pr)) {
            act.apply(pr);
        }
        return logic.fromProof(pr);
    }
}
