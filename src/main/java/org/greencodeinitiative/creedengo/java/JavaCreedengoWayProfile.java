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

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

import static org.greencodeinitiative.creedengo.java.JavaRulesDefinition.LANGUAGE;
import static org.greencodeinitiative.creedengo.java.JavaRulesDefinition.REPOSITORY_KEY;

public final class JavaCreedengoWayProfile implements BuiltInQualityProfilesDefinition {
	static final String PROFILE_NAME = "creedengo way";
	static final String PROFILE_PATH = JavaCreedengoWayProfile.class.getPackageName().replace('.', '/') + "/creedengo_way_profile.json";

	@Override
	public void define(Context context) {
		NewBuiltInQualityProfile creedengoProfile = context.createBuiltInQualityProfile(PROFILE_NAME, LANGUAGE);
		loadProfile(creedengoProfile);
		creedengoProfile.done();
	}

	private void loadProfile(NewBuiltInQualityProfile profile) {
		BuiltInQualityProfileJsonLoader.load(profile, REPOSITORY_KEY, PROFILE_PATH);
	}
}
