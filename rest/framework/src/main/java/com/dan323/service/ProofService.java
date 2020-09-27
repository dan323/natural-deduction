package com.dan323.service;

import com.dan323.model.Action;
import com.dan323.model.Proof;

import java.io.Serializable;
import java.util.List;

public interface ProofService<T extends Serializable, Q extends Serializable> {

    List<String> initPossibleActions();

    String getLogicName();

    Proof<T> applyAction(Proof<T> proof, Action<Q> action);

}
