#!/usr/bin/env sh
# tool_docker-logs.sh — Tail the stack logs (Sonar + Postgres).
#
# Pass extra args to scope to a single service, e.g. :
#     ./tool_docker-logs.sh sonar
#
# Override the runtime with COMPOSE_CMD if needed.
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
. "$SCRIPT_DIR/tool_lib_container.sh"
cd "$SCRIPT_DIR"

echo "[creedengo-infra] tailing logs with: $COMPOSE  (Ctrl+C to quit)"
# shellcheck disable=SC2086
$COMPOSE logs -f "$@"

