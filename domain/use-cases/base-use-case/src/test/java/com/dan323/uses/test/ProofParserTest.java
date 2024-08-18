package com.dan323.uses.test;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.ProofParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProofParserTest {

    @Test
    public void proofParserTest() {
        var proofParser = new ProofParserStub();
        var proof = proofParser.parseProof("\n".repeat(5));
        assertEquals(5, proof.getSteps().size());
    }


    private static final class ProofParserStub implements ProofParser<Proof<LogicOperation, ProofStep<LogicOperation>>, LogicOperation, ProofStep<LogicOperation>, Action<LogicOperation, ProofStep<LogicOperation>, Proof<LogicOperation, ProofStep<LogicOperation>>>> {

        @Override
        public String logic() {
            return "l1";
        }

        @Override
        public Proof<LogicOperation, ProofStep<LogicOperation>> getNewProof() {
            return new Proof<>() {
                @Override
                public List<? extends Action<LogicOperation, ProofStep<LogicOperation>, ? extends Proof<LogicOperation, ProofStep<LogicOperation>>>> parse() {
                    return List.of();
                }

                @Override
                protected ProofStep<LogicOperation> generateAssm(LogicOperation logicexpression) {
                    return new ProofStep<>(0, logicexpression, new ProofReason("Ass", List.of()));
                }

                @Override
                public void automate() {

                }

                @Override
                public void initializeProof(List<LogicOperation> assms, LogicOperation goal) {
                    getSteps().clear();
                    setAssms(assms);
                }
            };
        }

        @Override
        public ProofStep<LogicOperation> parseLine(String line) {
            return new ProofStep<>(0, new LogicOperation() {
                @Override
                public String toString() {
                    return "P";
                }
            }, new ProofReason("Ass", List.of()));
        }
    }
}
