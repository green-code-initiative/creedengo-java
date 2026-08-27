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
package org.greencodeinitiative.creedengo.java.reusedrules;

import java.util.List;

import org.greencodeinitiative.creedengo.java.JavaCreedengoWayProfile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReusedNativeRulesProfileReaderTest {

	@Test
	void readsKeysDeclaredInProfile() {
		List<String> keys = new ReusedNativeRulesProfileReader("reusedrules/profile-with-reused.json")
				.readReusedNativeRuleKeys();
		assertThat(keys).containsExactly("java:S6904", "java:S1234");
	}

	@Test
	void returnsEmptyWhenProfileHasNoReusedNativeRules() {
		List<String> keys = new ReusedNativeRulesProfileReader("reusedrules/profile-without-reused.json")
				.readReusedNativeRuleKeys();
		assertThat(keys).isEmpty();
	}

	@Test
	void returnsEmptyWhenProfileResourceIsMissing() {
		List<String> keys = new ReusedNativeRulesProfileReader("reusedrules/does-not-exist.json")
				.readReusedNativeRuleKeys();
		assertThat(keys).isEmpty();
	}

	@Test
	void readsTheRealPluginProfile() {
		List<String> keys = new ReusedNativeRulesProfileReader(JavaCreedengoWayProfile.PROFILE_PATH)
				.readReusedNativeRuleKeys();
		assertThat(keys).contains("java:S6904");
	}
}
