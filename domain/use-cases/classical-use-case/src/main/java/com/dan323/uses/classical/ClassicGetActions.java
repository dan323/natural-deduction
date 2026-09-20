package com.dan323.uses.classical;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ParamKind;
import com.dan323.uses.LogicalGetActions;

import java.util.Arrays;
import java.util.List;

import static com.dan323.model.ParamKind.EXPRESSION;
import static com.dan323.model.ParamKind.INT;

/**
 * The actions of classical logic: one per {@link AvailableAction}, built once. The parameters mirror the constructor of
 * the action, see {@code ParseClassicalAction.parseAction}.
 */
public class ClassicGetActions implements LogicalGetActions {

    private final List<ActionDescriptorDto> actions = Arrays.stream(AvailableAction.values())
            .map(action -> new ActionDescriptorDto(action.name(), paramsOf(action)))
            .toList();

    @Override
    public List<ActionDescriptorDto> perform() {
        return actions;
    }

    @Override
    public String getLogicName() {
        return "classical";
    }

    // No default branch: adding an AvailableAction does not compile until it is described here.
    private static List<ParamKind> paramsOf(AvailableAction action) {
        return switch (action) {
            case ASSUME -> List.of(EXPRESSION);
            case ORI1, ORI2, FE -> List.of(INT, EXPRESSION);
            case ORE -> List.of(INT, INT, INT);
            case ANDI, MP, FI -> List.of(INT, INT);
            case ANDE1, ANDE2, COPY, NOTE -> List.of(INT);
            case NOTI, DT -> List.of();
        };
    }
}
