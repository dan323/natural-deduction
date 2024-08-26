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
}
