/**
 * @author danco
 */
module rest.model {
    requires framework.deduction;
    requires implementation.deduction.classic;
    requires implementation.deduction.modal;
    requires language.implementation.classical;
    requires language.implementation.modal;
    requires use.cases;
    requires deduction.model;

    exports com.dan323.rest.model;
}