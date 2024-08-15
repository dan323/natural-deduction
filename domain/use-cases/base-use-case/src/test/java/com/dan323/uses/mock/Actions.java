package com.dan323.uses.mock;

import com.dan323.model.ActionDto;

import java.util.List;
import java.util.Map;

public final class Actions {

    public static ActionDto actionAddOneStep(){
        return new ActionDto("Action1", List.of(), Map.of());
    }

    public static ActionDto invalid(){
        return new ActionDto("Action2", List.of(1,2), Map.of());
    }
}
