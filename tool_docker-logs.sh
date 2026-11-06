#!/usr/bin/env sh
# tool_docker-logs.sh — Tail the stack logs (Sonar + Postgres).
#
# Pass extra args to scope to a single service, e.g. :
#     ./tool_docker-logs.sh sonar
#
# Auto-detects the container engine + compose flavor (Docker, Rancher
# Desktop, OrbStack, Colima, Podman, nerdctl, Finch). See
# `tool_lib_container.sh` for the override knobs.
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
. "$SCRIPT_DIR/tool_lib_container.sh"
cd "$SCRIPT_DIR"

printf '[creedengo-infra] tailing logs on %s via `%s`  (Ctrl+C to quit)\n' \
  "$CONTAINER_PRODUCT" "$COMPOSE"

# shellcheck disable=SC2086
$COMPOSE logs -f "$@"

