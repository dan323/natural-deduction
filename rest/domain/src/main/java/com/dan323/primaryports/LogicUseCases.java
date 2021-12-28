package com.dan323.primaryports;

import com.dan323.exception.InvalidProofException;
import reactor.core.publisher.Flux;

import java.io.Serializable;

public interface LogicUseCases{

    GetAllActions getAllActions(String logic);
    <Q extends Serializable> SaveRule<Q> saveRule(String logic, Proof<Q> proof);

    @FunctionalInterface
    interface GetAllActions{
        Flux<String> perform();
    }

    @FunctionalInterface
    interface SaveRule<Q>{
        void perform() throws InvalidProofException;
    }

}
