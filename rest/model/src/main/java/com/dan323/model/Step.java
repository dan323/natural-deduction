package com.dan323.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author danco
 */
public class Step<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 114523452L;
    private String expression;
    private String ruleString;
    private int assmsLevel;
    private Extra extraInformation;

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

    public Extra getExtraInformation() {
        return extraInformation;
    }

    public void setExtraInformation(Extra extraInformation) {
        this.extraInformation = extraInformation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Step)) return false;

        Step<?> step = (Step<?>) o;

        return Objects.equals(extraInformation, step.extraInformation) &&
                Objects.equals(ruleString, step.ruleString) &&
                Objects.equals(expression, step.expression) &&
                assmsLevel == step.assmsLevel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(assmsLevel, ruleString, expression, extraInformation);
    }
}
