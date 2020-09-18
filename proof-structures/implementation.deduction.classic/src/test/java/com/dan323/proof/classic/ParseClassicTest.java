package com.dan323.proof.classic;

import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expresions.classical.ClassicalLogicOperation;
import com.dan323.expresions.classical.ConjunctionClassic;
import com.dan323.expresions.classical.DisjunctionClassic;
import com.dan323.expresions.classical.ImplicationClassic;
import com.dan323.expresions.classical.VariableClassic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParseClassicTest {

    @Test
    void parseProofPImpP() {
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
    void parseProofPorP() {
        var p = new VariableClassic("P");
        var naturalDeduction = new NaturalDeduction();
        var goal = new DisjunctionClassic(p, p);
        naturalDeduction.initializeProof(List.of(p), goal);
        naturalDeduction.automate();
        String original = naturalDeduction.toString();
        List<ClassicalAction> actions = naturalDeduction.parse();
        assertEquals(2, actions.size());
        naturalDeduction.initializeProof(List.of(p), goal);
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

    @Test
    void parseProofPAndP() {
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

    @Test
    void parseProofOrE() {
        var p = new VariableClassic("P");
        var q = new VariableClassic("Q");
        var r = new VariableClassic("R");
        var naturalDeduction = new NaturalDeduction();
        List<ClassicalLogicOperation> assms = List.of(new DisjunctionClassic(p,q),new ImplicationClassic(p,r), new ImplicationClassic(q,r));
        naturalDeduction.initializeProof(assms, r);
        naturalDeduction.automate();
        String original = naturalDeduction.toString();
        List<ClassicalAction> actions = naturalDeduction.parse();
        assertEquals(19, actions.size());
        naturalDeduction.initializeProof(assms, r);
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
