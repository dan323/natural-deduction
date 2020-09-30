/**
 * Module of modal languages
 */
module language.implementation.modal {
    requires transitive language.base;
    requires javaluator;

    exports com.dan323.expressions.modal;
    exports com.dan323.expressions.relation;
    exports com.dan323.expressions;
}