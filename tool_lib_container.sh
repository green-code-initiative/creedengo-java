#!/usr/bin/env sh
# tool_lib_container.sh
# -----------------------------------------------------------------------------
# Shared helpers for creedengo-infra tooling : detect which container-compose
# runtime is available on the host and expose it through ${COMPOSE}.
#
# This file is meant to be SOURCED from the other tool_*.sh scripts :
#
#     SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
#     . "$SCRIPT_DIR/tool_lib_container.sh"
#     cd "$SCRIPT_DIR"
#     $COMPOSE up --build -d
#
# Detection order (first match wins) :
#   1.  $COMPOSE_CMD (explicit override, e.g.  COMPOSE_CMD='docker compose')
#   2.  docker compose         (Docker CE v2 plugin — most common)
#   3.  docker-compose         (Docker CE v1 legacy binary)
#   4.  podman compose         (Podman 4+ native sub-command)
#   5.  podman-compose         (standalone Python wrapper)
#   6.  nerdctl compose        (containerd / Rancher Desktop / Finch)
#
# The function prints the resolved command to stdout and the discovery trace
# to stderr (only when COMPOSE_VERBOSE=1). It does NOT exit on error so that
# the caller can give a useful contextual message.
# -----------------------------------------------------------------------------

# Print to stderr only when COMPOSE_VERBOSE=1
_compose_log() {
  if [ "${COMPOSE_VERBOSE:-0}" = "1" ]; then
    printf '[container-runtime] %s\n' "$*" >&2
  fi
}

# Try a candidate. Args: name, probe-command...
# On success prints the name (= command line) to stdout and returns 0.
_compose_try() {
  _name="$1"
  shift
  _compose_log "probing: $_name"
  if "$@" >/dev/null 2>&1; then
    printf '%s' "$_name"
    return 0
  fi
  return 1
}

detect_compose() {
  # 1) Explicit override
  if [ -n "${COMPOSE_CMD:-}" ]; then
    _compose_log "using \$COMPOSE_CMD override : $COMPOSE_CMD"
    printf '%s' "$COMPOSE_CMD"
    return 0
  fi

  # 2) docker compose (v2)
  if command -v docker >/dev/null 2>&1; then
    _compose_try 'docker compose' docker compose version && return 0
  fi

  # 3) docker-compose (v1)
  if command -v docker-compose >/dev/null 2>&1; then
    _compose_try 'docker-compose' docker-compose version && return 0
  fi

  # 4) podman compose (Podman 4+)
  if command -v podman >/dev/null 2>&1; then
    _compose_try 'podman compose' podman compose version && return 0
  fi

  # 5) podman-compose (Python wrapper)
  if command -v podman-compose >/dev/null 2>&1; then
    _compose_try 'podman-compose' podman-compose version && return 0
  fi

  # 6) nerdctl compose (containerd)
  if command -v nerdctl >/dev/null 2>&1; then
    _compose_try 'nerdctl compose' nerdctl compose version && return 0
  fi

  return 1
}

# Resolve once and expose via $COMPOSE. Caller can then run e.g. `$COMPOSE up`.
COMPOSE="$(detect_compose)" || {
  cat >&2 <<'EOF'
[container-runtime] ERROR — no container compose runtime found.

Looked for (in order) :
  - docker compose      (Docker CE v2 plugin)
  - docker-compose      (Docker CE v1)
  - podman compose      (Podman 4+)
  - podman-compose      (Python wrapper)
  - nerdctl compose     (containerd / Rancher Desktop / Finch)

Install one of the above, or force a specific command :

    COMPOSE_CMD='docker compose' ./tool_docker-init.sh

EOF
  return 1 2>/dev/null || exit 1
}

# Friendly banner (silent unless verbose)
_compose_log "using : $COMPOSE"

# Make available to subshells in case a script forks (e.g. `xargs sh -c …`)
export COMPOSE

