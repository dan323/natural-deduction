package com.dan323.proof.generic.proof;

import java.util.List;

public interface Step<T> {

    String expression();
    T extraInformation();
    int assmsLevel();
    StepRule ruleString();

    interface StepRule{
        String name();
        List<Integer> lines();
    }
}
