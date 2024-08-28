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
}
