package com.dan323.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SequenceRule<T extends Serializable> implements Serializable {

    private Proof<T> proof;
    private Map<String, String> expressions;
    private static final long serialVersionUID = 15568243L;

    public Proof<T> getProof() {
        return proof;
    }

    public void setProof(Proof<T> proof) {
        this.proof = proof;
    }

    public Map<String, String> getExpressions() {
        return expressions;
    }

    public void setExpressions(Map<String, String> expressions) {
        this.expressions = expressions;
    }

    public void setExpression(String variable, String expression) {
        if (expressions == null) {
            expressions = new HashMap<>();
        }
        expressions.put(variable, expression);
    }
}
