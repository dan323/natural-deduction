package com.dan323.uses.modal.mock;

import com.dan323.expressions.modal.ImplicationModal;
import com.dan323.expressions.modal.VariableModal;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;

import java.util.List;

public class ModalProof {
        public static ModalNaturalDeduction<String> naturalDeductionNoAssms() {
            var naturalDeduction = new ModalNaturalDeduction<>("s0");
            naturalDeduction.initializeProof(List.of(), new ImplicationModal(new VariableModal("P"), new VariableModal("P")));
            return naturalDeduction;
        }

        public static ModalNaturalDeduction<String> naturalDeductionWithAssms() {
            var naturalDeduction = new ModalNaturalDeduction<>("s0");
            naturalDeduction.initializeProof(List.of(new VariableModal("P")), new VariableModal("P"));
            return naturalDeduction;
        }

        public static ModalNaturalDeduction<String> naturalDeductionNotProvable() {
            var naturalDeduction = new ModalNaturalDeduction<>("s0");
            naturalDeduction.initializeProof(List.of(new VariableModal("P")), new VariableModal("Q"));
            return naturalDeduction;
        }
}
