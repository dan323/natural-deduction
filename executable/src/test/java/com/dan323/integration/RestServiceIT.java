package com.dan323.integration;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.ImplicationClassic;
import com.dan323.expressions.classical.VariableClassic;
import com.dan323.main.Application;
import com.dan323.main.ApplicationConfiguration;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ActionDto;
import com.dan323.model.Difficulty;
import com.dan323.model.ExerciseDto;
import com.dan323.model.ParamKind;
import com.dan323.model.ProofDto;
import com.dan323.model.StepDto;
import com.dan323.rest.model.ErrorResponse;
import com.dan323.rest.model.ProofActionRequest;
import com.dan323.rest.model.ProofResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, ApplicationConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestServiceIT {
    @LocalServerPort
    private int port;

    TestRestTemplate restTemplate = new TestRestTemplate();

    HttpHeaders headers = new HttpHeaders();

    @Test
    public void getActions() {
        var expectedNames = Arrays.stream(AvailableAction.values()).map(AvailableAction::name).collect(Collectors.toSet());
        var response = restTemplate
                .exchange(createURLWithPort("/logic/classical/actions"),
                        HttpMethod.GET,
                        new HttpEntity<>(null, headers),
                        ActionDescriptorDto[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var actions = Objects.requireNonNull(response.getBody());
        assertEquals(expectedNames.size(), actions.length);
        assertEquals(expectedNames, Arrays.stream(actions).map(ActionDescriptorDto::name).collect(Collectors.toSet()));
        var byName = Arrays.stream(actions).collect(Collectors.toMap(ActionDescriptorDto::name, ActionDescriptorDto::params));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("ANDI"));
        assertEquals(List.of(ParamKind.EXPRESSION), byName.get("ASSUME"));
        assertEquals(List.of(ParamKind.INT, ParamKind.EXPRESSION), byName.get("FE"));
        assertEquals(List.of(), byName.get("DT"));
    }

    @Test
    public void getActionsIsJsonWithParamKindNames() {
        var response = restTemplate.getForEntity(createURLWithPort("/logic/classical/actions"), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("{\"name\":\"ORI1\",\"params\":[\"INT\",\"EXPRESSION\"],"));
    }

    @Test
    public void classicalActionsCarryTheirPresentationFields() {
        var response = restTemplate.getForEntity(createURLWithPort("/logic/classical/actions"), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("{\"name\":\"MP\",\"params\":[\"INT\",\"INT\"],\"label\":\"Modus ponens\","
                + "\"symbol\":\"→E\",\"category\":\"ELIMINATION\",\"description\":\"From A → B and A, derive B\","
                + "\"paramLabels\":[\"Implication (A → B)\",\"Antecedent (A)\"]}"), response.getBody());

        var actions = Objects.requireNonNull(restTemplate.getForObject(createURLWithPort("/logic/classical/actions"), ActionDescriptorDto[].class));
        for (var action : actions) {
            assertFalse(action.label().isBlank(), action.name());
            assertFalse(action.description().isBlank(), action.name());
            assertNotNull(action.category(), action.name());
            assertEquals(action.params().size(), action.paramLabels().size(), action.name());
        }
    }

    @Test
    public void getModalActions() {
        var response = restTemplate.getForEntity(createURLWithPort("/logic/modal/actions"), ActionDescriptorDto[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var byName = Arrays.stream(Objects.requireNonNull(response.getBody()))
                .collect(Collectors.toMap(ActionDescriptorDto::name, ActionDescriptorDto::params));
        assertEquals(20, byName.size());
        assertEquals(List.of(ParamKind.EXPRESSION, ParamKind.STATE), byName.get("Ass"));
        assertEquals(List.of(ParamKind.INT, ParamKind.INT), byName.get("[]E"));
        for (var action : response.getBody()) {
            assertFalse(action.label().isBlank(), action.name());
            assertFalse(action.symbol().isBlank(), action.name());
            assertFalse(action.description().isBlank(), action.name());
            assertNotNull(action.category(), action.name());
            assertEquals(action.params().size(), action.paramLabels().size(), action.name());
        }
        var boxE = Arrays.stream(response.getBody()).filter(action -> action.name().equals("[]E")).findFirst().orElseThrow();
        assertEquals("Box elimination", boxE.label());
        assertEquals("□E", boxE.symbol());
        assertEquals(List.of("Necessity (□A in state s)", "Relation (s <= t)"), boxE.paramLabels());
    }

    @Test
    public void modalActionNamesCanBeApplied() {
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of("state", "s0"))), "modal", "P");
        var response = restTemplate.exchange(createURLWithPort("/logic/modal/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("Rep", List.of(1), Map.of()), proof), headers), ProofResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).proof().steps().size());
    }

    @Test
    public void everyLogicAcceptsBothNamesOfASharedRule() {
        var classical = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of())), "classical", "P");
        var modalStep = new StepDto("P", "Ass", 0, Map.of("state", "s0"));
        for (var name : List.of("COPY", "Rep")) {
            for (var logic : List.of("classical", "intuitionistic")) {
                assertCopies(logic, name, new ProofDto(classical.steps(), logic, "P"));
            }
            for (var logic : List.of("modal", "modal-next-until")) {
                assertCopies(logic, name, new ProofDto(List.of(modalStep), logic, "P"));
            }
        }
    }

    private void assertCopies(String logic, String name, ProofDto proof) {
        var response = restTemplate.exchange(createURLWithPort("/logic/" + logic + "/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto(name, List.of(1), Map.of()), proof), headers), ProofResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), logic + " " + name);
        assertEquals("Rep [1]", last(Objects.requireNonNull(response.getBody()).proof()).rule(), logic + " " + name);
    }

    @Test
    public void intuitionisticRejectsDoubleNegationEliminationUnderBothNames() {
        var proof = new ProofDto(List.of(new StepDto("- (- P)", "Ass", 0, Map.of())), "intuitionistic", "P");
        for (var name : List.of("NOTE", "-E")) {
            var response = restTemplate.exchange(createURLWithPort("/logic/intuitionistic/action"), HttpMethod.POST,
                    new HttpEntity<>(new ProofActionRequest(new ActionDto(name, List.of(1), Map.of()), proof), headers), ErrorResponse.class);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals("Rule " + name + " is not a rule of intuitionistic logic", Objects.requireNonNull(response.getBody()).message());
        }
    }

    private static final String NEXT_UNTIL = "modal-next-until";

    private static StepDto last(ProofDto proof) {
        return proof.steps().get(proof.steps().size() - 1);
    }

    private ResponseEntity<ProofResponse> applyNextUntil(ActionDto action, ProofDto proof) {
        return restTemplate.exchange(createURLWithPort("/logic/" + NEXT_UNTIL + "/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(action, proof), headers), ProofResponse.class);
    }

    @Test
    public void modalNextUntilActionsAreTheModalOnesThenNextAndUntil() {
        var modal = Objects.requireNonNull(restTemplate.getForObject(createURLWithPort("/logic/modal/actions"), ActionDescriptorDto[].class));
        var response = restTemplate.getForEntity(createURLWithPort("/logic/" + NEXT_UNTIL + "/actions"), ActionDescriptorDto[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var actions = Arrays.asList(Objects.requireNonNull(response.getBody()));
        assertEquals(Arrays.asList(modal), actions.subList(0, modal.length));
        assertEquals(List.of("XI", "XE", "Succ", "UI1", "UI2", "UE", "U<>", "Ind"),
                actions.subList(modal.length, actions.size()).stream().map(ActionDescriptorDto::name).toList());
        for (var action : actions) {
            assertFalse(action.label().isBlank(), action.name());
            assertFalse(action.symbol().isBlank(), action.name());
            assertFalse(action.description().isBlank(), action.name());
            assertEquals(action.params().size(), action.paramLabels().size(), action.name());
        }
    }

    @Test
    public void modalNextUntilProvesWithNextAndUntil() {
        var proof = new ProofDto(List.of(new StepDto("p", "Ass", 0, Map.of("state", "s0")),
                new StepDto("X q", "Ass", 0, Map.of("state", "s0"))), NEXT_UNTIL, "p U q");

        var next = Objects.requireNonNull(applyNextUntil(new ActionDto("XE", List.of(2), Map.of()), proof).getBody());
        assertTrue(next.success());
        assertEquals(new StepDto("q", "XE [2]", 0, Map.of("state", "s0+1")), last(next.proof()));

        var untilNow = Objects.requireNonNull(applyNextUntil(new ActionDto("UI1", List.of(3), Map.of("expression", "p")), next.proof()).getBody());
        var back = Objects.requireNonNull(applyNextUntil(new ActionDto("XI", List.of(4), Map.of()), untilNow.proof()).getBody());
        assertEquals(new StepDto("X (p U q)", "XI [4]", 0, Map.of("state", "s0")), last(back.proof()));
        assertFalse(back.done());

        var response = applyNextUntil(new ActionDto("UI2", List.of(1, 5), Map.of()), back.proof());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var body = Objects.requireNonNull(response.getBody());
        assertTrue(body.done());
        assertEquals(NEXT_UNTIL, body.proof().logic());
        assertEquals(new StepDto("p U q", "UI [1, 5]", 0, Map.of("state", "s0")), last(body.proof()));
    }

    @Test
    public void modalNextUntilRejectsARuleInTheWrongState() {
        var proof = new ProofDto(List.of(new StepDto("p", "Ass", 0, Map.of("state", "s0"))), NEXT_UNTIL, "X p");

        var response = applyNextUntil(new ActionDto("XI", List.of(1), Map.of()), proof);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).success());

        var badState = restTemplate.exchange(createURLWithPort("/logic/" + NEXT_UNTIL + "/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("Ass", List.of(), Map.of("expression", "q", "state", "s0-1")), proof), headers),
                ErrorResponse.class);
        assertError(HttpStatus.BAD_REQUEST, badState);
    }

    @Test
    public void modalNextUntilHasNoSolver() {
        var response = restTemplate.exchange(createURLWithPort("/logic/" + NEXT_UNTIL + "/solve"), HttpMethod.POST,
                new HttpEntity<>(new ProofDto(List.of(), NEXT_UNTIL, "p -> p"), headers), ErrorResponse.class);
        assertError(HttpStatus.BAD_REQUEST, response);
        assertEquals("There is no solver for the logic 'modal-next-until'", response.getBody().message());
    }

    @Test
    public void modalNextUntilLoadsAProofText() {
        var gap = " ".repeat(11);
        var text = "s0: [] p" + gap + "Ass\n"
                + "s0 <= s0+1" + gap + "Succ [1]\n"
                + "s0+1: p" + gap + "[]E [1, 2]\n"
                + "s0: X p" + gap + "XI [3]\n";
        var upload = new HttpHeaders();
        upload.setContentType(MediaType.MULTIPART_FORM_DATA);
        var part = new HttpHeaders();
        part.setContentType(MediaType.TEXT_PLAIN);
        var resource = new ByteArrayResource(text.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "next.pf";
            }
        };
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new HttpEntity<>(resource, part));

        var response = restTemplate.exchange(createURLWithPort("/logic/" + NEXT_UNTIL + "/proof"), HttpMethod.POST,
                new HttpEntity<>(map, upload), ProofDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        var proof = Objects.requireNonNull(response.getBody());
        assertEquals(NEXT_UNTIL, proof.logic());
        assertEquals("X p", proof.goal());
        assertEquals(new StepDto("s0 <= s0+1", "Succ [1]", 0, Map.of()), proof.steps().get(1));
        assertEquals(new StepDto("p", "[]E [1, 2]", 0, Map.of("state", "s0+1")), proof.steps().get(2));
        assertError(HttpStatus.BAD_REQUEST, postUpload("modal", text));
        // p in s0+1 does not prove the goal p, which is in s0: no 201 whose done would say otherwise.
        var notInS0 = postUpload(NEXT_UNTIL, "s0: X p" + gap + "Ass\n" + "s0+1: p" + gap + "XE [1]\n");
        assertError(HttpStatus.BAD_REQUEST, notInS0);
        assertEquals("The proof is invalid: it proves its goal in s0+1, not in the initial state s0", notInS0.getBody().message());
    }

    @Test
    public void modalNextUntilExercisesAreListedWithoutSolutions() {
        var response = restTemplate.getForEntity(createURLWithPort("/logic/" + NEXT_UNTIL + "/exercises"), ExerciseDto[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var exercises = Objects.requireNonNull(response.getBody());
        assertTrue(exercises.length >= 8);
        assertEquals(new ExerciseDto("next-in-and-out", "In and out of the next state", List.of("X p"), "X (p | q)", Difficulty.EASY), exercises[0]);
        var raw = restTemplate.getForObject(createURLWithPort("/logic/" + NEXT_UNTIL + "/exercises"), String.class);
        assertFalse(raw.contains("XE ["), raw);
    }

    @Test
    public void modalDoesNotHaveTheNextAndUntilRules() {
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of("state", "s0"))), "modal", "P");
        var response = restTemplate.exchange(createURLWithPort("/logic/modal/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("Succ", List.of(1), Map.of()), proof), headers), ErrorResponse.class);
        assertError(HttpStatus.BAD_REQUEST, response);
    }

    private ResponseEntity<ProofDto> solve(String logic, ProofDto proof) {
        return restTemplate.exchange(createURLWithPort("/logic/" + logic + "/solve"), HttpMethod.POST,
                new HttpEntity<>(proof, headers), ProofDto.class);
    }

    @Test
    public void solveClassicalProof() {
        var response = solve("classical", new ProofDto(List.of(), "classical", "P -> P"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var solved = Objects.requireNonNull(response.getBody());
        assertTrue(solved.isDone());
        assertEquals("P -> P", solved.goal());
        assertFalse(solved.steps().isEmpty());
    }

    @Test
    public void solveModalProof() {
        var response = solve("modal", new ProofDto(List.of(new StepDto("[]P", "Ass", 0, Map.of("state", "s0"))), "modal", "<>P"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).isDone());
    }

    @Test
    public void solveKeepsTheAssumptionsOfAnUnprovableProof() {
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of())), "classical", "Q");
        var response = solve("classical", proof);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).isDone());
        assertEquals("P", response.getBody().steps().get(0).expression());
    }

    @Test
    public void solveErrorsUseTheErrorBody() {
        var unknown = restTemplate.exchange(createURLWithPort("/logic/nope/solve"), HttpMethod.POST,
                new HttpEntity<>(new ProofDto(List.of(), "nope", "P"), headers), ErrorResponse.class);
        assertError(HttpStatus.NOT_FOUND, unknown);

        var tampered = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of()), new StepDto("P", "->E [1, 1]", 0, Map.of())), "classical", "P");
        var bad = restTemplate.exchange(createURLWithPort("/logic/classical/solve"), HttpMethod.POST,
                new HttpEntity<>(tampered, headers), ErrorResponse.class);
        assertError(HttpStatus.BAD_REQUEST, bad);
        assertTrue(bad.getBody().message().startsWith("Line 2 "));

        var noBody = restTemplate.exchange(createURLWithPort("/logic/classical/solve"), HttpMethod.POST,
                new HttpEntity<>(null, headers), ErrorResponse.class);
        assertError(HttpStatus.BAD_REQUEST, noBody);
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    @Test
    public void doAction() {
        ProofDto proofDto = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of()),
                new StepDto("Q", "Ass", 1, Map.of())), "classical", "Q->P");
        ActionDto actionDto = new ActionDto("COPY", List.of(1), Map.of());
        ProofActionRequest request = new ProofActionRequest(actionDto, proofDto);
        var response = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST, new HttpEntity<>(request, headers), ProofResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, Objects.requireNonNull(response.getBody()).proof().steps().size());
    }

    @Test
    public void doActionReportsWhetherTheProofIsDone() {
        // The goal P is a top level step, though not the last one: the domain considers this proof done.
        var goalNotLast = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of()), new StepDto("Q", "Ass", 0, Map.of())), "classical", "P");
        var done = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("COPY", List.of(2), Map.of()), goalNotLast), headers), ProofResponse.class);
        assertEquals(HttpStatus.OK, done.getStatusCode());
        assertTrue(Objects.requireNonNull(done.getBody()).done());

        var open = new ProofDto(List.of(new StepDto("Q", "Ass", 0, Map.of())), "classical", "P");
        var notDone = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("COPY", List.of(1), Map.of()), open), headers), ProofResponse.class);
        assertEquals(HttpStatus.OK, notDone.getStatusCode());
        assertFalse(Objects.requireNonNull(notDone.getBody()).done());

        var json = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("COPY", List.of(1), Map.of()), open), headers), String.class);
        assertTrue(Objects.requireNonNull(json.getBody()).contains("\"done\":false"), json.getBody());
    }

    @Test
    public void postProof() {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        NaturalDeduction nd = new NaturalDeduction();
        var p = new VariableClassic("P");
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 1, Map.of()),
                new StepDto("P -> P", "->I [1-1]", 0, Map.of())
        ), "classical", "P -> P");
        nd.initializeProof(List.of(), new ImplicationClassic(p, p));
        nd.automate();

        HttpHeaders parts = new HttpHeaders();
        parts.setContentType(MediaType.TEXT_PLAIN);
        final ByteArrayResource byteArrayResource = new ByteArrayResource(nd.toString().getBytes()) {
            @Override
            public String getFilename() {
                return "myProof.pf";
            }
        };
        final HttpEntity<ByteArrayResource> partsEntity = new HttpEntity<>(byteArrayResource, parts);


        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", partsEntity);

        var response = restTemplate.exchange(createURLWithPort("/logic/classical/proof"), HttpMethod.POST,
                new HttpEntity<>(map, headers), ProofDto.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(proof, response.getBody());
    }

    private ResponseEntity<ErrorResponse> postUpload(String logic, String contents) {
        var upload = new HttpHeaders();
        upload.setContentType(MediaType.MULTIPART_FORM_DATA);
        var part = new HttpHeaders();
        part.setContentType(MediaType.TEXT_PLAIN);
        var resource = new ByteArrayResource(contents.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "myProof.pf";
            }
        };
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new HttpEntity<>(resource, part));
        return restTemplate.exchange(createURLWithPort("/logic/" + logic + "/proof"), HttpMethod.POST,
                new HttpEntity<>(map, upload), ErrorResponse.class);
    }

    private ResponseEntity<String> postAction(String logic, String json) {
        var jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(createURLWithPort("/logic/" + logic + "/action"), HttpMethod.POST,
                new HttpEntity<>(json, jsonHeaders), String.class);
    }

    private static void assertError(HttpStatus expected, ResponseEntity<ErrorResponse> response) {
        assertEquals(expected, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().message().isBlank());
    }

    @Test
    public void exercisesAreListedWithoutSolutions() {
        var response = restTemplate.getForEntity(createURLWithPort("/logic/classical/exercises"), ExerciseDto[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var exercises = Objects.requireNonNull(response.getBody());
        assertTrue(exercises.length >= 12, "about a dozen classical exercises");
        assertEquals(new ExerciseDto("modus-ponens", "Modus ponens", List.of("p", "p -> q"), "q", Difficulty.EASY), exercises[0]);

        var raw = restTemplate.getForObject(createURLWithPort("/logic/classical/exercises"), String.class);
        assertFalse(raw.contains("solution"), raw);
        assertFalse(raw.contains("->E"), raw);

        var modal = restTemplate.getForEntity(createURLWithPort("/logic/modal/exercises"), ExerciseDto[].class);
        assertEquals(HttpStatus.OK, modal.getStatusCode());
        var modalExercises = Objects.requireNonNull(modal.getBody());
        assertTrue(modalExercises.length >= 5, "a handful of modal exercises");
        assertEquals(new ExerciseDto("box-elimination", "What is necessary is true", List.of("[] p"), "p", Difficulty.EASY), modalExercises[0]);
        var rawModal = restTemplate.getForObject(createURLWithPort("/logic/modal/exercises"), String.class);
        assertFalse(rawModal.contains("solution"), rawModal);
        assertFalse(rawModal.contains("Refl"), rawModal);

        var unknown = restTemplate.exchange(createURLWithPort("/logic/nope/exercises"), HttpMethod.GET,
                new HttpEntity<>(null, headers), ErrorResponse.class);
        assertError(HttpStatus.NOT_FOUND, unknown);
        assertEquals("Unknown logic 'nope'", unknown.getBody().message());
    }

    private static final String DOUBLE_NEGATION_ELIMINATION = "- (- p)" + " ".repeat(11) + "Ass\n"
            + "p" + " ".repeat(11) + "-E [1]\n";

    @Test
    public void intuitionisticActionsAreTheClassicalOnesWithoutDoubleNegationElimination() {
        var classical = Objects.requireNonNull(restTemplate.getForObject(createURLWithPort("/logic/classical/actions"), ActionDescriptorDto[].class));
        var response = restTemplate.getForEntity(createURLWithPort("/logic/intuitionistic/actions"), ActionDescriptorDto[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var expected = Arrays.stream(classical).filter(action -> !action.name().equals("NOTE")).toList();
        assertEquals(expected, List.of(Objects.requireNonNull(response.getBody())));
        assertEquals(classical.length - 1, response.getBody().length);
    }

    @Test
    public void intuitionisticRejectsDoubleNegationElimination() {
        var premise = new ProofDto(List.of(new StepDto("- (- p)", "Ass", 0, Map.of())), "intuitionistic", "p");
        var note = new ProofActionRequest(new ActionDto("NOTE", List.of(1), Map.of()), premise);
        var rejected = restTemplate.exchange(createURLWithPort("/logic/intuitionistic/action"), HttpMethod.POST,
                new HttpEntity<>(note, headers), ErrorResponse.class);
        assertError(HttpStatus.BAD_REQUEST, rejected);
        assertEquals("Rule NOTE is not a rule of intuitionistic logic", rejected.getBody().message());

        var classical = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                new HttpEntity<>(note, headers), ProofResponse.class);
        assertEquals(HttpStatus.OK, classical.getStatusCode());
        assertTrue(Objects.requireNonNull(classical.getBody()).done());

        // A proof that already uses the rule does not replay either
        var classicalOnly = new ProofDto(List.of(new StepDto("- (- p)", "Ass", 0, Map.of()), new StepDto("p", "-E [1]", 0, Map.of())), "intuitionistic", "p");
        var replay = restTemplate.exchange(createURLWithPort("/logic/intuitionistic/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("COPY", List.of(1), Map.of()), classicalOnly), headers), ErrorResponse.class);
        assertError(HttpStatus.BAD_REQUEST, replay);
        assertTrue(replay.getBody().message().startsWith("Line 2 "), replay.getBody().message());
    }

    @Test
    public void intuitionisticActionsApply() {
        var proof = new ProofDto(List.of(new StepDto("p & q", "Ass", 0, Map.of())), "intuitionistic", "q");
        var response = restTemplate.exchange(createURLWithPort("/logic/intuitionistic/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("ANDE2", List.of(1), Map.of()), proof), headers), ProofResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var body = Objects.requireNonNull(response.getBody());
        assertTrue(body.success());
        assertTrue(body.done());
        assertEquals("intuitionistic", body.proof().logic());
    }

    @Test
    public void intuitionisticUploadRejectsAClassicalOnlyProof() {
        var rejected = postUpload("intuitionistic", DOUBLE_NEGATION_ELIMINATION);
        assertError(HttpStatus.BAD_REQUEST, rejected);
        assertTrue(rejected.getBody().message().startsWith("Line 2 "), rejected.getBody().message());

        var upload = new HttpHeaders();
        upload.setContentType(MediaType.MULTIPART_FORM_DATA);
        var part = new HttpHeaders();
        part.setContentType(MediaType.TEXT_PLAIN);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new HttpEntity<>(new ByteArrayResource(DOUBLE_NEGATION_ELIMINATION.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return "myProof.pf";
            }
        }, part));
        var classical = restTemplate.exchange(createURLWithPort("/logic/classical/proof"), HttpMethod.POST,
                new HttpEntity<>(map, upload), ProofDto.class);
        assertEquals(HttpStatus.CREATED, classical.getStatusCode());
        assertTrue(Objects.requireNonNull(classical.getBody()).isDone());
    }

    @Test
    public void intuitionisticHasNoSolver() {
        var response = restTemplate.exchange(createURLWithPort("/logic/intuitionistic/solve"), HttpMethod.POST,
                new HttpEntity<>(new ProofDto(List.of(), "intuitionistic", "p -> p"), headers), ErrorResponse.class);
        assertError(HttpStatus.BAD_REQUEST, response);
        assertEquals("There is no solver for the logic 'intuitionistic'", response.getBody().message());
    }

    @Test
    public void intuitionisticExercisesAreListed() {
        var response = restTemplate.getForEntity(createURLWithPort("/logic/intuitionistic/exercises"), ExerciseDto[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var ids = Arrays.stream(Objects.requireNonNull(response.getBody())).map(ExerciseDto::id).toList();
        assertTrue(ids.size() >= 12, "about a dozen intuitionistic exercises");
        assertTrue(ids.contains("modus-ponens"));
        assertTrue(ids.contains("excluded-middle-not-refutable"));
        assertFalse(ids.contains("double-negation-elimination"));
        assertFalse(ids.contains("excluded-middle"));
    }

    @Test
    public void unknownLogicIsNotFound() {
        var actions = restTemplate.exchange(createURLWithPort("/logic/nope/actions"), HttpMethod.GET,
                new HttpEntity<>(null, headers), ErrorResponse.class);
        assertError(HttpStatus.NOT_FOUND, actions);
        assertEquals("Unknown logic 'nope'", actions.getBody().message());

        var action = restTemplate.exchange(createURLWithPort("/logic/nope/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("COPY", List.of(1), Map.of()),
                        new ProofDto(List.of(), "nope", "P")), headers), ErrorResponse.class);
        assertError(HttpStatus.NOT_FOUND, action);

        assertError(HttpStatus.NOT_FOUND, postUpload("nope", "P" + " ".repeat(11) + "Ass"));
    }

    @Test
    public void unknownActionIsBadRequest() {
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of())), "classical", "P");
        var response = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("NOPE", List.of(1), Map.of()), proof), headers), ErrorResponse.class);
        assertError(HttpStatus.BAD_REQUEST, response);
        assertTrue(response.getBody().message().contains("NOPE"));
    }

    @Test
    public void malformedExpressionIsBadRequest() {
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of())), "classical", "P");
        var response = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("ASSUME", List.of(), Map.of("expression", "P Q")), proof), headers), ErrorResponse.class);
        assertError(HttpStatus.BAD_REQUEST, response);
    }

    @Test
    public void blankAssumeExpressionIsBadRequest() {
        var proof = new ProofDto(List.of(new StepDto("P -> Q", "Ass", 0, Map.of()), new StepDto("P", "Ass", 0, Map.of())), "classical", "Q");
        for (var extra : List.of(Map.<String, String>of(), Map.of("expression", ""), Map.of("expression", "   "))) {
            var response = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                    new HttpEntity<>(new ProofActionRequest(new ActionDto("ASSUME", List.of(), extra), proof), headers), ErrorResponse.class);
            assertError(HttpStatus.BAD_REQUEST, response);
            assertEquals("ASSUME needs an expression", response.getBody().message());
        }
    }

    @Test
    public void unparsableExpressionMessageDoesNotMentionNull() {
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of())), "classical", "P");
        var response = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("ASSUME", List.of(), Map.of("expression", "P ->")), proof), headers), ErrorResponse.class);
        assertError(HttpStatus.BAD_REQUEST, response);
        assertTrue(response.getBody().message().startsWith("Cannot build action 'ASSUME': "));
        assertFalse(response.getBody().message().contains("null"), response.getBody().message());
    }

    @Test
    public void everyClassicalActionWithBlankInputsIsBadRequestOrRejectedButNeverAServerError() {
        var proof = new ProofDto(List.of(new StepDto("P -> Q", "Ass", 0, Map.of()), new StepDto("P", "Ass", 0, Map.of())), "classical", "Q");
        var descriptors = restTemplate.getForObject(createURLWithPort("/logic/classical/actions"), ActionDescriptorDto[].class);
        for (var descriptor : Objects.requireNonNull(descriptors)) {
            for (var expression : List.of("", "  ", "P ->", "->", "(")) {
                var lines = Collections.nCopies((int) descriptor.params().stream().filter(ParamKind.INT::equals).count(), 1);
                var response = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                        new HttpEntity<>(new ProofActionRequest(new ActionDto(descriptor.name(), lines, Map.of("expression", expression)), proof), headers), String.class);
                var description = descriptor.name() + " '" + expression + "': " + response.getBody();
                assertTrue(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is4xxClientError(), description);
                assertFalse(Objects.requireNonNull(response.getBody()).contains("null"), description);
            }
        }
    }

    @Test
    public void blankModalExpressionIsBadRequest() {
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of("state", "s0"))), "modal", "P");
        for (var extra : List.of(Map.of("state", "s0"), Map.of("expression", "", "state", "s0"), Map.of("expression", " ", "state", "s0"))) {
            var response = restTemplate.exchange(createURLWithPort("/logic/modal/action"), HttpMethod.POST,
                    new HttpEntity<>(new ProofActionRequest(new ActionDto("Ass", List.of(), extra), proof), headers), ErrorResponse.class);
            assertError(HttpStatus.BAD_REQUEST, response);
            assertEquals("Ass needs an expression", response.getBody().message());
        }
        var unparsable = restTemplate.exchange(createURLWithPort("/logic/modal/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("Ass", List.of(), Map.of("expression", "P ->", "state", "s0")), proof), headers), ErrorResponse.class);
        assertError(HttpStatus.BAD_REQUEST, unparsable);
        assertFalse(unparsable.getBody().message().contains("null"), unparsable.getBody().message());
    }

    @Test
    public void tamperedProofIsBadRequest() {
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of()), new StepDto("P", "->E [1, 1]", 0, Map.of())), "classical", "P");
        var response = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("COPY", List.of(1), Map.of()), proof), headers), ErrorResponse.class);
        assertError(HttpStatus.BAD_REQUEST, response);
        assertTrue(response.getBody().message().startsWith("Line 2 "));
    }

    @Test
    public void missingActionOrProofIsBadRequest() {
        var noAction = postAction("classical", """
                {"proofDto": {"logic": "classical", "goal": "P"}}""");
        assertEquals(HttpStatus.BAD_REQUEST, noAction.getStatusCode());
        assertTrue(Objects.requireNonNull(noAction.getBody()).contains("actionDto"));

        var noProof = postAction("classical", """
                {"actionDto": {"name": "COPY", "sources": [1]}}""");
        assertEquals(HttpStatus.BAD_REQUEST, noProof.getStatusCode());

        assertEquals(HttpStatus.BAD_REQUEST, postAction("classical", "{}").getStatusCode());
    }

    @Test
    public void malformedBodyIsBadRequest() {
        var response = postAction("classical", "{not json");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("message"));
    }

    @Test
    public void outOfRangeSourceIsRejectedWithAMessage() {
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of())), "classical", "P");
        var response = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("COPY", List.of(9), Map.of()), proof), headers), ProofResponse.class);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).success());
        assertEquals("Line 9 does not exist, the proof has 1 lines", response.getBody().message());
        assertEquals(proof, response.getBody().proof());
    }

    // A proof loaded from text (frontend loadProofFromText) is replayed through the same out-of-range COPY; the levels
    // a client sends do not shape the replay, so the 202 answers with the levels the rules imply, not the request's.
    @Test
    public void rejectedActionAnswersWithTheReplayedLevels() {
        var misIndented = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of()), new StepDto("Q", "Ass", 1, Map.of()),
                new StepDto("P", "Rep [1]", 0, Map.of()), new StepDto("Q -> P", "->I [2-3]", 1, Map.of())), "classical", "Q -> P");
        var response = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("COPY", List.of(5), Map.of()), misIndented), headers), ProofResponse.class);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).done());
        assertEquals(List.of(0, 1, 1, 0), response.getBody().proof().steps().stream().map(StepDto::assmsLevel).toList());
    }

    // The frontend's "Undo last step" resends the whole proof minus its last step to POST .../action, with a
    // deliberately out-of-range COPY (see outOfRangeSourceIsRejectedWithAMessage above and
    // frontend/src/service/actions.ts#undoLastStep): the server is stateless and always replays every step of the
    // ProofDto it is given, so this is safe, and the rejected (202) response still carries the domain's own `done`
    // and the replayed proof unchanged. This checks the discharge case plan.md and issue #121 call out specifically:
    // DT/NOTI-style rules mark an earlier assumption step disabled only by dropping the assumption level of the
    // steps that follow it (ProofStep.disable() is never serialized, see StepDto), so once the discharging step
    // itself is undone, replaying what remains proves the assumption is open again, and `done` correctly flips back.
    @Test
    public void undoDropsTheLastStepAndRevalidatesTheDischarge() {
        // "P" is assumed at level 1, then discharged by ->I into "P -> P" at level 0: the goal is reached.
        var assumption = new StepDto("P", "Ass", 1, Map.of());
        var discharging = new StepDto("P -> P", "->I [1-1]", 0, Map.of());
        var finished = new ProofDto(List.of(assumption, discharging), "classical", "P -> P");

        var doneResponse = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("COPY", List.of(3), Map.of()), finished), headers), ProofResponse.class);
        assertEquals(HttpStatus.ACCEPTED, doneResponse.getStatusCode());
        assertFalse(Objects.requireNonNull(doneResponse.getBody()).success());
        assertTrue(doneResponse.getBody().done());
        assertEquals(finished, doneResponse.getBody().proof());

        // Undo: resend without the last (discharging) step.
        var undone = new ProofDto(List.of(assumption), "classical", "P -> P");
        var undoResponse = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("COPY", List.of(2), Map.of()), undone), headers), ProofResponse.class);
        assertEquals(HttpStatus.ACCEPTED, undoResponse.getStatusCode());
        assertFalse(Objects.requireNonNull(undoResponse.getBody()).success());
        // The remaining prefix (just the open assumption) still replays validly, and is echoed back unchanged...
        assertEquals(undone, undoResponse.getBody().proof());
        // ...but the discharge is gone along with the step that caused it: the goal is not proved any more.
        assertFalse(undoResponse.getBody().done());
    }

    @Test
    public void notApplicableActionIsRejectedWithAMessage() {
        var proof = new ProofDto(List.of(new StepDto("P", "Ass", 0, Map.of())), "classical", "P");
        var response = restTemplate.exchange(createURLWithPort("/logic/classical/action"), HttpMethod.POST,
                new HttpEntity<>(new ProofActionRequest(new ActionDto("ANDE1", List.of(1), Map.of()), proof), headers), ProofResponse.class);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertFalse(Objects.requireNonNull(response.getBody()).success());
        assertEquals("Rule ANDE1 cannot be applied to lines [1]", response.getBody().message());
    }

    @Test
    public void garbledUploadIsBadRequest() {
        var goodLine = "P" + " ".repeat(11) + "Ass";
        var blankLine = postUpload("classical", goodLine + "\n\n" + goodLine);
        assertError(HttpStatus.BAD_REQUEST, blankLine);
        assertTrue(blankLine.getBody().message().startsWith("Line 2 "));

        assertError(HttpStatus.BAD_REQUEST, postUpload("classical", "P Ass"));
        assertError(HttpStatus.BAD_REQUEST, postUpload("classical", ""));
        assertError(HttpStatus.BAD_REQUEST, postUpload("classical", "P" + " ".repeat(11) + "Nope"));
        assertError(HttpStatus.BAD_REQUEST, postUpload("modal", "s0:"));
    }

    @Test
    public void modalActionWithOmittedParametersIsAccepted() {
        var response = postAction("modal", """
                {"actionDto": {"name": "Ass", "extraParameters": {"expression": "P", "state": "s0"}},
                 "proofDto": {"logic": "modal", "goal": "P"}}""");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("\"success\":true"));

        var withStep = postAction("modal", """
                {"actionDto": {"name": "Rep", "sources": [1]},
                 "proofDto": {"logic": "modal", "goal": "P",
                              "steps": [{"expression": "P", "rule": "Ass", "assmsLevel": 0, "extraParameters": {"state": "s0"}}]}}""");
        assertEquals(HttpStatus.OK, withStep.getStatusCode());
    }
}
