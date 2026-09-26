package com.dan323.classical.proof;

import com.dan323.classical.*;

public enum AvailableAction {
    ASSUME(ClassicAssume.class, "Ass"), ORI1(ClassicOrI1.class, "|I1"), ORI2(ClassicOrI2.class, "|I2"),
    ORE(ClassicOrE.class, "|E"), ANDI(ClassicAndI.class, "&I"), ANDE1(ClassicAndE1.class, "&E1"),
    ANDE2(ClassicAndE2.class, "&E2"), COPY(ClassicCopy.class, "Rep"), NOTE(ClassicNotE.class, "-E"),
    NOTI(ClassicNotI.class, "-I"), DT(ClassicDeductionTheorem.class, "->I"), MP(ClassicModusPonens.class, "->E"),
    FE(ClassicFE.class, "FE"), FI(ClassicFI.class, "FI");

    final String actionName;
    private final String ruleName;

    AvailableAction(Class<? extends ClassicalAction> actionName, String ruleName){
        this.actionName = actionName.getSimpleName();
        this.ruleName = ruleName;
    }

    /**
     * The name of the rule as modal logic knows it ({@code ParseModalAction.parseAction}), e.g. {@code Rep} for
     * {@link #COPY}, so that a client can send the same name to every logic.
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * The action called {@code name}, either its constant name ({@code COPY}) or its rule name ({@code Rep}).
     *
     * @throws IllegalArgumentException when no action has that name
     */
    public static AvailableAction fromName(String name) {
        for (AvailableAction action : values()) {
            if (action.name().equals(name) || action.ruleName.equals(name)) {
                return action;
            }
        }
        throw new IllegalArgumentException("The rule " + name + " is not valid.");
    }

    public String getActionName(){
        return actionName;
    }

    /**
     * Whether the rule is also a rule of intuitionistic logic, which is classical logic without double negation
     * elimination ({@link #NOTE}); ex falso ({@link #FE}) stays.
     */
    public boolean isIntuitionistic() {
        // No default branch: adding an action does not compile until it is decided here.
        return switch (this) {
            case NOTE -> false;
            case ASSUME, ORI1, ORI2, ORE, ANDI, ANDE1, ANDE2, COPY, NOTI, DT, MP, FE, FI -> true;
        };
    }
}
