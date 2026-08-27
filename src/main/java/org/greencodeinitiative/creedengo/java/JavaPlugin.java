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

import org.greencodeinitiative.creedengo.java.reusedrules.NativeRuleTagger;
import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;

public class JavaPlugin implements Plugin {

    private static final String REUSED_NATIVE_RULES_CATEGORY = "creedengo";

    @Override
    public void define(Context context) {
        // server extensions -> objects are instantiated during server startup
        context.addExtension(JavaRulesDefinition.class);

        // batch extensions -> objects are instantiated during code analysis
        context.addExtension(JavaCheckRegistrar.class);

        // tags/untags, on plugin start/stop, the native rules reused as "eco-design" and
        // declared in creedengo_way_profile.json; activating them in the "creedengo way" profile
        // itself requires no configuration at all, see JavaCreedengoWayProfile
        context.addExtension(NativeRuleTagger.class);
        context.addExtension(PropertyDefinition.builder(NativeRuleTagger.PROPERTY_TOKEN)
                .name("Reused native rules: admin token")
                .description("User token with the 'Administer Quality Profiles' permission, used to tag "
                        + "(and untag on uninstall) the native SonarQube rules reused as eco-design rules. "
                        + "Leave empty to disable this automatic tagging.")
                .category(REUSED_NATIVE_RULES_CATEGORY)
                .type(PropertyType.PASSWORD)
                .build());

    }

}
