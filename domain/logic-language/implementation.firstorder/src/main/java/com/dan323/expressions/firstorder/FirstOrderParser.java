package com.dan323.expressions.firstorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A hand-written recursive-descent parser for first-order formulas. The other languages use javaluator, but it cannot
 * read this one: it only knows function symbols declared up front (so {@code f(x) = y} or {@code P(x, y)} with arbitrary
 * names fail), and its operators are fixed symbols that cannot bind a variable ({@code forall x. P & Q} is read as the
 * name {@code forall x. P} conjoined with {@code Q}).
 * <p>
 * Grammar, from the loosest binding to the tightest (the connectives keep the precedence the classical and modal
 * parsers give them, all left-associative):
 * <pre>
 * formula := or ('&amp;' or)*
 * or      := imp ('|' imp)*
 * imp     := unary ('-&gt;' unary)*
 * unary   := '-' unary | ('forall' | 'exists') var '.' formula | primary
 * primary := term '=' term | '(' formula ')' | 'TRUE' | 'FALSE' | name ['(' term (',' term)* ')']
 * term    := '(' term ')' | name ['(' term (',' term)* ')']
 * </pre>
 * A quantifier body reaches as far right as it can, so {@code forall x. P(x) & Q} quantifies over the conjunction.
 * Terms (variables, constants and function symbols) are identifiers that start with a lowercase letter; a predicate
 * name is any identifier, and one without arguments is a propositional variable, so {@code p -> q} parses.
 * {@code forall}, {@code exists}, {@code TRUE} and {@code FALSE} are reserved. Nesting (parentheses, negations,
 * quantifiers, function arguments) is limited to {@value #MAX_DEPTH} levels, so deep input cannot overflow the stack.
 * Every error is an {@link IllegalArgumentException}.
 */
public final class FirstOrderParser {

    private static final Set<String> RESERVED = Set.of(Forall.KEYWORD, Exists.KEYWORD, "TRUE", "FALSE");
    private static final List<String> SYMBOLS = List.of("->", "&", "|", "-", "(", ")", ",", ".", "=");
    static final int MAX_DEPTH = 500;

    /**
     * @param text a formula
     * @return the formula {@code text} denotes
     * @throws IllegalArgumentException if {@code text} is not a formula
     */
    public FirstOrderOperation parse(String text) {
        Cursor cursor = new Cursor(tokenize(text));
        FirstOrderOperation formula = cursor.formula();
        cursor.expectEnd();
        return formula;
    }

    /**
     * @param text a term
     * @return the term {@code text} denotes
     * @throws IllegalArgumentException if {@code text} is not a term
     */
    public Term parseTerm(String text) {
        Cursor cursor = new Cursor(tokenize(text));
        Term term = cursor.term();
        cursor.expectEnd();
        return term;
    }

    private static List<String> tokenize(String text) {
        if (text == null) {
            throw new IllegalArgumentException("No formula given");
        }
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if (Character.isLetter(c)) {
                int start = i;
                while (i < text.length() && isIdentifierPart(text.charAt(i))) {
                    i++;
                }
                tokens.add(text.substring(start, i));
            } else {
                String symbol = symbolAt(text, i);
                tokens.add(symbol);
                i += symbol.length();
            }
        }
        return tokens;
    }

    private static boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static String symbolAt(String text, int index) {
        for (String symbol : SYMBOLS) {
            if (text.startsWith(symbol, index)) {
                return symbol;
            }
        }
        throw new IllegalArgumentException("Unexpected character '" + text.charAt(index) + "' at position " + index);
    }

    private static boolean isIdentifier(String token) {
        return token != null && Character.isLetter(token.charAt(0)) && !RESERVED.contains(token);
    }

    private static final class Cursor {

        private final List<String> tokens;
        private int position;
        private int depth;

        private Cursor(List<String> tokens) {
            this.tokens = tokens;
        }

        private static boolean isTermName(String token) {
            return isIdentifier(token) && Character.isLowerCase(token.charAt(0));
        }

        private String peek() {
            return position < tokens.size() ? tokens.get(position) : null;
        }

        private boolean accept(String token) {
            if (token.equals(peek())) {
                position++;
                return true;
            }
            return false;
        }

        private void expect(String token) {
            if (!accept(token)) {
                throw error("'" + token + "'");
            }
        }

        private void expectEnd() {
            if (peek() != null) {
                throw error("the end of the input");
            }
        }

        private IllegalArgumentException error(String expected) {
            String found = peek() == null ? "the end of the input" : "'" + peek() + "'";
            return new IllegalArgumentException("Expected " + expected + " but found " + found);
        }

        private FirstOrderOperation formula() {
            FirstOrderOperation left = disjunction();
            while (accept("&")) {
                left = new ConjunctionFirstOrder(left, disjunction());
            }
            return left;
        }

        private FirstOrderOperation disjunction() {
            FirstOrderOperation left = implication();
            while (accept("|")) {
                left = new DisjunctionFirstOrder(left, implication());
            }
            return left;
        }

        private FirstOrderOperation implication() {
            FirstOrderOperation left = unary();
            while (accept("->")) {
                left = new ImplicationFirstOrder(left, unary());
            }
            return left;
        }

        private void enter() {
            if (++depth > MAX_DEPTH) {
                throw new IllegalArgumentException("The input is nested more than " + MAX_DEPTH + " levels deep");
            }
        }

        private FirstOrderOperation unary() {
            enter();
            try {
                return unaryBody();
            } finally {
                depth--;
            }
        }

        private FirstOrderOperation unaryBody() {
            if (accept("-")) {
                return new NegationFirstOrder(unary());
            } else if (accept(Forall.KEYWORD)) {
                String variable = boundVariable();
                return new Forall(variable, formula());
            } else if (accept(Exists.KEYWORD)) {
                String variable = boundVariable();
                return new Exists(variable, formula());
            }
            return primary();
        }

        private String boundVariable() {
            String variable = peek();
            if (!isTermName(variable)) {
                throw error("a variable");
            }
            position++;
            expect(".");
            return variable;
        }

        private FirstOrderOperation primary() {
            int start = position;
            Term left = tryTerm();
            if (left != null && accept("=")) {
                return new Equals(left, term());
            }
            position = start;
            if (accept("(")) {
                FirstOrderOperation formula = formula();
                expect(")");
                return formula;
            } else if (accept("TRUE")) {
                return ConstantFirstOrder.TRUE;
            } else if (accept("FALSE")) {
                return ConstantFirstOrder.FALSE;
            }
            String name = peek();
            if (!isIdentifier(name)) {
                throw error("a formula");
            }
            position++;
            return new Predicate(name, accept("(") ? arguments() : List.of());
        }

        /**
         * @return the term that starts here, or {@code null} (and the position unchanged) if none does
         */
        private Term tryTerm() {
            int start = position;
            try {
                return term();
            } catch (IllegalArgumentException e) {
                position = start;
                return null;
            }
        }

        private Term term() {
            enter();
            try {
                return termBody();
            } finally {
                depth--;
            }
        }

        private Term termBody() {
            if (accept("(")) {
                Term term = term();
                expect(")");
                return term;
            }
            String name = peek();
            if (!isTermName(name)) {
                throw error("a term");
            }
            position++;
            if (accept("(")) {
                return new FunctionApplication(name, arguments());
            }
            return new VariableTerm(name);
        }

        /**
         * Reads the arguments after an opening parenthesis, and the closing one
         */
        private List<Term> arguments() {
            List<Term> arguments = new ArrayList<>();
            arguments.add(term());
            while (accept(",")) {
                arguments.add(term());
            }
            expect(")");
            return arguments;
        }
    }
}
