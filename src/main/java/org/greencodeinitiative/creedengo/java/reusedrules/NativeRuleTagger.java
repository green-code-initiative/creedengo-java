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
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Startable;
import org.sonar.api.config.Configuration;
import org.sonar.api.platform.Server;
import org.sonar.api.server.ServerSide;

import org.greencodeinitiative.creedengo.java.JavaCreedengoWayProfile;

/**
 * Tags the reused native rules declared in {@code creedengo_way_profile.json} with the
 * {@value #TAG} label when the plugin starts, and removes that tag again when it stops (e.g. on
 * uninstall/upgrade).
 *
 * <p>This reuses, as its single source of reused native rule keys,
 * {@link JavaCreedengoWayProfile#reusedNativeRuleKeys()} — the very same list already used to
 * build the built-in "creedengo way" profile. Rule <b>activation</b> in that profile requires no
 * admin API call at all (see {@link JavaCreedengoWayProfile}). Rule <b>tagging</b>, on the other
 * hand, is metadata SonarQube only lets an administrator change through the Web API
 * ({@code api/rules/update}); there is no declarative/built-in equivalent, so this component needs
 * a single admin token, configured once by the SonarQube administrator, to perform that one HTTP
 * call — nothing else is configurable, and when no token is set the feature simply stays inactive
 * instead of failing.</p>
 */
@ServerSide
public final class NativeRuleTagger implements Startable {

	public static final String TAG = "eco-design";
	public static final String PROPERTY_TOKEN = "sonar.creedengo.reusedNativeRules.token";

	private static final Logger LOGGER = LoggerFactory.getLogger(NativeRuleTagger.class);

	private final Configuration configuration;
	private final Server server;
	private final Supplier<List<String>> reusedNativeRuleKeysSupplier;

	public NativeRuleTagger(Configuration configuration, Server server) {
		this(configuration, server, JavaCreedengoWayProfile::reusedNativeRuleKeys);
	}

	NativeRuleTagger(Configuration configuration, Server server, Supplier<List<String>> reusedNativeRuleKeysSupplier) {
		this.configuration = configuration;
		this.server = server;
		this.reusedNativeRuleKeysSupplier = reusedNativeRuleKeysSupplier;
	}

	@Override
	public void start() {
		withClientAndRuleKeys((client, ruleKeys) -> ruleKeys.forEach(ruleKey -> client.addTag(ruleKey, TAG)));
	}

	@Override
	public void stop() {
		withClientAndRuleKeys((client, ruleKeys) -> ruleKeys.forEach(ruleKey -> client.removeTag(ruleKey, TAG)));
	}

	private void withClientAndRuleKeys(java.util.function.BiConsumer<SonarQubeRulesClient, List<String>> action) {
		List<String> ruleKeys = reusedNativeRuleKeysSupplier.get();
		if (ruleKeys.isEmpty()) {
			return;
		}
		configuration.get(PROPERTY_TOKEN)
				.filter(token -> !token.isBlank())
				.ifPresentOrElse(
						token -> action.accept(new SonarQubeRulesClient(server.getPublicRootUrl(), token), ruleKeys),
						() -> LOGGER.debug("Property '{}' not set: reused native rules will not be auto-tagged", PROPERTY_TOKEN));
	}
}

