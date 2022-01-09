package com.dan323.bean;

import com.dan323.expressions.modal.ModalLogicalOperation;
import com.dan323.expressions.modal.ModalOperation;
import com.dan323.expressions.relation.RelationOperation;
import com.dan323.proof.generic.bean.Actions;
import com.dan323.proof.generic.bean.Construct;
import com.dan323.proof.generic.bean.ExpressionParser;
import com.dan323.proof.generic.bean.Input;
import com.dan323.proof.modal.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ModalActions<T> implements Actions<Input<T>, ModalOperation, ModalAction<T>> {

    @Override
    public List<String> get() {
        return Arrays.stream(Actions.values())
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    @Override
    public String ofLogic() {
        return "modal";
    }

    @Override
    public Construct<Input<T>, ModalOperation, ModalAction<T>> getAction(String name) {
        return (Construct) Actions.valueOf(name);
    }

    public enum Actions implements Construct<Input<?>, ModalOperation, ModalAction<?>> {
        AND_E1((input, par) -> new ModalAndE1<>(input.ints().get(0)), 1, false, false),
        AND_E2((input, par) -> new ModalAndE2<>(input.ints().get(0)), 1, false, false),
        AND_I((input, par) -> new ModalAndI<>(input.ints().get(0), input.ints().get(1)), 2, false, false),
        ASSUME((input, par) -> {
            if (input.extraInformation() != null) {
                return new ModalAssume<>((ModalLogicalOperation) par.apply(input.expression()), input.extraInformation());
            } else {
                return new ModalAssume<>((RelationOperation<?>) par.apply(input.expression()));
            }
        }, 0, true, true),
        BOX_E((input, par) -> new ModalBoxE<>(input.ints().get(0), input.ints().get(1)), 2, false, false),
        DIA_E((input, par) -> new ModalDiaE<>(input.ints().get(0)), 1, false, false),
        BOX_I((input, par) -> new ModalBoxI<>(), 0, false, false),
        DIA_I((input, par) -> new ModalDiaI<>(input.ints().get(0), input.ints().get(1)), 2, false, false),
        REP((input, par) -> new ModalCopy<>(input.ints().get(0)), 1, false, false),
        IMP_I((input, par) -> new ModalDeductionTheorem<>(), 0, false, false),
        FALSE_E((input, par) -> new ModalFE<>(input.ints().get(0), (ModalLogicalOperation) par.apply(input.expression()), input.extraInformation()), 1, true, true),
        FALSE_I((input, par) -> new ModalFI<>(input.extraInformation(), input.ints().get(0), input.ints().get(1)), 2, false, true),
        IMP_E((input, par) -> new ModalModusPonens<>(input.ints().get(0), input.ints().get(1)), 2, false, false),
        NOT_E((input, par) -> new ModalNotE<>(input.ints().get(0)), 1, false, false),
        NOT_I((input, par) -> new ModalNotI<>(), 0, false, false),
        OR_E((input, par) -> new ModalOrE<>(input.ints().get(0), input.ints().get(1), input.ints().get(2)), 3, false, false),
        OR_I1((input, par) -> new ModalOrI1<>(input.ints().get(0), (ModalLogicalOperation) par.apply(input.expression())), 1, true, false),
        OR_I2((input, par) -> new ModalOrI2<>(input.ints().get(0), (ModalLogicalOperation) par.apply(input.expression())), 1, true, false);

        private final int inputs;
        private final boolean needsExpr;
        private final boolean needsExtra;
        private final BiFunction<Input<?>, ExpressionParser<ModalOperation>, ModalAction<?>> cons;

        Actions(BiFunction<Input<?>, ExpressionParser<ModalOperation>, ModalAction<?>> cons, int inputs, boolean needsExpr, boolean needsExtra) {
            this.cons = cons;
            this.needsExpr = needsExpr;
            this.inputs = inputs;
            this.needsExtra = needsExtra;
        }

        @Override
        public int getInputs() {
            return inputs;
        }

        @Override
        public boolean isNeedsExpr() {
            return needsExpr;
        }

        @Override
        public BiFunction<Input<?>, ExpressionParser<ModalOperation>, ModalAction<?>> getCons() {
            return cons;
        }

        public boolean isNeedsExtra() {
            return needsExtra;
        }
    }
}
