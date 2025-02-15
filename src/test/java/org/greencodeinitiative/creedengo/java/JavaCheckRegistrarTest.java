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
package org.greencodeinitiative.creedengo.java;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.CheckRegistrar;

import static org.assertj.core.api.Assertions.assertThat;

class JavaCheckRegistrarTest {

    @Test
    void checkNumberRules() {
        final CheckRegistrar.RegistrarContext context = new CheckRegistrar.RegistrarContext();

        final JavaCheckRegistrar registrar = new JavaCheckRegistrar();
        registrar.register(context);
        assertThat(context.checkClasses())
                .describedAs("All implemented rules must be registered into " + JavaCheckRegistrar.class)
                .containsExactlyInAnyOrder(getDefinedRules().toArray(new Class[0]));
        assertThat(context.testCheckClasses()).isEmpty();

    }

    static Set<Class<?>> getDefinedRules() {
        Reflections r = new Reflections(JavaCheckRegistrar.class.getPackageName() + ".checks");
        return r.getTypesAnnotatedWith(Rule.class);
    }

}
