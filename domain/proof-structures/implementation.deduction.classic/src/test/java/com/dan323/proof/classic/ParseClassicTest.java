package com.dan323.proof.classic;

import com.dan323.classical.ClassicCopy;
import com.dan323.classical.ClassicFE;
import com.dan323.classical.ClassicalAction;
import com.dan323.classical.complex.DeMorgan;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.*;
import org.junit.jupiter.api.Assertions;
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
    public void parseProofPorP() {
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
    public void parseProofFE() {
        var p = new VariableClassic("P");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(ConstantClassic.FALSE), p);
        (new ClassicFE(1,p)).apply(naturalDeduction);
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
    public void parseDeMorgan() {
        var p = new VariableClassic("P");
        var q = new VariableClassic("Q");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(new NegationClassic(new DisjunctionClassic(p,q))),new NegationClassic(p));
        (new DeMorgan(1)).apply(naturalDeduction);
        List<ClassicalAction> actions = naturalDeduction.parse();
        naturalDeduction.initializeProof(List.of(new NegationClassic(new DisjunctionClassic(p,q))),new NegationClassic(p));
        assertEquals(9, actions.size());
        naturalDeduction.initializeProof(List.of(p), p);
        int i = 0;
        for (ClassicalAction action : actions) {
            if (i < naturalDeduction.getAssms().size()) {
                i++;
            } else {
                action.apply(naturalDeduction);
            }
        }
    }

    @Test
    public void parseProofCopy() {
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
    public void parseProofAndE() {
        var p = new VariableClassic("P");
        var q = new VariableClassic("Q");
        var naturalDeduction = new NaturalDeduction();
        naturalDeduction.initializeProof(List.of(new ConjunctionClassic(p,q)), p);
        naturalDeduction.automate();
        String original = naturalDeduction.toString();
        List<ClassicalAction> actions = naturalDeduction.parse();
        assertEquals(2, actions.size());
        naturalDeduction.initializeProof(List.of(new ConjunctionClassic(p,q)), p);
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

    @Test
    public void parseProofOrE() {
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

    @Test
    public void parseProofOrE2() {
        var p = new VariableClassic("P");
        var q = new VariableClassic("Q");
        var naturalDeduction = new NaturalDeduction();
        List<ClassicalLogicOperation> assms = List.of(new DisjunctionClassic(p, q), new NegationClassic(q));
        naturalDeduction.initializeProof(assms, p);
        naturalDeduction.automate();
        String original = naturalDeduction.toString();
        List<ClassicalAction> actions = naturalDeduction.parse();
        assertEquals(9, actions.size());
        naturalDeduction.initializeProof(assms, p);
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
