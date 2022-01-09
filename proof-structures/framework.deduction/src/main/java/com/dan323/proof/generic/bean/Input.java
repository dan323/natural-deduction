package com.dan323.proof.generic.bean;

import java.util.List;

public interface Input<T> {

    List<Integer> ints();

    String expression();

    T extraInformation();

}
