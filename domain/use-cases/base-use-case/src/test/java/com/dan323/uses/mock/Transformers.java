package com.dan323.uses.mock;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.model.ActionDto;
import com.dan323.model.ProofDto;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.Transformer;

import java.util.List;

public final class Transformers {

    public static List<Transformer> getTransformers() {
        return List.of(getTransformer("l1"), getTransformer("l2"));
    }

    private static Transformer getTransformer(String logic) {
        return new Transformer() {
            @Override
            public String logic() {
                return logic;
            }

            @Override
            public Proof from(ProofDto proofDto) {
                return new Proof() {
                    @Override
                    public List<? extends Action> parse() {
                        return List.of();
                    }

                    @Override
                    protected ProofStep generateAssm(LogicOperation logicexpression) {
                        return new ProofStep(1, null, null);
                    }

                    @Override
                    public void automate() {

                    }

                    @Override
                    public void initializeProof(List assms, LogicOperation goal) {

                    }
                };
            }

            @Override
            public Action from(ActionDto actionDto) {
                return new Action() {
                    @Override
                    public boolean isValid(Proof pf) {
                        return "l1".equals(logic);
                    }

                    @Override
                    public void apply(Proof pf) {
                        pf.getSteps().add(new ProofStep<>(1, new LogicOperation() {
                            @Override
                            public String toString() {
                                return "P";
                            }
                        }, new ProofReason("R", List.of())));
                    }
                };
            }

            @Override
            public ProofDto fromProof(Proof proof) {
                return new ProofDto(proof.getSteps().stream().map(Object::toString).toList(), logic, "P");
            }

        };
    }
}
