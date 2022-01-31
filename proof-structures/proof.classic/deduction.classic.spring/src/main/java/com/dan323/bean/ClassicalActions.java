package com.dan323.bean;

import com.dan323.classical.*;
import com.dan323.expressions.classical.ClassicalLogicOperation;
import com.dan323.proof.generic.bean.Actions;
import com.dan323.proof.generic.bean.Construct;
import com.dan323.proof.generic.bean.ExpressionParser;
import com.dan323.proof.generic.bean.Input;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ClassicalActions implements Actions<Input<Void>, ClassicalLogicOperation, ClassicalAction> {

    @Override
    public List<String> get() {
        return Arrays.stream(Constructor.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    public Construct<Input<Void>, ClassicalLogicOperation, ClassicalAction> getAction(String name) {
        return Constructor.valueOf(name);
    }

    @Override
    public String ofLogic() {
        return "classical";
    }

    public enum Constructor implements Construct<Input<Void>, ClassicalLogicOperation, ClassicalAction> {
        AND_E1((input, par) -> new ClassicAndE1(input.ints().get(0)), 1, false, "&E1"),
        AND_E2((input, par) -> new ClassicAndE2(input.ints().get(0)), 1, false, "&E2"),
        AND_I((input, par) -> new ClassicAndI(input.ints().get(0), input.ints().get(1)), 2, false, "&I"),
        ASSUME((input, par) -> new ClassicAssume(par.apply(input.expression())), 0, true, "ASS"),
        REP((input, par) -> new ClassicCopy(input.ints().get(0)), 1, false, "REP"),
        IMP_I((input, par) -> new ClassicDeductionTheorem(), 0, false, "->I"),
        FALSE_E((input, par) -> new ClassicFE(input.ints().get(0), par.apply(input.expression())), 1, true, "\\bot\\E"),
        FALSE_I((input, par) -> new ClassicFI(input.ints().get(0), input.ints().get(1)), 2, false, "\\bot\\I"),
        IMP_E((input, par) -> new ClassicModusPonens(input.ints().get(0), input.ints().get(1)), 2, false, "->E"),
        NOT_E((input, par) -> new ClassicNotE(input.ints().get(0)), 1, false, "-E"),
        NOT_I((input, par) -> new ClassicNotI(), 0, false, "-I"),
        OR_E((input, par) -> new ClassicOrE(input.ints().get(0), input.ints().get(1), input.ints().get(2)), 3, false,"|E"),
        OR_I1((input, par) -> new ClassicOrI1(input.ints().get(0), par.apply(input.expression())), 1, true,"|I1"),
        OR_I2((input, par) -> new ClassicOrI2(input.ints().get(0), par.apply(input.expression())), 1, true, "|I2");

        private final BiFunction<Input<Void>, ExpressionParser<ClassicalLogicOperation>, ClassicalAction> cons;
        private final int inputs;
        private final boolean needsExpr;
        private final String name;

        Constructor(BiFunction<Input<Void>, ExpressionParser<ClassicalLogicOperation>, ClassicalAction> cons, int k, boolean expression, String name) {
            this.cons = cons;
            this.inputs = k;
            this.needsExpr = expression;
            this.name = name;
        }

        public String getName(){
            return name;
        }

        public BiFunction<Input<Void>, ExpressionParser<ClassicalLogicOperation>, ClassicalAction> getCons() {
            return cons;
        }

        public boolean isNeedsExpr() {
            return needsExpr;
        }

        @Override
        public boolean isNeedsExtra() {
            return false;
        }

        public int getInputs() {
            return inputs;
        }
    }
}