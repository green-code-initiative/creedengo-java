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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

class TestClass {

	@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 10))
	public void springMaxRetryOK() {
	}

	@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 10, multiplier = 2))
	public void springMaxRetryWithFullParamsOK() {
	}

	@Retryable()
	public void springMaxRetryWithoutParamsOK() {
	}

	@Retryable(maxAttempts = 5, backoff = @Backoff(delay = 10, multiplier = 2)) // Noncompliant {{Please use optimized @Retryable parameters.}}
	public void springMaxRetryMaxAttemptsKO() {
	}

	@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 10, multiplier = 10000)) // Noncompliant {{Please use optimized @Retryable parameters.}}
	public void springMaxRetryTimeOutKO() {
	}
}
