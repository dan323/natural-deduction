package com.dan323.proof.modal;

import com.dan323.expresions.modal.*;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.relational.Reflexive;
import com.dan323.proof.modal.relational.Transitive;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseModalActionTest {

    @Test
    public void parseAndETest() {
        var mnd = new ModalNaturalDeduction<>("s0");
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        mnd.initializeProof(List.of(new ConjunctionModal(p, q)), p);
        var conj1 = new ModalAndE1<String>(1);
        conj1.apply(mnd);
        List<AbstractModalAction<String>> lst = mnd.parse();
        assertEquals(2, lst.size());
        mnd.initializeProof(List.of(new ConjunctionModal(p, q)), p);
        assertEquals(conj1, lst.get(1));
        assertEquals(new ModalAssume<>(new ConjunctionModal(p, q), "s0"), lst.get(0));

        mnd.initializeProof(List.of(new ConjunctionModal(p, q)), q);
        var conj2 = new ModalAndE2<String>(1);
        conj2.apply(mnd);
        lst = mnd.parse();
        assertEquals(2, lst.size());
        mnd.initializeProof(List.of(new ConjunctionModal(p, q)), q);
        assertEquals(conj2, lst.get(1));
    }

    @Test
    public void parseAndITest() {
        var mnd = new ModalNaturalDeduction<>("s0");
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        mnd.initializeProof(List.of(p, q), new ConjunctionModal(p, q));
        var conj = new ModalAndI<String>(1, 2);
        conj.apply(mnd);
        List<AbstractModalAction<String>> lst = mnd.parse();
        assertEquals(3, lst.size());
        mnd.initializeProof(List.of(new ConjunctionModal(p, q)), p);
        assertEquals(conj, lst.get(2));
    }

    @Test
    public void parseOrITest() {
        var mnd = new ModalNaturalDeduction<>("s0");
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        mnd.initializeProof(List.of(p), new DisjunctionModal(p, q));
        var conj1 = new ModalOrI1<String>(1, q);
        conj1.apply(mnd);
        List<AbstractModalAction<String>> lst = mnd.parse();
        assertEquals(2, lst.size());
        mnd.initializeProof(List.of(p), new DisjunctionModal(p, q));
        assertEquals(conj1, lst.get(1));

        mnd.initializeProof(List.of(p), new DisjunctionModal(q, p));
        var conj2 = new ModalOrI2<String>(1, q);
        conj2.apply(mnd);
        lst = mnd.parse();
        assertEquals(2, lst.size());
        mnd.initializeProof(List.of(p), new DisjunctionModal(q, p));
        assertEquals(conj2, lst.get(1));
    }

    @Test
    public void parseOrETest() {
        var mnd = new ModalNaturalDeduction<>("s0");
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        var r = new VariableModal("R");
        mnd.initializeProof(List.of(new DisjunctionModal(p, q), new ImplicationModal(p, r), new ImplicationModal(q, r)), r);
        var conj1 = new ModalOrE<String>(1, 2, 3);
        conj1.apply(mnd);
        List<AbstractModalAction<String>> lst = mnd.parse();
        assertEquals(4, lst.size());
        mnd.initializeProof(List.of(new DisjunctionModal(p, q), new ImplicationModal(p, r), new ImplicationModal(q, r)), r);
        assertEquals(conj1, lst.get(3));
    }

    @Test
    public void parseCopyRelations() {
        var mnd = new ModalNaturalDeduction<>("s0");
        var p = new VariableModal("P");
        var q = new VariableModal("Q");
        var refl = new Reflexive<String>(1);
        var trans = new Transitive<String>(4, 5);
        mnd.initializeProof(List.of(p, q), q);
        var copy = new ModalCopy<String>(1);
        copy.apply(mnd);
        refl.apply(mnd);
        refl.apply(mnd);
        trans.apply(mnd);
        var lst = mnd.parse();
        assertEquals(copy, lst.get(2));
        assertEquals(refl, lst.get(3));
        assertEquals(trans, lst.get(5));
    }

    @Test
    public void parseNot() {
        var mnd = new ModalNaturalDeduction<>("s0");
        var p = new VariableModal("P");
        mnd.initializeProof(List.of(), new ImplicationModal(p, new NegationModal(new NegationModal(p))));
        (new ModalAssume<>(p, "s0")).apply(mnd);
        (new ModalAssume<>(new NegationModal(new NegationModal(new NegationModal(p))), "s0")).apply(mnd);
        (new ModalNotE<String>(2)).apply(mnd);
        (new ModalFI<>("s0", 1, 3)).apply(mnd);
        (new ModalNotI<String>()).apply(mnd);
        (new ModalNotE<String>(5)).apply(mnd);
        (new ModalNotE<String>(6)).apply(mnd);
        var lst = mnd.parse();
        assertEquals(7, lst.size());
        assertEquals(new ModalNotE<String>(6), lst.get(6));
        assertEquals(new ModalNotI<>(), lst.get(4));
    }
}
