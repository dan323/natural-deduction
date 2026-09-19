package com.dan323.uses;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofStep;

public class LogicalApplyAction<T extends LogicOperation, Q extends ProofStep<T>, P extends Proof<T, Q>, A extends Action<T, Q, P>> implements ActionsUseCases.ApplyAction {

    private final Transformer<T, Q, P, A> logic;

    public LogicalApplyAction(Transformer<T, Q, P, A> transformer) {
        this.logic = transformer;
    }

    @Override
    public ActionsUseCases.ApplyResult perform(ActionDto action, ProofDto proof) {
        var act = buildAction(action);
        var pr = logic.from(proof);
        var lines = action.sources();
        for (int line : lines) {
            if (line < 1 || line > pr.getSteps().size()) {
                return ActionsUseCases.ApplyResult.rejected(proof, "Line " + line + " does not exist, the proof has " + pr.getSteps().size() + " lines");
            }
        }
        if (!act.isValid(pr)) {
            var where = lines.isEmpty() ? "" : " to lines " + lines;
            return ActionsUseCases.ApplyResult.rejected(proof, "Rule " + action.name() + " cannot be applied" + where);
        }
        act.apply(pr);
        return ActionsUseCases.ApplyResult.applied(logic.fromProof(pr));
    }

    private A buildAction(ActionDto action) {
        try {
            return logic.from(action);
        } catch (RuntimeException e) {
            throw new InvalidActionException("Cannot build action '" + action.name() + "': " + e.getMessage(), e);
        }
    }
}
