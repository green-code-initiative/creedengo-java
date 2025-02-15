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

import java.util.Optional;

class UseOptionalOrElseGetVsOrElse {

    private static Optional<String> variable = Optional.empty();

    public static final String NAME = Optional.of("creedengo").orElse(getUnpredictedMethod()); // Noncompliant {{Use optional orElseGet instead of orElse.}}

    public static final String NAME2 = Optional.of("creedengo").orElseGet(() -> getUnpredictedMethod()); // Compliant

    public static final String NAME3 = variable.orElse(getUnpredictedMethod()); // Compliant

    private static String getUnpredictedMethod() {
        return "unpredicted";
    }

}
