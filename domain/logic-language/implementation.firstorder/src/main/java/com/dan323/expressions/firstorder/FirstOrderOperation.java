package com.dan323.expressions.firstorder;

import com.dan323.expressions.base.LogicOperation;

import java.util.Set;

/**
 * A first-order formula. Formulas are equal (and hash the same) up to renaming of bound variables, so
 * {@code forall x. P(x)} equals {@code forall y. P(y)}. Their {@code toString()} is read back by {@link FirstOrderParser}.
 */
public sealed interface FirstOrderOperation extends LogicOperation
        permits ConjunctionFirstOrder, DisjunctionFirstOrder, ImplicationFirstOrder, NegationFirstOrder,
        ConstantFirstOrder, Predicate, Equals, Quantifier {

    /**
     * @return the names that occur free in this formula (variables and constants look the same)
     */
    Set<String> freeVariables();

    /**
     * Capture-avoiding substitution: replaces the free occurrences of {@code variable} by {@code term}, renaming a bound
     * variable when {@code term} mentions it.
     *
     * @param variable the name to replace
     * @param term     the term that replaces it
     * @return the formula {@code this[variable:=term]}
     */
    FirstOrderOperation substitute(String variable, Term term);
}
