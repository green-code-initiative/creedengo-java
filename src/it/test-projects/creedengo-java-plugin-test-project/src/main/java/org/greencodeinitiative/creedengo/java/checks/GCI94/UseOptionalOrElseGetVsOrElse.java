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

    private static final String DEFAULT_NAME = "default";

    private static Optional<String> variable = Optional.empty();

    public static final String NAME = Optional.of("creedengo").orElse(getUnpredictedMethod()); // Noncompliant {{Use optional orElseGet instead of orElse.}}

    public static final String NAME_CONCAT = Optional.of("creedengo").orElse("prefix_" + getUnpredictedMethod()); // Noncompliant {{Use optional orElseGet instead of orElse.}}

    public static final String NAME_NEW = Optional.of("creedengo").orElse(new StringBuilder().toString()); // Noncompliant {{Use optional orElseGet instead of orElse.}}

    public static final String NAME7 = variable.orElse(getUnpredictedMethod()); // Noncompliant {{Use optional orElseGet instead of orElse.}}

    public static final String NAME2 = Optional.of("creedengo").orElseGet(() -> getUnpredictedMethod()); // Compliant

    public static final String NAME3 = Optional.of("creedengo").orElseGet(UseOptionalOrElseGetVsOrElse::getUnpredictedMethod); // Compliant

    public static final String NAME4 = Optional.of("creedengo").orElse(DEFAULT_NAME); // Compliant - constant

    public static final String NAME5 = Optional.of("creedengo").orElse("fallback"); // Compliant - string literal

    public static final String NAME6 = Optional.of("creedengo").orElse(null); // Compliant - null literal

    public static final Boolean FLAG = Optional.of(Boolean.TRUE).orElse(Boolean.FALSE); // Compliant - static field reference

    private static String getUnpredictedMethod() {
        return "unpredicted";
    }

    static void testVariableCases() {
        Optional<String> opt = Optional.of("creedengo");
        String r1 = opt.orElse(getUnpredictedMethod()); // Noncompliant {{Use optional orElseGet instead of orElse.}}
        String r2 = opt.orElse("a" + getUnpredictedMethod()); // Noncompliant {{Use optional orElseGet instead of orElse.}}
        String r3 = opt.orElse(Boolean.TRUE ? getUnpredictedMethod() : DEFAULT_NAME); // Noncompliant {{Use optional orElseGet instead of orElse.}}
        String r4 = opt.orElse(new String("default")); // Noncompliant {{Use optional orElseGet instead of orElse.}}
        String r5 = opt.orElse(DEFAULT_NAME); // Compliant - constant
    }

}
