package com.dan323.proof.classic;

import com.dan323.classical.complex.DeMorgan;
import com.dan323.classical.complex.OrE1;
import com.dan323.classical.complex.OrE2;
import com.dan323.expresions.classical.ConstantClassic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ClassicalComplexTest {

    @Test
    public void equalsTest() {
        var deMorgan = new DeMorgan(1);
        var deMorgan2 = new DeMorgan(2);
        var deMorgan3 = new DeMorgan(2);
        var log = ConstantClassic.TRUE;

        assertEquals(deMorgan, deMorgan);
        assertNotEquals(deMorgan, deMorgan2);
        assertEquals(deMorgan2, deMorgan3);
        assertNotEquals(deMorgan, log);

        var orE11 = new OrE1(1,2);
        var orE12 = new OrE1(1,3);
        var orE13 = new OrE1(2,3);
        var orE14 = new OrE1(2,3);

        assertEquals(orE11, orE11);
        assertNotEquals(orE11, orE12);
        assertNotEquals(orE11, orE13);
        assertNotEquals(orE12, orE13);
        assertEquals(orE13, orE14);
        assertNotEquals(orE11, log);

        var orE21 = new OrE2(1,2);
        var orE22 = new OrE2(1,3);
        var orE23 = new OrE2(2,3);
        var orE24 = new OrE2(2,3);

        assertEquals(orE21, orE21);
        assertNotEquals(orE21, orE22);
        assertNotEquals(orE21, orE23);
        assertNotEquals(orE22, orE23);
        assertEquals(orE23, orE24);
        assertNotEquals(orE21, log);
    }
}
