package com.dan323.integration;

import org.junit.jupiter.api.Test;
import org.springframework.core.SpringVersion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The {@code executable} module has Spring Boot as its parent, so the Spring Framework version it runs with is the one
 * managed by that Boot release. The root pom manages Spring separately ({@code spring.version}) for the domain modules.
 * Maven cannot share a property between the two, so this test fails when a Boot bump (for example by dependabot) is
 * not followed by the matching {@code spring.version} bump.
 */
class SpringVersionAlignmentTest {

    private static final Pattern SPRING_VERSION = Pattern.compile("<spring\\.version>([^<]+)</spring\\.version>");

    @Test
    void rootPomSpringVersionMatchesTheOneSpringBootManages() throws IOException {
        // surefire runs with the module directory as working directory
        var rootPom = Path.of("..", "pom.xml");
        assertTrue(Files.isRegularFile(rootPom), "Root pom not found at " + rootPom.toAbsolutePath());

        var matcher = SPRING_VERSION.matcher(Files.readString(rootPom));
        assertTrue(matcher.find(), "No <spring.version> property in " + rootPom.toAbsolutePath());

        assertEquals(SpringVersion.getVersion(), matcher.group(1).trim(),
                "spring.version in the root pom must match the Spring Framework version managed by the Spring Boot "
                        + "parent of executable/pom.xml");
    }
}
