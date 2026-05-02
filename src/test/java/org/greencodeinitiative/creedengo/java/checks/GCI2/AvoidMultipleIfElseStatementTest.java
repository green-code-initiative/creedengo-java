/*
 * creedengo - Java language - Provides rules to reduce the environmental footprint of your Java programs
 * Copyright © 2024 Green Code Initiative (https://green-code-initiative.org/)
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
package org.greencodeinitiative.creedengo.java.checks.GCI2;

import org.greencodeinitiative.creedengo.java.checks.AvoidMultipleIfElseStatement;
import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.CheckVerifier;

class AvoidMultipleIfElseStatementTest {
    @Test
    void test() {
        CheckVerifier.newVerifier()
                .onFile(System.getProperty("testfiles.path") + "/GCI2/AvoidMultipleIfElseStatement.java")
                .withCheck(new AvoidMultipleIfElseStatement())
                .verifyIssues();
        CheckVerifier.newVerifier()
                .onFile(System.getProperty("testfiles.path") + "/GCI2/AvoidMultipleIfElseStatementNoIssue.java")
                .withCheck(new AvoidMultipleIfElseStatement())
                .verifyNoIssues();
    }

    @Test
    void testInterfaceMethodStatement() {
        CheckVerifier.newVerifier()
                .onFile(System.getProperty("testfiles.path") + "/GCI2/AvoidMultipleIfElseStatementInterfaceNoIssue.java")
                .withCheck(new AvoidMultipleIfElseStatement())
                .verifyNoIssues();
    }

    @Test
    void testNoBlockStatement() {
        CheckVerifier.newVerifier()
                .onFile(System.getProperty("testfiles.path") + "/GCI2/AvoidMultipleIfElseStatementNoBlockNoIssue.java")
                .withCheck(new AvoidMultipleIfElseStatement())
                .verifyNoIssues();
    }

    @Test
    void testCompareMethod() {
        CheckVerifier.newVerifier()
                .onFile(System.getProperty("testfiles.path") + "/GCI2/AvoidMultipleIfElseStatementCompareMethodNoIssue.java")
                .withCheck(new AvoidMultipleIfElseStatement())
                .verifyNoIssues();
    }


}
