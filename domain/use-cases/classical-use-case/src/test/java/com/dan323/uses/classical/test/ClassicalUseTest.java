package com.dan323.uses.classical.test;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.ParamKind;
import com.dan323.uses.LogicalGetActions;
import com.dan323.uses.LogicalSolver;
import com.dan323.uses.classical.ClassicalConfiguration;
import com.dan323.uses.classical.ClassicalProofTransformer;
import com.dan323.uses.classical.mock.ClassicalProof;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ClassicalUseTest {

    @Test
    public void classicSolver() {
        var solver = new LogicalSolver<>(new ClassicalProofTransformer(), Duration.ofSeconds(30));
        var proof = solver.perform(new ClassicalProofTransformer().fromProof(ClassicalProof.naturalDeductionNoAssms()));
        assertTrue(proof.isDone());
        proof = solver.perform(new ClassicalProofTransformer().fromProof(ClassicalProof.naturalDeductionWithAssms()));
        assertTrue(proof.isDone());
        proof = solver.perform(new ClassicalProofTransformer().fromProof(ClassicalProof.naturalDeductionNotProvable()));
        assertFalse(proof.isDone());
    }

    @Test
    public void classicActions() {
        LogicalGetActions actions = (new ClassicalConfiguration()).classicalActions();
        assertEquals(14, actions.perform().size());
    }

    @Test
    void thereIsOneDescriptorPerAvailableAction() {
        var actions = new ClassicalConfiguration().classicalActions().perform();

        assertEquals(Arrays.stream(AvailableAction.values()).map(Enum::name).toList(),
                actions.stream().map(ActionDescriptorDto::name).toList());
    }

    @Test
    void descriptorsHaveTheParametersOfTheActions() {
        var byName = new ClassicalConfiguration().classicalActions().perform().stream()
                .collect(Collectors.toMap(ActionDescriptorDto::name, ActionDescriptorDto::params));

        assertEquals(List.of(ParamKind.EXPRESSION), byName.get("ASSUME"));
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), byName.get("ORI1"));
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), byName.get("ORI2"));
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), byName.get("FE"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT, ParamKind.INT), byName.get("ORE"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("ANDI"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("MP"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("FI"));
        assertEquals(List.of(ParamKind.INT), byName.get("ANDE1"));
        assertEquals(List.of(ParamKind.INT), byName.get("ANDE2"));
        assertEquals(List.of(ParamKind.INT), byName.get("COPY"));
        assertEquals(List.of(ParamKind.INT), byName.get("NOTE"));
        assertEquals(List.of(), byName.get("NOTI"));
        assertEquals(List.of(), byName.get("DT"));
    }

    @Test
    void everyDescriptorBuildsAnActionFromItsParameters() {
        var transformer = new ClassicalProofTransformer();
        for (var descriptor : new ClassicalConfiguration().classicalActions().perform()) {
            var lines = Collections.nCopies((int) descriptor.params().stream().filter(ParamKind.INT::equals).count(), 1);
            var extra = descriptor.params().contains(ParamKind.EXPRESSION) ? Map.of("expression", "P") : Map.<String, String>of();

            assertNotNull(transformer.from(new ActionDto(descriptor.name(), lines, extra)), descriptor.name());
        }
    }

    @Test
    void theActionListIsBuiltOnce() {
        var actions = new ClassicalConfiguration().classicalActions();

        assertSame(actions.perform(), actions.perform());
    }
}
