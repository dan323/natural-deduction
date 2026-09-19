package com.dan323.uses.classical.test;

import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.classical.ImplicationClassic;
import com.dan323.expressions.classical.VariableClassic;
import com.dan323.proof.generic.proof.ProofReason;
import com.dan323.proof.generic.proof.ProofStep;
import com.dan323.uses.InvalidProofException;
import com.dan323.uses.classical.ParseClassicalProof;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClassicalParserTest {

    private static final ClassicalLogicOperation P = new VariableClassic("P");
    private static final ClassicalLogicOperation Q = new VariableClassic("Q");
    private static final ClassicalLogicOperation QimpP = new ImplicationClassic(Q, P);

    @Test
    public void parseProofSuccessfully() {
        ParseClassicalProof parser = new ParseClassicalProof();
        NaturalDeduction nd = new NaturalDeduction();
        nd.initializeProof(List.of(P), QimpP);
        nd.automate();
        var parsedProof = parser.parseProof(nd.toString());
        assertEquals(4, parsedProof.getSteps().size());
        assertEquals(nd.toString(), parsedProof.toString());
    }

    @Test
    public void parseProofSuccessfulFailed() {
        ParseClassicalProof parser = new ParseClassicalProof();
        NaturalDeduction nd = new NaturalDeduction();
        nd.initializeProof(List.of(P), QimpP);
        var parsedProof = parser.parseProof(nd.toString());
        assertEquals(1, parsedProof.getSteps().size());
        assertNotEquals(nd.getGoal(), parsedProof.getGoal());
    }


    @Test
    public void parseProofFailed() {
        ParseClassicalProof parser = new ParseClassicalProof();
        NaturalDeduction nd = new NaturalDeduction();
        nd.initializeProof(List.of(P), QimpP);
        nd.getSteps().add(new ProofStep<>(1, Q, new ProofReason("Ass", List.of(), List.of())));
        var exception = assertThrowsExactly(InvalidProofException.class, () -> parser.parseProof(nd.toString()));
        assertTrue(exception.getMessage().contains("invalid"));
    }

    @Test
    public void parseEmptyFile() {
        var parser = new ParseClassicalProof();
        assertThrows(InvalidProofException.class, () -> parser.parseProof(""));
    }

    @Test
    public void parseGarbledFileReportsTheLine() {
        var parser = new ParseClassicalProof();
        var goodLine = "P" + " ".repeat(11) + "Ass";
        var blank = assertThrows(InvalidProofException.class, () -> parser.parseProof(goodLine + "\n\n" + goodLine));
        assertTrue(blank.getMessage().startsWith("Line 2 "), blank.getMessage());
        var noSeparator = assertThrows(InvalidProofException.class, () -> parser.parseProof(goodLine + "\nP Ass"));
        assertTrue(noSeparator.getMessage().startsWith("Line 2 "), noSeparator.getMessage());
        var badExpression = assertThrows(InvalidProofException.class, () -> parser.parseProof("P Q" + " ".repeat(11) + "Ass"));
        assertTrue(badExpression.getMessage().startsWith("Line 1 "), badExpression.getMessage());
        var badRule = assertThrows(InvalidProofException.class, () -> parser.parseProof("P" + " ".repeat(11) + "Nope"));
        assertTrue(badRule.getMessage().startsWith("Line 1 "), badRule.getMessage());
    }
}
