package com.dan323.model;

import java.io.Serializable;

/**
 * @author danco
 */
public class Step<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 114523452L;
    private String expression;
    private String ruleString;
    private int assmsLevel;
    private T extraInformation;

    public Step(String expression, String ruleString, int assmsLevel) {
        this.expression = expression;
        this.ruleString = ruleString;
        this.assmsLevel = assmsLevel;
    }

    public String getRuleString() {
        return ruleString;
    }

    public void setRuleString(String ruleString) {
        this.ruleString = ruleString;
    }

    public int getAssmsLevel() {
        return assmsLevel;
    }

    public void setAssmsLevel(int assmsLevel) {
        this.assmsLevel = assmsLevel;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public T getExtraInformation() {
        return extraInformation;
    }

    public void setExtraInformation(T extraInformation) {
        this.extraInformation = extraInformation;
    }
}
