package com.dan323.integration;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.classical.proof.NaturalDeduction;
import com.dan323.expressions.classical.ImplicationClassic;
import com.dan323.expressions.classical.VariableClassic;
import com.dan323.main.Application;
import com.dan323.main.ApplicationConfiguration;
import com.dan323.model.ActionDto;
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
        var actionArray = Arrays.stream(AvailableAction.values()).map(AvailableAction::name).collect(Collectors.toSet());
        var response = restTemplate
                .exchange(createURLWithPort("/logic/classical/actions"),
                        HttpMethod.GET,
                        new HttpEntity<>(null, headers),
                        String[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(actionArray, Arrays.stream(response.getBody()).map(act -> act.split("\\(")[0]).collect(Collectors.toSet()));
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
