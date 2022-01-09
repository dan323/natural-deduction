/**
 * Module of classic languages
 */
module language.classical{
    requires transitive language.base;
    requires javaluator;

    exports com.dan323.expressions.classical to implementation.deduction.classic, deduction.classic.spring;
    exports com.dan323.expressions;
}