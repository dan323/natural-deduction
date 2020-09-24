package com.dan323.proof.modal;

import com.dan323.expresions.modal.ModalOperation;
import com.dan323.proof.generic.Action;
import com.dan323.proof.modal.proof.ModalNaturalDeduction;
import com.dan323.proof.modal.proof.ProofStepModal;

public interface AbstractModalAction<T> extends Action<ModalOperation, ProofStepModal<T>, ModalNaturalDeduction<T>> {
}
