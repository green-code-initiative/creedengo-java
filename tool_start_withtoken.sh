#!/usr/bin/env sh
# tool_start_withtoken.sh — (Re)build and start the stack, exposing a
# SonarQube user token to the container as the SONAR_TOKEN environment
# variable (useful for plugins that auto-authenticate or for scripted
# scanner runs spawned inside the network).
#
# Usage :
#   1. Generate a token in SonarQube : My Account → Security → Generate.
#   2. Export it BEFORE running this script, e.g. :
#          export SONAR_TOKEN=squ_abc123…
#          ./tool_start_withtoken.sh
#      Or inline :
#          SONAR_TOKEN=squ_abc123… ./tool_start_withtoken.sh
#
# Auto-detects the compose runtime; override with COMPOSE_CMD if needed.
set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
. "$SCRIPT_DIR/tool_lib_container.sh"
cd "$SCRIPT_DIR"

if [ -z "${SONAR_TOKEN:-}" ]; then
  cat >&2 <<'EOF'
[creedengo-infra] ERROR — environment variable SONAR_TOKEN is not set.

Generate a token in SonarQube (My Account → Security) and export it
before running this script :

    export SONAR_TOKEN=squ_xxxxxxxxxxxxxxxxxxxxxxxx
    ./tool_start_withtoken.sh

Or inline :

    SONAR_TOKEN=squ_xxxxxxxxxxxxxxxxxxxxxxxx ./tool_start_withtoken.sh
EOF
  exit 2
fi

echo "[creedengo-infra] starting stack with: $COMPOSE  (SONAR_TOKEN=***)"
# shellcheck disable=SC2086
SONAR_TOKEN="$SONAR_TOKEN" $COMPOSE up --build -d

