package com.dan323.expressions.firstorder;

import java.util.Set;

/**
 * A first-order term: a variable (or constant) name or a function application.
 * Terms bind no variables, so they are equal exactly when they are structurally equal.
 */
public sealed interface Term permits VariableTerm, FunctionApplication {

    /**
     * @return the names that occur in this term (variables and constants look the same)
     */
    Set<String> freeVariables();

    /**
     * @param variable the name to replace
     * @param term     the term that replaces it
     * @return this term with every occurrence of {@code variable} replaced by {@code term}
     */
    Term substitute(String variable, Term term);
}
