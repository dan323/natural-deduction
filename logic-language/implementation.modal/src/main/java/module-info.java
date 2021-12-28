/**
 * Module of modal languages
 */
module language.implementation.modal {
    requires transitive language.base;
    requires javaluator;

    exports com.dan323.expressions;
    exports com.dan323.expressions.modal to implementation.deduction.modal, deduction.modal.spring;
    exports com.dan323.expressions.relation to implementation.deduction.modal;
}