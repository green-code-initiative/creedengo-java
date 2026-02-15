/*
 * ecoCode - Java language - Provides rules to reduce the environmental footprint of your Java programs
 * Copyright © 2023 Green Code Initiative (https://www.ecocode.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.greencodeinitiative.creedengo.java.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.CheckVerifier;

class UseEveryColumnQueriedTest {

    @Test
    void UseEveryColumnQueriedTestExtractSelectedSQLColumns(){
        String query = "\"SELECT id AS registration_id,\tfirst, last as Final, AGE FROM Registration\"";
        List<String> columns = UseEveryColumnQueried.extractSelectedSQLColumns(query);
        assertEquals(4, columns.size());
        assertEquals("REGISTRATION_ID", columns.get(0));
        assertEquals("FIRST", columns.get(1));
        assertEquals("FINAL", columns.get(2));
        assertEquals("AGE", columns.get(3));
    }

    @Test
    void UseEveryColumnQueriedAttributeQueryNonCompliant() {
        CheckVerifier.newVerifier()
                .onFile("src/test/files/UseEveryColumnQueried/UseEveryColumnQueriedAttributeQueryNonCompliant.java")
                .withCheck(new UseEveryColumnQueried())
                .verifyIssues();
    }

    @Test
    void UseEveryColumnQueriedLitteralQueryNonCompliant() {
        CheckVerifier.newVerifier()
                .onFile("src/test/files/UseEveryColumnQueried/UseEveryColumnQueriedLitteralQueryNonCompliant.java")
                .withCheck(new UseEveryColumnQueried())
                .verifyIssues();
    }

    @Test
    void UseEveryColumnQueriedUseColumnIdsAndNameAttributesNonCompliant() {
        CheckVerifier.newVerifier()
                .onFile("src/test/files/UseEveryColumnQueried/UseEveryColumnQueriedUseColumnIdsAndNameAttributesNonCompliant.java")
                .withCheck(new UseEveryColumnQueried())
                .verifyIssues();
    }

    @Test
    void UseEveryColumnQueriedAttributeQueryCompliant() {
        CheckVerifier.newVerifier()
                .onFile("src/test/files/UseEveryColumnQueried/UseEveryColumnQueriedAttributeQueryCompliant.java")
                .withCheck(new UseEveryColumnQueried())
                .verifyNoIssues();
    }

    @Test
    void UseEveryColumnQueriedLitteralQueryCompliant() {
        CheckVerifier.newVerifier()
                .onFile("src/test/files/UseEveryColumnQueried/UseEveryColumnQueriedLitteralQueryCompliant.java")
                .withCheck(new UseEveryColumnQueried())
                .verifyNoIssues();
    }

    @Test
    void UseEveryColumnQueriedUseColumnIdsAndNameAttributesCompliant() {
        CheckVerifier.newVerifier()
                .onFile("src/test/files/UseEveryColumnQueried/UseEveryColumnQueriedUseColumnIdsAndNameAttributesCompliant.java")
                .withCheck(new UseEveryColumnQueried())
                .verifyNoIssues();
    }

    @Test
    void UseEveryColumnQueriedMultipleQueriesCompliant() {
        CheckVerifier.newVerifier()
                .onFile("src/test/files/UseEveryColumnQueried/UseEveryColumnQueriedMultipleQueriesCompliant.java")
                .withCheck(new UseEveryColumnQueried())
                .verifyNoIssues();
    }

    @Test
    void UseEveryColumnQueriedUseMethodCompliant() {
        CheckVerifier.newVerifier()
                .onFile("src/test/files/UseEveryColumnQueried/UseEveryColumnQueriedUseMethodCompliant.java")
                .withCheck(new UseEveryColumnQueried())
                .verifyNoIssues();
    }

    @Test
    void UseEveryColumnQueriedSelectStar() {
        CheckVerifier.newVerifier()
                .onFile("src/test/files/UseEveryColumnQueried/UseEveryColumnQueriedSelectStar.java")
                .withCheck(new UseEveryColumnQueried())
                .verifyNoIssues();
    }
}
