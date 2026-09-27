package com.dan323.expressions.firstorder;

import com.dan323.expressions.base.BinaryOperation;
import com.dan323.expressions.base.UnaryOperation;

/**
 * Printing of the binary connectives. It follows the framework's {@link BinaryOperation#toString()} (parentheses around
 * a binary or unary operand) and also puts them around a quantifier operand: on the left its body would otherwise reach
 * over the connective, and on the right they are not needed but make the scope plain.
 */
final class Printer {

    private Printer() {
    }

    static String binary(FirstOrderOperation left, String operator, FirstOrderOperation right) {
        return operand(left) + " " + operator + " " + operand(right);
    }

    private static String operand(FirstOrderOperation operand) {
        return operand instanceof BinaryOperation || operand instanceof UnaryOperation || operand instanceof Quantifier
                ? "(" + operand + ")" : operand.toString();
    }
}
