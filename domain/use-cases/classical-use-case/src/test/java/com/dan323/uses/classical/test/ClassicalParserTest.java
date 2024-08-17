package com.dan323.uses.classical.test;

import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.expressions.classical.ImplicationClassic;
import com.dan323.expressions.classical.VariableClassic;
import com.dan323.uses.classical.ParseClassicalProof;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
