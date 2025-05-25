package org.greencodeinitiative.creedengo.java.checks;

import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.CheckVerifier;

public class UseFilterBeforeSortTest {
    @Test
    void testHasIssues() {
        CheckVerifier.newVerifier()
                .onFile("src/test/files/UseFilterBeforeSort.java")
                .withCheck(new UseFilterBeforeSort())
                .verifyIssues();
    }
}
