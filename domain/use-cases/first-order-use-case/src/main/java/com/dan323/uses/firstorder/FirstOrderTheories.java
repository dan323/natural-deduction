package com.dan323.uses.firstorder;

import com.dan323.model.TheoryDto;
import com.dan323.uses.LogicalTheories;

import java.util.List;

/**
 * The theories of first-order logic. {@code group}: the group axioms for the operation {@code m}, the identity
 * {@code e} and the inverse {@code i}. {@code e} is a free name in the premises, so it acts as a constant.
 */
public class FirstOrderTheories implements LogicalTheories {

    /** The group axioms: associativity, identity and inverses. The group exercises start from exactly these. */
    public static final List<String> GROUP_AXIOMS = List.of(
            "forall x. forall y. forall z. m(m(x, y), z) = m(x, m(y, z))",
            "forall x. m(e, x) = x & m(x, e) = x",
            "forall x. m(i(x), x) = e & m(x, i(x)) = e");

    private static final List<TheoryDto> THEORIES = List.of(new TheoryDto("group", "Group", GROUP_AXIOMS));

    @Override
    public String logic() {
        return FirstOrderConfiguration.LOGIC;
    }

    @Override
    public List<TheoryDto> theories() {
        return THEORIES;
    }
}
