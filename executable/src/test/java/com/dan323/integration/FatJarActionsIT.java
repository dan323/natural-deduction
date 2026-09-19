package com.dan323.integration;

import com.dan323.classical.proof.AvailableAction;
import com.dan323.uses.classical.ClassicGetActions;
import com.dan323.uses.modal.ModalGetActions;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Starts the packaged Spring Boot fat jar (not the classpath) and checks that the reflection-based
 * action discovery still finds every rule there. Reflections scans differently inside nested jars,
 * so {@link RestServiceIT}, which runs on the plain classpath, cannot catch a regression.
 * <p>
 * The jar is built by the {@code package} phase, so this only works from {@code mvn verify}.
 * The jar location comes from the {@code fatJar} system property set in the pom; the jar's output
 * goes to {@code fat-jar-it.log} next to it.
 */
public class FatJarActionsIT {

    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(90);

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
            Thread.sleep(500);
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
        try {
            return CLIENT.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                    .timeout(Duration.ofSeconds(5)).GET().build(), HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    private static List<String> actionsFromJar(String logic) throws IOException {
        var response = get("/logic/" + logic + "/actions");
        if (response.statusCode() != 200) {
            fail("GET /logic/" + logic + "/actions from the fat jar returned " + response.statusCode()
                    + " (204 means Reflections found no actions):\n" + Files.readString(log));
        }
        return Arrays.asList(MAPPER.readValue(response.body(), String[].class));
    }

    @Test
    void classicalActionsAreDiscoveredInTheFatJar() throws IOException {
        var fromJar = actionsFromJar("classical");

        assertEquals(AvailableAction.values().length, fromJar.size());
        assertEquals(Arrays.stream(AvailableAction.values()).map(AvailableAction::name).collect(Collectors.toSet()),
                fromJar.stream().map(action -> action.split("\\(")[0]).collect(Collectors.toSet()));
        assertEquals(new HashSet<>(new ClassicGetActions().perform()), new HashSet<>(fromJar));
    }

    @Test
    void modalActionsAreDiscoveredInTheFatJar() throws IOException {
        var fromJar = actionsFromJar("modal");
        Set<String> onClasspath = new HashSet<>(new ModalGetActions().perform());

        assertFalse(onClasspath.isEmpty());
        assertEquals(onClasspath.size(), fromJar.size());
        assertEquals(onClasspath, new HashSet<>(fromJar));
    }
}
