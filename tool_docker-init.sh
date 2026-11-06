#!/usr/bin/env sh
# tool_docker-init.sh — Build images and start the local SonarQube stack.
#
# Auto-detects the container engine + compose flavor on this host (Docker
# Desktop, Rancher Desktop, OrbStack, Colima, Podman, nerdctl, Finch …).
# See `tool_lib_container.sh` for the full detection matrix and override
# environment variables (COMPOSE_CMD, CONTAINER_ENGINE_CMD, COMPOSE_VERBOSE).
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
. "$SCRIPT_DIR/tool_lib_container.sh"
cd "$SCRIPT_DIR"

printf '[creedengo-infra] starting stack on %s via `%s`\n' \
  "$CONTAINER_PRODUCT" "$COMPOSE"

# shellcheck disable=SC2086  # $COMPOSE may contain spaces (e.g. "docker compose")
$COMPOSE up --build -d

