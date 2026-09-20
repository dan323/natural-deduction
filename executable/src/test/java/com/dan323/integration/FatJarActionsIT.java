package com.dan323.integration;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.model.ActionDescriptorDto;
import com.dan323.model.ProofDto;
import com.dan323.uses.classical.ClassicGetActions;
import com.dan323.uses.modal.ModalGetActions;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Starts the packaged Spring Boot fat jar (not the classpath) and checks that it serves the action lists and the
 * solver. Wiring and resources can behave differently inside nested jars, so {@link RestServiceIT}, which runs on the
 * plain classpath, cannot catch a regression there.
 * <p>
 * The jar is built by the {@code package} phase, so this only works from {@code mvn verify}.
 * The jar location comes from the {@code fatJar} system property set in the pom; the jar's output
 * goes to {@code fat-jar-it.log} next to it.
 */
public class FatJarActionsIT {

    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(90);

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static Process process;
    private static Path log;
    private static int port;

    @BeforeAll
    static void startJar() throws Exception {
        var jar = Path.of(System.getProperty("fatJar", "target/executable-0.1-SNAPSHOT.jar"));
        assertTrue(Files.isRegularFile(jar), "Packaged jar not found at " + jar.toAbsolutePath()
                + ". Run this test through 'mvn verify' so the package phase builds it first.");

        try (var socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        log = jar.toAbsolutePath().resolveSibling("fat-jar-it.log");
        var java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        process = new ProcessBuilder(java, "-jar", jar.toAbsolutePath().toString(), "--server.port=" + port)
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .start();

        var deadline = System.nanoTime() + STARTUP_TIMEOUT.toNanos();
        while (true) {
            if (!process.isAlive()) {
                fail("The jar exited during startup:\n" + Files.readString(log));
            }
            if (System.nanoTime() > deadline) {
                fail("The jar did not start in " + STARTUP_TIMEOUT + ":\n" + Files.readString(log));
            }
            try {
                if (get("/actuator/health").statusCode() == 200) {
                    return;
                }
            } catch (IOException notUpYet) {
                // connection refused while Tomcat starts
            }
            // waits between polls; returns early if the jar dies, which the loop head reports
            process.waitFor(500, TimeUnit.MILLISECONDS);
        }
    }

    @AfterAll
    static void stopJar() {
        if (process != null) {
            process.destroy();
            try {
                if (!process.waitFor(10, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }
    }

    private static HttpResponse<String> get(String path) throws IOException {
        return send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(5)).GET().build());
    }

    private static HttpResponse<String> post(String path, Object body) throws IOException {
        return send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(30)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body))).build());
    }

    private static HttpResponse<String> send(HttpRequest request) throws IOException {
        try {
            return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    private static List<ActionDescriptorDto> actionsFromJar(String logic) throws IOException {
        var response = get("/logic/" + logic + "/actions");
        if (response.statusCode() != 200) {
            fail("GET /logic/" + logic + "/actions from the fat jar returned " + response.statusCode()
                    + ":\n" + Files.readString(log));
        }
        return Arrays.asList(MAPPER.readValue(response.body(), ActionDescriptorDto[].class));
    }

    @Test
    void classicalActionsAreServedByTheFatJar() throws IOException {
        var fromJar = actionsFromJar("classical");

        assertEquals(AvailableAction.values().length, fromJar.size());
        assertEquals(Arrays.stream(AvailableAction.values()).map(AvailableAction::name).collect(Collectors.toSet()),
                fromJar.stream().map(ActionDescriptorDto::name).collect(Collectors.toSet()));
        assertEquals(new ClassicGetActions().perform(), fromJar);
    }

    @Test
    void modalActionsAreServedByTheFatJar() throws IOException {
        var fromJar = actionsFromJar("modal");
        var onClasspath = new ModalGetActions().perform();

        assertFalse(onClasspath.isEmpty());
        assertEquals(onClasspath, fromJar);
    }

    @Test
    void theSolverWorksInTheFatJar() throws IOException {
        for (var logic : List.of("classical", "modal")) {
            var response = post("/logic/" + logic + "/solve", new ProofDto(List.of(), logic, "P -> P"));

            assertEquals(200, response.statusCode(), response.body());
            assertTrue(MAPPER.readValue(response.body(), ProofDto.class).isDone(), logic);
        }
    }
}
