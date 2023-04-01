package com.dan323.proof.modal;

import com.dan323.expressions.modal.ConjunctionModal;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.relational.Reflexive;
import com.dan323.proof.modal.relational.Transitive;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RelationRuleTest {

    @Test
    public void reflexionTest() {
        var reflexive = new Reflexive(1);
        var mnd = new ModalNaturalDeduction("s0");
        var p = new VariableModal("P");

        mnd.initializeProof(List.of(new ConjunctionModal(p, p)), p);
        assertTrue(reflexive.isValid(mnd));

        reflexive.apply(mnd);
        assertFalse((new Reflexive(2)).isValid(mnd));
        assertEquals(2, mnd.getSteps().size());
    }

    @Test
    public void transitiveTest() {
        var transitive = new Transitive(2,2);
        var mnd = new ModalNaturalDeduction("s0");
        var p = new VariableModal("P");
        var reflexive = new Reflexive(1);

        mnd.initializeProof(List.of(new ConjunctionModal(p, p)), p);
        reflexive.apply(mnd);

        assertTrue(transitive.isValid(mnd));
        transitive.apply(mnd);
        assertEquals(3, mnd.getSteps().size());
        (new ModalAssume(p,"s1")).apply(mnd);
        reflexive = new Reflexive(4);
        reflexive.apply(mnd);
        assertFalse((new Transitive(1,1)).isValid(mnd));
        assertFalse((new Transitive(2,5)).isValid(mnd));
    }
}
