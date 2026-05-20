#!/usr/bin/env sh
# tool_lib_container.sh
# -----------------------------------------------------------------------------
# Shared helpers for the creedengo-infra dev tooling.
#
# Detects, at source-time:
#
#   1. The container *engine* actually reachable on this host
#      → exported as $CONTAINER_ENGINE (one of: docker, podman, nerdctl, finch)
#      and $CONTAINER_PRODUCT (cosmetic label, e.g. "Docker Desktop",
#      "Rancher Desktop (dockerd)", "Podman", "Finch", "OrbStack", "Colima"…).
#
#   2. The matching `compose` command line that can talk to it
#      → exported as $COMPOSE (whitespace-separated, e.g. "docker compose").
#
# Usage from the tool_*.sh scripts:
#
#     SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
#     . "$SCRIPT_DIR/tool_lib_container.sh"
#     cd "$SCRIPT_DIR"
#     # shellcheck disable=SC2086
#     $COMPOSE up --build -d
#
# Environment overrides (highest priority first):
#   • COMPOSE_CMD           — force the compose command, e.g. 'docker compose'
#                              or 'podman compose --in-pod=false'.
#   • CONTAINER_ENGINE_CMD  — force the underlying engine binary (docker,
#                              podman, nerdctl, finch). Used only as a hint
#                              when COMPOSE_CMD is not set.
#   • DOCKER_HOST           — honoured as-is (Colima / Lima / SSH remote daemons).
#   • COMPOSE_VERBOSE=1     — print discovery trace to stderr.
#   • COMPOSE_QUIET=1       — suppress the resolved-runtime banner on stdout.
#
# Detection order (first match wins). Every probe also requires that the
# matching daemon answers `<engine> info`, so we don't hand back a compose
# binary that has nothing to talk to:
#
#   1.  $COMPOSE_CMD                                  (explicit override)
#   2.  docker compose      (Docker CE v2 plugin, also: Docker Desktop,
#                            Rancher Desktop dockerd backend, OrbStack,
#                            Colima docker runtime)
#   3.  docker-compose      (Docker CE v1 legacy binary)
#   4.  podman compose      (Podman 4+ native sub-command)
#   5.  podman-compose      (Python wrapper)
#   6.  nerdctl compose     (containerd / Rancher Desktop containerd
#                            backend / Lima nerdctl)
#   7.  finch compose       (AWS Finch)
#
# The library NEVER calls `exit` from a sourced context — instead the
# calling script gets `return 1`. If the library is executed directly
# (./tool_lib_container.sh) it falls back to `exit 1`.
# -----------------------------------------------------------------------------

# ---------- internal helpers ------------------------------------------------

_ce_log() {
  if [ "${COMPOSE_VERBOSE:-0}" = "1" ]; then
    printf '[container-runtime] %s\n' "$*" >&2
  fi
}

_ce_silent() { "$@" >/dev/null 2>&1; }

_ce_have() { command -v "$1" >/dev/null 2>&1; }

# Is the given engine's daemon actually reachable ?
_ce_engine_alive() {
  _ce_silent "$@" info
}

# Cosmetic engine label, best-effort. Falls back to the binary name.
_ce_engine_product() {
  _bin="$1"
  case "$_bin" in
    docker)
      if [ -n "${RD_PATH:-}" ] || [ -d "$HOME/.rd" ]; then
        printf 'Rancher Desktop (dockerd backend)'; return
      fi
      if [ -S "/var/run/docker.sock.raw" ] || [ -d "/Applications/OrbStack.app" ]; then
        printf 'OrbStack'; return
      fi
      if _ce_have colima && _ce_silent colima status; then
        printf 'Colima'; return
      fi
      _name="$(docker info --format '{{.ServerVersion}} ({{.OperatingSystem}})' 2>/dev/null || true)"
      if [ -n "$_name" ]; then
        printf 'Docker — %s' "$_name"
      else
        printf 'Docker'
      fi
      ;;
    podman)
      _name="$(podman info --format '{{.Host.Distribution.Distribution}} {{.Version.Version}}' 2>/dev/null || true)"
      if [ -n "$_name" ]; then
        printf 'Podman — %s' "$_name"
      else
        printf 'Podman'
      fi
      ;;
    nerdctl)
      if [ -n "${RD_PATH:-}" ] || [ -d "$HOME/.rd" ]; then
        printf 'Rancher Desktop (containerd backend)'
      else
        printf 'nerdctl / containerd'
      fi
      ;;
    finch) printf 'AWS Finch' ;;
    *)     printf '%s' "$_bin" ;;
  esac
}

# Probe a compose flavor. On success:
#   - prints "<engine>|<product>|<command-line>" to stdout,
#   - returns 0.
# Args: human-name, engine-binary, compose-args...
_ce_try() {
  _name="$1"
  _engine="$2"
  shift 2
  _ce_log "probing: $_name"
  if _ce_silent "$@" version && _ce_engine_alive "$_engine"; then
    printf '%s|%s|%s' "$_engine" "$(_ce_engine_product "$_engine")" "$_name"
    return 0
  fi
  return 1
}

# ---------- public API ------------------------------------------------------

# Echoes "<engine>|<product>|<compose-command>" on success, returns 1 otherwise.
# The pipe-separated payload is parsed by the caller (the `$(...)` sub-shell
# eats any global variables we set here, so we serialise everything we want
# to propagate).
detect_compose() {
  # 1) Explicit override — trust the user. We still try to fingerprint the
  #    engine so the banner remains informative.
  if [ -n "${COMPOSE_CMD:-}" ]; then
    _ce_log "using \$COMPOSE_CMD override: $COMPOSE_CMD"
    _eng="$(printf '%s' "$COMPOSE_CMD" | awk '{print $1}')"
    if _ce_have "$_eng"; then
      _prod="$(_ce_engine_product "$_eng")"
    else
      _prod="$_eng"
    fi
    printf '%s|%s|%s' "$_eng" "$_prod" "$COMPOSE_CMD"
    return 0
  fi

  # If the user pinned an engine binary but not a compose flavor, prefer
  # the matching subcommand.
  case "${CONTAINER_ENGINE_CMD:-}" in
    podman)  _ce_try 'podman compose'  podman  podman  compose  && return 0 ;;
    nerdctl) _ce_try 'nerdctl compose' nerdctl nerdctl compose  && return 0 ;;
    finch)   _ce_try 'finch compose'   finch   finch   compose  && return 0 ;;
    docker)  _ce_try 'docker compose'  docker  docker  compose  && return 0 ;;
  esac

  # 2) docker compose (v2 plugin) — Docker Desktop / Rancher dockerd /
  #    OrbStack / Colima docker runtime
  if _ce_have docker; then
    _ce_try 'docker compose' docker docker compose && return 0
  fi

  # 3) docker-compose (v1 legacy)
  if _ce_have docker-compose; then
    _ce_log 'probing: docker-compose'
    if _ce_silent docker-compose version && _ce_engine_alive docker; then
      printf '%s|%s|%s' docker "$(_ce_engine_product docker)" 'docker-compose'
      return 0
    fi
  fi

  # 4) podman compose (Podman 4+)
  if _ce_have podman; then
    _ce_try 'podman compose' podman podman compose && return 0
  fi

  # 5) podman-compose (Python wrapper) — gated on `podman info`.
  if _ce_have podman-compose && _ce_have podman; then
    _ce_log 'probing: podman-compose'
    if _ce_silent podman-compose version && _ce_engine_alive podman; then
      printf '%s|%s|%s' podman "$(_ce_engine_product podman)" 'podman-compose'
      return 0
    fi
  fi

  # 6) nerdctl compose (Rancher Desktop containerd / Lima)
  if _ce_have nerdctl; then
    _ce_try 'nerdctl compose' nerdctl nerdctl compose && return 0
  fi

  # 7) finch compose (AWS Finch on macOS)
  if _ce_have finch; then
    _ce_try 'finch compose' finch finch compose && return 0
  fi

  return 1
}

# ---------- module load -----------------------------------------------------

if _ce_payload="$(detect_compose)"; then
  # Parse the "<engine>|<product>|<compose>" payload. Use parameter expansion
  # so we stay portable to dash / busybox sh.
  CONTAINER_ENGINE="${_ce_payload%%|*}"
  _ce_rest="${_ce_payload#*|}"
  CONTAINER_PRODUCT="${_ce_rest%%|*}"
  COMPOSE="${_ce_rest#*|}"
  unset _ce_payload _ce_rest
  export COMPOSE CONTAINER_ENGINE CONTAINER_PRODUCT

  if [ "${COMPOSE_QUIET:-0}" != "1" ]; then
    printf '[container-runtime] engine : %s\n' "$CONTAINER_PRODUCT"
    printf '[container-runtime] compose: %s\n' "$COMPOSE"
  fi
else
  cat >&2 <<'EOF'
[container-runtime] ERROR — no usable container engine found.

Checked (in order, requires both the compose binary AND a reachable daemon):

  • docker compose      (Docker Desktop / Rancher Desktop dockerd / OrbStack /
                         Colima docker runtime / Docker CE)
  • docker-compose      (Docker CE v1 legacy)
  • podman compose      (Podman 4+)
  • podman-compose      (Python wrapper for Podman)
  • nerdctl compose     (Rancher Desktop containerd backend / Lima nerdctl)
  • finch compose       (AWS Finch)

Fixes:
  • Start your container engine (e.g. open Docker Desktop / Rancher Desktop,
    or run `colima start`, `podman machine start`, `finch vm start`, …).
  • Or force a specific compose command:
        COMPOSE_CMD='docker compose' ./tool_docker-init.sh
  • Re-run with COMPOSE_VERBOSE=1 to see which probe failed.

EOF
  return 1 2>/dev/null || exit 1
fi


