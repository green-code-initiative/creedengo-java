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

import java.util.List;

import org.greencodeinitiative.creedengo.java.reusedrules.ReusedNativeRulesProfileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

import static org.greencodeinitiative.creedengo.java.JavaRulesDefinition.LANGUAGE;
import static org.greencodeinitiative.creedengo.java.JavaRulesDefinition.REPOSITORY_KEY;

public final class JavaCreedengoWayProfile implements BuiltInQualityProfilesDefinition {
	static final String PROFILE_NAME = "creedengo way";
	public static final String PROFILE_PATH = JavaCreedengoWayProfile.class.getPackageName().replace('.', '/') + "/creedengo_way_profile.json";

	private static final Logger LOGGER = LoggerFactory.getLogger(JavaCreedengoWayProfile.class);

	@Override
	public void define(Context context) {
		NewBuiltInQualityProfile creedengoProfile = context.createBuiltInQualityProfile(PROFILE_NAME, LANGUAGE);
		loadProfile(creedengoProfile);
		activateReusedNativeRules(creedengoProfile);
		creedengoProfile.done();
	}

	private void loadProfile(NewBuiltInQualityProfile profile) {
		BuiltInQualityProfileJsonLoader.load(profile, REPOSITORY_KEY, PROFILE_PATH);
	}

	/**
	 * The list of reused native rule keys (e.g. {@code java:S6904}) declared in the
	 * {@code reusedNativeRules} sub-structure of {@value #PROFILE_PATH}.
	 *
	 * <p>This is the single source of truth for reused native rules in this plugin: it is used
	 * both to activate them in this built-in "creedengo way" profile (below) and, reusing this very
	 * same method, by {@link org.greencodeinitiative.creedengo.java.reusedrules.NativeRuleTagger}
	 * to know which rules to tag/untag when the plugin starts/stops.</p>
	 */
	public static List<String> reusedNativeRuleKeys() {
		return new ReusedNativeRulesProfileReader(PROFILE_PATH).readReusedNativeRuleKeys();
	}

	/**
	 * Activate, in this built-in profile, the native SonarQube rules declared as reused
	 * eco-design rules in the {@code reusedNativeRules} sub-structure of the profile JSON
	 * (e.g. {@code java:S6904}).
	 *
	 * <p>This reuses the very same mechanism that activates the plugin's own rules
	 * ({@link BuiltInQualityProfilesDefinition}, run internally by SonarQube at server startup):
	 * {@link NewBuiltInQualityProfile#activateRule(String, String)} accepts any repository key,
	 * not only the plugin's own, so no admin Web API call or credentials are needed. The rule
	 * keeps its own default severity (no override).</p>
	 */
	private void activateReusedNativeRules(NewBuiltInQualityProfile profile) {
		for (String ruleKey : reusedNativeRuleKeys()) {
			int separator = ruleKey.indexOf(':');
			if (separator <= 0 || separator == ruleKey.length() - 1) {
				LOGGER.warn("Invalid reused native rule key '{}' in {}: expected 'repository:key', skipping", ruleKey, PROFILE_PATH);
				continue;
			}
			profile.activateRule(ruleKey.substring(0, separator), ruleKey.substring(separator + 1));
		}
	}
}


