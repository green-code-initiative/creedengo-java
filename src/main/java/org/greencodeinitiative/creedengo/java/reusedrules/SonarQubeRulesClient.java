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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal client for the two SonarQube Web API endpoints needed to tag/untag a native rule
 * ({@code api/rules/search} and {@code api/rules/update}), both requiring the
 * <i>Administer Quality Profiles</i> permission.
 *
 * <p>This mirrors, with the JDK {@link HttpClient} instead of the {@code sonar-ws} client library
 * (to avoid shipping/shading an extra dependency inside the plugin), the same two calls already
 * used by {@code creedengo-integration-test}'s {@code ReusedRulesConfigurator} during integration
 * tests. Only the rule tagging is handled here: activating a reused rule in the "creedengo way"
 * profile requires no API call at all, see {@link org.greencodeinitiative.creedengo.java.JavaCreedengoWayProfile}.</p>
 */
public final class SonarQubeRulesClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(SonarQubeRulesClient.class);
	private static final Duration TIMEOUT = Duration.ofSeconds(10);

	private final HttpClient httpClient;
	private final String baseUrl;
	private final String authorizationHeader;

	public SonarQubeRulesClient(String baseUrl, String token) {
		this.httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		this.authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((token + ":").getBytes(StandardCharsets.UTF_8));
	}

	/** Add {@code tag} to the rule's editable tags, preserving existing tags and ignoring sysTags. */
	public void addTag(String ruleKey, String tag) {
		RuleTags current = fetchTags(ruleKey);
		if (current == null) {
			return;
		}
		if (current.sysTags.contains(tag) || current.tags.contains(tag)) {
			LOGGER.debug("Rule '{}' already has tag '{}': skipping", ruleKey, tag);
			return;
		}
		Set<String> updated = new LinkedHashSet<>(current.tags);
		updated.add(tag);
		updateTags(ruleKey, updated);
		LOGGER.info("Tagged reused native rule '{}' with '{}'", ruleKey, tag);
	}

	/** Remove {@code tag} from the rule's editable tags, preserving the other tags. */
	public void removeTag(String ruleKey, String tag) {
		RuleTags current = fetchTags(ruleKey);
		if (current == null || !current.tags.contains(tag)) {
			return;
		}
		Set<String> updated = new LinkedHashSet<>(current.tags);
		updated.remove(tag);
		updateTags(ruleKey, updated);
		LOGGER.info("Removed tag '{}' from reused native rule '{}'", tag, ruleKey);
	}

	private RuleTags fetchTags(String ruleKey) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(baseUrl + "/api/rules/search?rule_key=" + ruleKey))
					.header("Authorization", authorizationHeader)
					.timeout(TIMEOUT)
					.GET()
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				LOGGER.warn("Unable to fetch rule '{}' (HTTP {}): skipping tag update", ruleKey, response.statusCode());
				return null;
			}
			JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();
			JsonArray rules = body.getAsJsonArray("rules");
			if (rules == null || rules.isEmpty()) {
				LOGGER.warn("Reused native rule '{}' not found on SonarQube: skipping (is the language plugin installed?)", ruleKey);
				return null;
			}
			JsonObject rule = rules.get(0).getAsJsonObject();
			return new RuleTags(toSet(rule.getAsJsonArray("tags")), toSet(rule.getAsJsonArray("sysTags")));
		} catch (HttpTimeoutException e) {
			LOGGER.warn("Timeout while fetching rule '{}': skipping tag update", ruleKey);
			return null;
		} catch (Exception e) {
			LOGGER.warn("Error while fetching rule '{}': skipping tag update ({})", ruleKey, e.getMessage());
			return null;
		}
	}

	private void updateTags(String ruleKey, Set<String> tags) {
		try {
			String form = "key=" + ruleKey + "&tags=" + String.join(",", tags);
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(baseUrl + "/api/rules/update"))
					.header("Authorization", authorizationHeader)
					.header("Content-Type", "application/x-www-form-urlencoded")
					.timeout(TIMEOUT)
					.POST(HttpRequest.BodyPublishers.ofString(form))
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				LOGGER.warn("Unable to update tags for rule '{}' (HTTP {})", ruleKey, response.statusCode());
			}
		} catch (Exception e) {
			LOGGER.warn("Error while updating tags for rule '{}': {}", ruleKey, e.getMessage());
		}
	}

	private static Set<String> toSet(JsonArray array) {
		if (array == null) {
			return Set.of();
		}
		return array.asList().stream().map(JsonElement::getAsString).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	private static final class RuleTags {
		final Set<String> tags;
		final Set<String> sysTags;

		RuleTags(Set<String> tags, Set<String> sysTags) {
			this.tags = tags;
			this.sysTags = sysTags;
		}
	}
}
