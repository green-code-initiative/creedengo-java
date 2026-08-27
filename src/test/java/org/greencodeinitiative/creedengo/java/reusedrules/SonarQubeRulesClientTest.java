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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises {@link SonarQubeRulesClient} against a stub HTTP server standing in for the two
 * SonarQube Web API endpoints it calls ({@code api/rules/search} and {@code api/rules/update}).
 */
class SonarQubeRulesClientTest {

	private HttpServer server;
	private String baseUrl;
	private final Map<String, String> ruleTags = new ConcurrentHashMap<>();
	private final AtomicInteger updateCalls = new AtomicInteger();
	private String lastAuthorizationHeader;

	@BeforeEach
	void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/api/rules/search", this::handleSearch);
		server.createContext("/api/rules/update", this::handleUpdate);
		server.start();
		baseUrl = "http://localhost:" + server.getAddress().getPort();
		ruleTags.put("java:S6904", "legacy");
	}

	@AfterEach
	void stopServer() {
		server.stop(0);
	}

	@Test
	void addsTagWhilePreservingExistingOnes() {
		new SonarQubeRulesClient(baseUrl, "my-token").addTag("java:S6904", "eco-design");

		assertThat(ruleTags.get("java:S6904")).contains("legacy", "eco-design");
		assertThat(updateCalls).hasValue(1);
		assertThat(lastAuthorizationHeader).isNotBlank();
	}

	@Test
	void doesNotCallUpdateWhenTagAlreadyPresent() {
		ruleTags.put("java:S6904", "legacy,eco-design");

		new SonarQubeRulesClient(baseUrl, "my-token").addTag("java:S6904", "eco-design");

		assertThat(updateCalls).hasValue(0);
	}

	@Test
	void removesTagWhilePreservingOtherOnes() {
		ruleTags.put("java:S6904", "legacy,eco-design");

		new SonarQubeRulesClient(baseUrl, "my-token").removeTag("java:S6904", "eco-design");

		assertThat(ruleTags.get("java:S6904")).isEqualTo("legacy");
		assertThat(updateCalls).hasValue(1);
	}

	@Test
	void doesNotCallUpdateWhenTagAlreadyAbsent() {
		new SonarQubeRulesClient(baseUrl, "my-token").removeTag("java:S6904", "eco-design");

		assertThat(updateCalls).hasValue(0);
	}

	private void handleSearch(HttpExchange exchange) throws IOException {
		lastAuthorizationHeader = exchange.getRequestHeaders().getFirst("Authorization");
		String tags = ruleTags.getOrDefault("java:S6904", "");
		String tagsJson = tags.isBlank() ? "[]" : "[\"" + String.join("\",\"", tags.split(",")) + "\"]";
		String body = "{\"rules\":[{\"key\":\"java:S6904\",\"tags\":" + tagsJson + ",\"sysTags\":[]}],\"total\":1}";
		respond(exchange, 200, body);
	}

	private void handleUpdate(HttpExchange exchange) throws IOException {
		lastAuthorizationHeader = exchange.getRequestHeaders().getFirst("Authorization");
		updateCalls.incrementAndGet();
		String form = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		for (String pair : form.split("&")) {
			String[] kv = pair.split("=", 2);
			if (kv.length == 2 && "tags".equals(kv[0])) {
				ruleTags.put("java:S6904", URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
			}
		}
		respond(exchange, 200, "{}");
	}

	private static void respond(HttpExchange exchange, int status, String body) throws IOException {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, bytes.length);
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}
	}
}
