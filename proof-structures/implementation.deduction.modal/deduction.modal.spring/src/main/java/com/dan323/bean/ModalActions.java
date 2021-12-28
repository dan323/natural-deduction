package com.dan323.bean;

import com.dan323.proof.generic.bean.Actions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ModalActions implements Actions {

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

    private enum Actions {
        AND_E1,
        AND_E2,
        AND_I,
        ASSUME,
        BOX_E,
        DIA_E,
        BOX_I,
        DIA_I,
        COPY,
        IMP_I,
        FALSE_E,
        FALSE_I,
        IMP_E,
        NOT_E,
        NOT_I,
        OR_E,
        OR_I1,
        OR_I2
    }
}
