#!/usr/bin/env sh
# tool_docker-clean.sh — Tear down the stack AND remove its named volumes.
#
# Use this when you want a fully clean SonarQube DB / data, e.g. after
# changing the plugin's rules / repository keys, or when recovering from
# a corrupted persistent volume.
#
# Auto-detects the container engine + compose flavor (Docker, Rancher
# Desktop, OrbStack, Colima, Podman, nerdctl, Finch). See
# `tool_lib_container.sh` for the override knobs (COMPOSE_CMD,
# CONTAINER_ENGINE_CMD, COMPOSE_VERBOSE).
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
. "$SCRIPT_DIR/tool_lib_container.sh"
cd "$SCRIPT_DIR"

printf '[creedengo-infra] stopping & removing volumes on %s via `%s`\n' \
  "$CONTAINER_PRODUCT" "$COMPOSE"

# shellcheck disable=SC2086
$COMPOSE down --volumes

