package com.dan323.uses.modal.test;

import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ParamKind;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.modal.AvailableModalAction;
import com.dan323.uses.modal.ModalConfiguration;
import com.dan323.uses.modal.ModalProofTransformer;
import com.dan323.uses.modal.mock.ModalProof;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModalUseTest {

    @Test
    public void modalSolver() {
        var solver = new LogicalSolver<>(new ModalProofTransformer(), Duration.ofSeconds(30));
        var e = solver.perform(new ModalProofTransformer().fromProof(ModalProof.naturalDeductionNoAssms()));
        assertTrue(e.isDone());
    }

    @Test
    public void modalActions() {
        LogicalGetActions actions = (new ModalConfiguration()).modalActions();
        assertEquals(20, actions.perform().size());
    }

    @Test
    void actionNamesAreUnique() {
        var names = new ModalConfiguration().modalActions().perform().stream().map(ActionDescriptorDto::name).toList();

        assertEquals(names.size(), new HashSet<>(names).size());
    }

    @Test
    void descriptorsHaveTheParametersOfTheActions() {
        var byName = new ModalConfiguration().modalActions().perform().stream()
                .collect(Collectors.toMap(ActionDescriptorDto::name, ActionDescriptorDto::params));

        assertEquals(List.of(ParamKind.EXPRESSION, ParamKind.STATE), byName.get("Ass"));
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION, ParamKind.STATE), byName.get("FE"));
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), byName.get("|I1"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT, ParamKind.INT), byName.get("|E"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("[]E"));
        assertEquals(List.of(ParamKind.INT), byName.get("Refl"));
        assertEquals(List.of(), byName.get("[]I"));
        assertEquals(List.of(), byName.get("->I"));
    }

    @Test
    void everyDescriptorNameBuildsAnAction() {
        var transformer = new ModalProofTransformer();
        for (var action : AvailableModalAction.values()) {
            var descriptor = action.descriptor();
            var lines = Collections.nCopies((int) descriptor.params().stream().filter(ParamKind.INT::equals).count(), 1);
            var extra = Map.of("expression", "P", "state", "s0");

            assertNotNull(transformer.from(new ActionDto(descriptor.name(), lines, extra)), descriptor.name());
        }
    }

    @Test
    void theActionListIsBuiltOnce() {
        var actions = new ModalConfiguration().modalActions();

        assertSame(actions.perform(), actions.perform());
    }
}
