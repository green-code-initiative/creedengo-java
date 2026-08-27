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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.config.Configuration;
import org.sonar.api.platform.Server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NativeRuleTaggerTest {

	private HttpServer server;
	private String baseUrl;
	private final AtomicInteger searchCalls = new AtomicInteger();

	@BeforeEach
	void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/api/rules/search", exchange -> {
			searchCalls.incrementAndGet();
			byte[] body = "{\"rules\":[{\"key\":\"java:S6904\",\"tags\":[],\"sysTags\":[]}],\"total\":1}"
					.getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, body.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(body);
			}
			exchange.close();
		});
		server.createContext("/api/rules/update", exchange -> {
			exchange.getRequestBody().readAllBytes();
			byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, body.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(body);
			}
			exchange.close();
		});
		server.start();
		baseUrl = "http://localhost:" + server.getAddress().getPort();
	}

	@AfterEach
	void stopServer() {
		server.stop(0);
	}

	@Test
	void doesNothingWhenTokenNotConfigured() {
		Configuration configuration = mock(Configuration.class);
		when(configuration.get(NativeRuleTagger.PROPERTY_TOKEN)).thenReturn(Optional.empty());
		Server sonarServer = mock(Server.class);
		when(sonarServer.getPublicRootUrl()).thenReturn(baseUrl);
		NativeRuleTagger tagger = new NativeRuleTagger(configuration, sonarServer, () -> List.of("java:S6904"));
		tagger.start();
		tagger.stop();

		assertThat(searchCalls).hasValue(0);
	}

	@Test
	void tagsOnStartAndUntagsOnStop() {
		Configuration configuration = mock(Configuration.class);
		when(configuration.get(NativeRuleTagger.PROPERTY_TOKEN)).thenReturn(Optional.of("my-token"));
		Server sonarServer = mock(Server.class);
		when(sonarServer.getPublicRootUrl()).thenReturn(baseUrl);
		NativeRuleTagger tagger = new NativeRuleTagger(configuration, sonarServer, () -> List.of("java:S6904"));
		tagger.start();
		tagger.stop();

		assertThat(searchCalls).hasValue(2);
	}

	@Test
	void doesNothingWhenProfileHasNoReusedNativeRules() {
		Configuration configuration = mock(Configuration.class);
		when(configuration.get(NativeRuleTagger.PROPERTY_TOKEN)).thenReturn(Optional.of("my-token"));
		Server sonarServer = mock(Server.class);
		when(sonarServer.getPublicRootUrl()).thenReturn(baseUrl);
		NativeRuleTagger tagger = new NativeRuleTagger(configuration, sonarServer, List::of);
		tagger.start();

		assertThat(searchCalls).hasValue(0);
	}
}
