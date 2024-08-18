package com.dan323.uses.mock;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.Assume;
import com.dan323.proof.generic.Copy;
import com.dan323.proof.generic.DeductionTheorem;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.ProofParser;

import java.util.List;

public final class Parsers {

    private static final LogicOperation P = new LogicOperation() {
        @Override
        public String toString() {
            return "P";
        }
    };
    private static final LogicOperation Q = new LogicOperation() {
        @Override
        public String toString() {
            return "Q";
        }
    };
    private static final ProofReason ASS = new ProofReason("Ass", List.of());


    static ProofParser parser(String logic) {
        return new ProofParser() {
            @Override
            public Proof parseProof(String proof) {
                return new Proof() {

                    @Override
                    public List getSteps() {
                        return List.of(new ProofStep<>(0, P, ASS),
                                new ProofStep<>(1, Q, ASS),
                                new ProofStep<>(1, P, new ProofReason("Rep", List.of(1))),
                                new ProofStep<>(0, new LogicOperation() {
                                    @Override
                                    public String toString() {
                                        return "Q -> P";
                                    }
                                }, new ProofReason("->I", List.of(2, 3))));
                    }

                    @Override
                    public List<? extends Action> parse() {
                        return List.of(new Assume(P) {
                            @Override
                            public void apply(Proof pf) {
                                pf.getSteps().add(new ProofStep<>(0, P, ASS));
                            }
                        }, new Assume(Q) {
                            @Override
                            public void apply(Proof pf) {
                                pf.getSteps().add(new ProofStep<>(1, Q, ASS));
                            }
                        }, new Copy(1) {
                            @Override
                            public void apply(Proof pf) {
                                pf.getSteps().add(new ProofStep<>(1, P, new ProofReason("Rep", List.of(1))));
                            }
                        }, new DeductionTheorem((x, y) -> new LogicOperation() {
                            @Override
                            public String toString() {
                                return "Q -> P";
                            }
                        }) {
                            @Override
                            public void apply(Proof pf) {
                                pf.getSteps().add(new ProofStep<>(0, new LogicOperation() {
                                    @Override
                                    public String toString() {
                                        return "Q -> P";
                                    }
                                }, new ProofReason("->I [2,3]", List.of(1))));
                            }
                        });
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

            @Override
            public Proof getNewProof() {
                throw new UnsupportedOperationException();
            }

            @Override
            public ProofStep parseLine(String line) {
                throw new UnsupportedOperationException();
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
