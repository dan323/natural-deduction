package com.dan323.proof.generic.bean;

import com.dan323.expressions.base.LogicOperation;
import com.dan323.proof.generic.Action;

import java.util.List;
import java.util.function.Supplier;

public interface Actions<K extends Input<?>, L extends LogicOperation, A extends Action<L, ?, ?>> extends LogicAware, Supplier<List<String>> {

    Construct<K, L, A> getAction(String name);

}
