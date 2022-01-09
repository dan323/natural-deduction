/**
 * Module of modal languages
 */
module language.modal {
    requires transitive language.base;
    requires javaluator;

    exports com.dan323.expressions;
    exports com.dan323.expressions.modal to deduction.modal, deduction.modal.spring;
    exports com.dan323.expressions.relation to deduction.modal, deduction.modal.spring;
}