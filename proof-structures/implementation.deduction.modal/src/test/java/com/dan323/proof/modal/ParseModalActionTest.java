package com.dan323.proof.modal;

import com.dan323.expresions.modal.ConjunctionModal;
import com.dan323.expresions.modal.VariableModal;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
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
        List<ModalAction<String>> lst = mnd.parse();
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
        mnd.initializeProof(List.of(p,q), new ConjunctionModal(p,q));
        var conj = new ModalAndI<String>(1,2);
        conj.apply(mnd);
        List<ModalAction<String>> lst = mnd.parse();
        assertEquals(2, lst.size());
        mnd.initializeProof(List.of(new ConjunctionModal(p, q)), p);
        assertEquals(conj, lst.get(1));
    }
}
