package com.dan323.model;

/**
 * @author danco
 */
public class Rule {

    private String expression;
    private String ruleString;
    private int assmsLevel;

    public Rule(String expression, String ruleString, int assmsLevel) {
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
}
