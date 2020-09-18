package com.dan323.proof.classic;

import com.dan323.expresions.classical.ConjunctionClassic;
import com.dan323.expresions.classical.ImplicationClassic;
import com.dan323.expresions.classical.VariableClassic;
import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.NaturalDeduction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseClassicTest {

    @Test
    public void parseProofPImpP() {
        var p = new VariableClassic("P");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(), new ImplicationClassic(p, p));
        naturalDeduction.automate();
        String original = naturalDeduction.toString();
        List<ClassicalAction> actions = naturalDeduction.parse();
        assertEquals(2, actions.size());
        naturalDeduction.initializeProof(List.of(), new ImplicationClassic(p, p));
        for (ClassicalAction action : actions) {
            action.apply(naturalDeduction);
        }
        assertEquals(original, naturalDeduction.toString());
    }

    @Test
    public void parseProofPAndP() {
        var p = new VariableClassic("P");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(p), new ConjunctionClassic(p, p));
        naturalDeduction.automate();
        String original = naturalDeduction.toString();
        List<ClassicalAction> actions = naturalDeduction.parse();
        assertEquals(2, actions.size());
        naturalDeduction.initializeProof(List.of(p), new ConjunctionClassic(p, p));
        int i = 0;
        for (ClassicalAction action : actions) {
            if (i < naturalDeduction.getAssms().size()) {
                i++;
            } else {
                action.apply(naturalDeduction);
            }
        }
        assertEquals(original, naturalDeduction.toString());
    }

}
