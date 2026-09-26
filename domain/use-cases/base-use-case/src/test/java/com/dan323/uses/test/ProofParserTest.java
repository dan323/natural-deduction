package com.dan323.uses.test;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.generic.proof.Proof;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.ProofParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProofParserTest {

    @Test
    void emptyProofIsRejectedTest() {
        var proofParser = new ProofParserStub();
        assertThrows(InvalidProofException.class, () -> proofParser.parseProof(""));
    }

    @Test
    void badLineIsReportedWithItsNumberTest() {
        var proofParser = new ProofParserStub();
        var exception = assertThrows(InvalidProofException.class, () -> proofParser.parseProof("ok\nok\nbad"));
        assertTrue(exception.getMessage().startsWith("Line 3 is not valid"), exception.getMessage());
    }

    @Test
    void lineFailureWithAMessageIsReportedTest() {
        var proofParser = new ProofParserStub();
        var text = String.join(System.lineSeparator(), "ok", "illegal");
        var exception = assertThrows(InvalidProofException.class, () -> proofParser.parseProof(text));
        assertEquals("Line 2 is not valid: nonsense in the line", exception.getMessage());
    }

    @Test
    void lineFailureWithoutAUsableMessageIsUnrecognizedFormatTest() {
        var proofParser = new ProofParserStub();
        for (var line : List.of("blank", "noMessage", "notIllegalArgument")) {
            var exception = assertThrows(InvalidProofException.class, () -> proofParser.parseProof(line));
            assertEquals("Line 1 is not valid: unrecognized format", exception.getMessage(), line);
        }
    }

    @Test
    void proofLineSplitTest() {
        var line = ProofParser.ProofLine.split("      P -> Q           ->E [1, 2]");
        assertEquals(new ProofParser.ProofLine(2, "P -> Q", "->E [1, 2]"), line);
    }

    @Test
    void malformedProofLinesTest() {
        assertThrows(InvalidProofException.class, () -> ProofParser.ProofLine.split(""));
        assertThrows(InvalidProofException.class, () -> ProofParser.ProofLine.split("     "));
        assertThrows(InvalidProofException.class, () -> ProofParser.ProofLine.split("P Ass"));
        var noRule = "P" + " ".repeat(11);
        assertThrows(InvalidProofException.class, () -> ProofParser.ProofLine.split(noRule));
        var oneSpace = " P" + " ".repeat(11) + "Ass";
        var tab = "\tP" + " ".repeat(11) + "Ass";
        assertThrows(InvalidProofException.class, () -> ProofParser.ProofLine.split(oneSpace));
        assertThrows(InvalidProofException.class, () -> ProofParser.ProofLine.split(tab));
    }

    @Test
    void proofParserTest() {
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
                    return new ProofStep<>(0, logicexpression, new ProofReason("Ass", List.of(), List.of()));
                }

                @Override
                public void automate() {
                    // Do nothing
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
            switch (line) {
                case "bad" -> throw new InvalidProofException("nonsense");
                case "illegal" -> throw new IllegalArgumentException("nonsense in the line");
                case "blank" -> throw new IllegalArgumentException("  ");
                case "noMessage" -> throw new IllegalArgumentException();
                case "notIllegalArgument" -> throw new IllegalStateException("not shown");
                default -> {
                    // A good line
                }
            }
            return new ProofStep<>(0, new LogicOperation() {
                @Override
                public String toString() {
                    return "P";
                }
            }, new ProofReason("Ass", List.of(), List.of()));
        }
    }
}
