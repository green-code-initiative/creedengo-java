#!/usr/bin/env sh
# tool_stop.sh — Stop the SonarQube stack without removing containers or
# volumes. Use tool_start.sh to bring it back up.
#
# Override the runtime with COMPOSE_CMD if needed.
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
. "$SCRIPT_DIR/tool_lib_container.sh"
cd "$SCRIPT_DIR"

echo "[creedengo-infra] stopping stack with: $COMPOSE"
# shellcheck disable=SC2086
$COMPOSE stop

