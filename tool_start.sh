#!/usr/bin/env sh
# tool_start.sh — Start the (already created) SonarQube stack.
#
# Equivalent to "make start". Use tool_docker-init.sh the first time.
# Override the runtime with COMPOSE_CMD if needed.
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
. "$SCRIPT_DIR/tool_lib_container.sh"
cd "$SCRIPT_DIR"

echo "[creedengo-infra] starting stack with: $COMPOSE"
# shellcheck disable=SC2086
$COMPOSE start

