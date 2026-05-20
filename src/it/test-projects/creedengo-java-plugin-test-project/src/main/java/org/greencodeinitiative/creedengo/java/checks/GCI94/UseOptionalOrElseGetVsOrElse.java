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

package org.greencodeinitiative.creedengo.java.checks;

import java.util.Optional;

class UseOptionalOrElseGetVsOrElse {

    private static Optional<String> variable = Optional.empty();

    public static final String NAME = Optional.of("creedengo").orElse(getUnpredictedMethod()); // Noncompliant {{Use optional orElseGet instead of orElse.}}

    public static final String NAME2 = Optional.of("creedengo").orElseGet(() -> getUnpredictedMethod()); // Compliant

    public static final String NAME3 = variable.orElse(getUnpredictedMethod()); // Compliant

    private static String getUnpredictedMethod() {
        return "unpredicted";
    }

    private static final String DEFAULT_VALUE = "default";

    void badMethodCall(String value) {
        Optional.ofNullable(value).orElse(getDefaultValue()); // Noncompliant
    }

    void badNewObject(String value) {
        Optional.ofNullable(value).orElse(new String("default")); // Noncompliant
    }

    void goodBooleanConstant(Boolean value) {
        Optional.ofNullable(value).orElse(Boolean.FALSE);
    }

    void goodStringLiteral(String value) {
        Optional.ofNullable(value).orElse("default");
    }

    void goodNull(String value) {
        Optional.ofNullable(value).orElse(null);
    }

    void goodIdentifier(String value) {
        Optional.ofNullable(value).orElse(DEFAULT_VALUE);
    }

    void goodAlreadyOrElseGet(String value) {
        Optional.ofNullable(value).orElseGet(this::getDefaultValue);
    }

    private String getDefaultValue() {
        return "default";
    }

    void badConcatenation(String value, String suffix) {
        Optional.ofNullable(value).orElse("default" + suffix); // Noncompliant
    }

    void badTernary(String value, boolean condition) {
        Optional.ofNullable(value).orElse(condition ? "default" : "fallback"); // Noncompliant
    }

    void badCast(String value, Object defaultValue) {
        Optional.ofNullable(value).orElse((String) defaultValue); // Noncompliant
    }

    void badArrayAccess(String value, String[] values) {
        Optional.ofNullable(value).orElse(values[0]); // Noncompliant
    }

}
