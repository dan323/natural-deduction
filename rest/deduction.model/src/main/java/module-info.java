/**
 * @author danco
 */
module deduction.model {
     requires framework.deduction;
     requires implementation.deduction.classic;
     requires implementation.deduction.modal;
     requires language.implementation.classical;
     requires language.implementation.modal;

     exports com.dan323.model;
}