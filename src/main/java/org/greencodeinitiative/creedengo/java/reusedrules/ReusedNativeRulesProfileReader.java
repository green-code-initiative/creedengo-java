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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Reads the {@code reusedNativeRules} sub-structure of a {@code creedengo_way_profile.json} file
 * bundled in the plugin.
 *
 * <p>The plugin's nominal profile loading ({@code BuiltInQualityProfileJsonLoader}) only reads the
 * {@code ruleKeys} key and ignores {@code reusedNativeRules}; this reader exposes that extra
 * structure so the reused native rules can be tagged at server startup.</p>
 *
 * <p>Expected structure:</p>
 * <pre>
 * {
 *   "ruleKeys": [ "GCI1" ],
 *   "reusedNativeRules": [
 *     { "key": "java:S6904", "reference": "https://..." }
 *   ]
 * }
 * </pre>
 */
public final class ReusedNativeRulesProfileReader {

	private final String profilePath;

	public ReusedNativeRulesProfileReader(String profilePath) {
		this.profilePath = profilePath;
	}

	/**
	 * @return the ordered list of reused native rule keys declared in the profile, or an empty list
	 *         when the profile has no {@code reusedNativeRules} entry
	 */
	public List<String> readReusedNativeRuleKeys() {
		try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(profilePath)) {
			if (in == null) {
				return List.of();
			}
			JsonElement root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			if (!root.isJsonObject()) {
				return List.of();
			}
			JsonElement reused = root.getAsJsonObject().get("reusedNativeRules");
			if (reused == null || !reused.isJsonArray()) {
				return List.of();
			}
			List<String> keys = new ArrayList<>();
			for (JsonElement element : reused.getAsJsonArray()) {
				extractKey(element).ifPresent(keys::add);
			}
			return keys;
		} catch (Exception e) {
			throw new IllegalStateException("Unable to read reused native rules from profile: " + profilePath, e);
		}
	}

	private static java.util.Optional<String> extractKey(JsonElement element) {
		if (!element.isJsonObject()) {
			return java.util.Optional.empty();
		}
		JsonObject object = element.getAsJsonObject();
		JsonElement key = object.get("key");
		if (key == null || !key.isJsonPrimitive()) {
			return java.util.Optional.empty();
		}
		String value = key.getAsString();
		return (value == null || value.isBlank()) ? java.util.Optional.empty() : java.util.Optional.of(value.trim());
	}
}
