package com.dan323.uses.modal;

import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ParamKind;

import static com.dan323.model.ParamKind.EXPRESSION;
import static com.dan323.model.ParamKind.INT;
import static com.dan323.model.ParamKind.STATE;

/**
 * The actions of modal logic: the rule names understood by {@code ParseModalAction.parseAction} (what a client sends
 * as {@code ActionDto.name}) with the inputs each one takes. {@code ParseModalAction} is the source of truth for the
 * names, and a test checks that every entry here builds an action.
 */
public enum AvailableModalAction {
    ASSUME("Ass", EXPRESSION, STATE),
    OR_I1("|I1", INT, EXPRESSION),
    OR_I2("|I2", INT, EXPRESSION),
    OR_E("|E", INT, INT, INT),
    AND_I("&I", INT, INT),
    AND_E1("&E1", INT),
    AND_E2("&E2", INT),
    COPY("Rep", INT),
    NOT_E("-E", INT),
    NOT_I("-I"),
    DEDUCTION_THEOREM("->I"),
    MODUS_PONENS("->E", INT, INT),
    FALSE_E("FE", INT, EXPRESSION, STATE),
    FALSE_I("FI", INT, INT),
    BOX_I("[]I"),
    BOX_E("[]E", INT, INT),
    DIA_I("<>I", INT, INT),
    DIA_E("<>E", INT),
    REFLEXIVE("Refl", INT),
    TRANSITIVE("Trans", INT, INT);

    private final ActionDescriptorDto descriptor;

    AvailableModalAction(String ruleName, ParamKind... params) {
        this.descriptor = ActionDescriptorDto.of(ruleName, params);
    }

    public ActionDescriptorDto descriptor() {
        return descriptor;
    }
}
