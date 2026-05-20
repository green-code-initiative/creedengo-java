#!/usr/bin/env sh
# tool_docker-init.sh — Build images and start the local SonarQube stack.
#
# Auto-detects the available compose runtime (docker compose, docker-compose,
# podman compose, podman-compose, nerdctl compose). Override with
# COMPOSE_CMD if needed, e.g. : COMPOSE_CMD='docker compose' ./tool_docker-init.sh
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
. "$SCRIPT_DIR/tool_lib_container.sh"
cd "$SCRIPT_DIR"

echo "[creedengo-infra] starting stack with: $COMPOSE"
# shellcheck disable=SC2086  # $COMPOSE may contain spaces (e.g. "docker compose")
$COMPOSE up --build -d

