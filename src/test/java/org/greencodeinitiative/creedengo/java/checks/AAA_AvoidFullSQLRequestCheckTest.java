package org.greencodeinitiative.creedengo.java.checks;

import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.CheckVerifier;

public class AAA_AvoidFullSQLRequestCheckTest {

    @Test
    void test() {
        CheckVerifier.newVerifier()
                .onFile("src/test/files/AAA_AvoidFullSQLRequestTestResourceFile.java")
                .withCheck(new AAA_AvoidFullSQLRequest())
                .verifyIssues();
    }

}
