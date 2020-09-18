package com.dan323.proof.classic;

import com.dan323.classical.ClassicCopy;
import com.dan323.classical.ClassicalAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expresions.classical.*;
import org.junit.jupiter.api.Assertions;
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
    void parseProofFE() {
        var p = new VariableClassic("P");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(ConstantClassic.FALSE), p);
        naturalDeduction.automate();
        String original = naturalDeduction.toString();
        List<ClassicalAction> actions = naturalDeduction.parse();
        assertEquals(2, actions.size());
        naturalDeduction.initializeProof(List.of(ConstantClassic.FALSE), p);
        int i = 0;
        for (ClassicalAction action : actions) {
            if (i < naturalDeduction.getAssms().size()) {
                i++;
            } else {
                action.apply(naturalDeduction);
            }
        }
        Assertions.assertEquals(original, naturalDeduction.toString());
    }

    @Test
    void parseProofCopy() {
        var p = new VariableClassic("P");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(p), p);
        (new ClassicCopy(1)).apply(naturalDeduction);
        String original = naturalDeduction.toString();
        List<ClassicalAction> actions = naturalDeduction.parse();
        assertEquals(2, actions.size());
        naturalDeduction.initializeProof(List.of(p), p);
        int i = 0;
        for (ClassicalAction action : actions) {
            if (i < naturalDeduction.getAssms().size()) {
                i++;
            } else {
                action.apply(naturalDeduction);
            }
        }
        Assertions.assertEquals(original, naturalDeduction.toString());
    }


    @Test
    void parseProofAndE() {
        var p = new VariableClassic("P");
        var q = new VariableClassic("Q");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(new ConjunctionClassic(p,q)), p);
        naturalDeduction.automate();
        String original = naturalDeduction.toString();
        List<ClassicalAction> actions = naturalDeduction.parse();
        assertEquals(2, actions.size());
        naturalDeduction.initializeProof(List.of(p), p);
        int i = 0;
        for (ClassicalAction action : actions) {
            if (i < naturalDeduction.getAssms().size()) {
                i++;
            } else {
                action.apply(naturalDeduction);
            }
        }
        Assertions.assertEquals(original, naturalDeduction.toString());
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
        List<ClassicalLogicOperation> assms = List.of(new DisjunctionClassic(p, q), new ImplicationClassic(p, r), new ImplicationClassic(q, r));
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
