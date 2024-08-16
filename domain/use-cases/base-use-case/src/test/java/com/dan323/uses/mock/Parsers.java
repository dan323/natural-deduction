package com.dan323.uses.mock;

import com.dan323.classical.ClassicAssume;
import com.dan323.classical.ClassicCopy;
import com.dan323.classical.ClassicDeductionTheorem;
import com.dan323.expressions.base.LogicOperation;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.classical.ImplicationClassic;
import com.dan323.expressions.classical.VariableClassic;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.ProofParser;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;

import java.util.List;

public final class Parsers {

    private static final ClassicalLogicOperation P = new VariableClassic("P");
    private static final ClassicalLogicOperation Q = new VariableClassic("Q");
    private static final ProofReason ASS = new ProofReason("Ass", List.of());


    static ProofParser parser(String logic) {
        return new ProofParser() {
            @Override
            public Proof parseProof(String proof) {
                return new Proof() {

                    @Override
                    public List getSteps() {
                        return List.of(new ProofStep<>(0, P, ASS),
                                new ProofStep<LogicOperation>(1, Q, ASS),
                                new ProofStep<>(1, P, new ProofReason("Rep", List.of(1))),
                                new ProofStep<LogicOperation>(0, new ImplicationClassic(Q, P), new ProofReason("->I", List.of(2, 3))));
                    }

                    @Override
                    public List<? extends Action> parse() {
                        return List.of(new ClassicAssume(new VariableClassic("P")), new ClassicAssume(new VariableClassic("Q")),
                                new ClassicCopy(1), new ClassicDeductionTheorem());
                    }

                    @Override
                    protected ProofStep generateAssm(LogicOperation logicexpression) {
                        return new ProofStep(0, logicexpression, new ProofReason("Ass", List.of()));
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
            public String logic() {
                return logic;
            }
        };
    }

    public static List<ProofParser> parsers() {
        return List.of(parser("l1"), parser("l2"));
    }

    public static int expectedLength() {
        return parser("l").parseProof("ANYTHING").getSteps().size();
    }
}
