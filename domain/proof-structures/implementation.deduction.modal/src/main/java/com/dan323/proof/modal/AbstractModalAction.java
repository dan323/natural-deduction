package com.dan323.proof.modal;

import com.dan323.expressions.modal.ModalOperation;
import com.dan323.proof.generic.AbstractAction;
import com.dan323.proof.generic.Action;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public interface AbstractModalAction extends Action<ModalOperation, ProofStepModal, ModalNaturalDeduction>, AbstractAction<ModalOperation, ProofStepModal, ModalNaturalDeduction> {
}
