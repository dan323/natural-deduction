package com.dan323.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SequenceRule implements Serializable {

    private Proof proof;
    private Map<String, String> expressions;
    private static final long serialVersionUID = 15568243L;

    public Proof getProof() {
        return proof;
    }

    public void setProof(Proof proof) {
        this.proof = proof;
    }

    public Map<String, String> getExpressions() {
        return expressions;
    }

    public void setExpressions(Map<String, String> expressions) {
        this.expressions = expressions;
    }

    public void setExpresion(String variable, String expression) {
        if (expressions == null) {
            expressions = new HashMap<>();
        }
        expressions.put(variable, expression);
    }
}
