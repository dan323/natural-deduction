package com.dan323.classical.proof;

import com.dan323.classical.*;

public enum AvailableAction {
    ASSUME(ClassicAssume.class), ORI1(ClassicOrI1.class), ORI2(ClassicOrI2.class),
    ORE(ClassicOrE.class), ANDI(ClassicAndI.class), ANDE1(ClassicAndE1.class),
    ANDE2(ClassicAndE2.class), COPY(ClassicCopy.class), NOTE(ClassicNotE.class),
    NOTI(ClassicNotI.class), DT(ClassicDeductionTheorem.class), MP(ClassicModusPonens.class),
    FE(ClassicFE.class), FI(ClassicFI.class);

    final String actionName;

    AvailableAction(Class<? extends ClassicalAction> actionName){
        this.actionName = actionName.getSimpleName();
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
