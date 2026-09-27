/**
 * Natural deduction for first-order logic with equality: the propositional rules bound to the first-order language
 * and the quantifier and equality rules
 */
module implementation.deduction.firstorder {
    exports com.dan323.proof.firstorder;
    exports com.dan323.proof.firstorder.proof;
    requires framework.deduction;
    requires language.implementation.firstorder;
}
