#!/usr/bin/env sh
# tool_docker-clean.sh — Tear down the stack AND remove its named volumes.
#
# Use this when you want a fully clean SonarQube DB / data, e.g. after
# changing the plugin's rules / repository keys, or when recovering from
# a corrupted persistent volume.
#
# Override the runtime with COMPOSE_CMD, e.g. :
#     COMPOSE_CMD='docker compose' ./tool_docker-clean.sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
. "$SCRIPT_DIR/tool_lib_container.sh"
cd "$SCRIPT_DIR"

echo "[creedengo-infra] stopping & removing volumes with: $COMPOSE"
# shellcheck disable=SC2086
$COMPOSE down --volumes

