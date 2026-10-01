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
#   • COMPOSE_CMD              — force the compose command, e.g. 'docker compose'
#                                 or 'podman compose --in-pod=false'.
#   • CONTAINER_ENGINE_CMD     — force the underlying engine binary (docker,
#                                 podman, nerdctl, finch). Used only as a hint
#                                 when COMPOSE_CMD is not set.
#   • DOCKER_HOST              — honoured as-is (Colima / Lima / SSH remote daemons).
#   • COMPOSE_VERBOSE=1        — print discovery trace to stderr.
#   • COMPOSE_QUIET=1          — suppress the resolved-runtime banner on stdout.
#   • COMPOSE_NO_AUTOSTART=1   — don't try to launch the GUI/CLI engine when
#                                 its daemon is offline. Default: enabled
#                                 (we WILL try to start Docker Desktop /
#                                 Rancher Desktop / OrbStack / Colima /
#                                 Podman machine / Finch VM when needed).
#   • COMPOSE_AUTOSTART_TIMEOUT — max seconds to wait for the daemon to come
#                                 up (default: 90).
#
# Self-healing applied automatically before any probe:
#   • $PATH augmented with the install dirs of every supported engine
#     (Homebrew on both Intel and Apple Silicon, Rancher Desktop's
#      ~/.rd/bin, Docker Desktop's ~/.docker/bin, /Applications/*.app
#      bundles, …). Existing entries take precedence.
#   • $DOCKER_HOST / $CONTAINER_HOST auto-set when a known engine socket
#     exists under $HOME (Docker Desktop, Rancher Desktop, Colima,
#     OrbStack, rootless Podman, …).
#   • Engine GUI/CLI auto-started when installed but offline (unless
#     COMPOSE_NO_AUTOSTART=1).
#
# Detection order (first match wins). Every probe also requires that the
# matching daemon answers `<engine> info` (auto-starting it as a side
# effect when possible), so we don't hand back a compose binary that has
# nothing to talk to:
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

# Augment $PATH with the install locations of every container engine we
# support, so the script works out-of-the-box from a minimal shell (IDE
# run-configurations, cron, sudo, env -i, …). Existing PATH entries take
# precedence; we only APPEND.
_ce_bootstrap_path() {
  _extra=''
  # macOS GUI app bundles
  _extra="$_extra:/Applications/Docker.app/Contents/Resources/bin"
  _extra="$_extra:/Applications/Rancher Desktop.app/Contents/Resources/resources/darwin/bin"
  _extra="$_extra:/Applications/OrbStack.app/Contents/MacOS/xbin"
  _extra="$_extra:/Applications/OrbStack.app/Contents/MacOS/bin"
  _extra="$_extra:/Applications/podman-desktop.app/Contents/MacOS"
  _extra="$_extra:/Applications/Finch.app/Contents/MacOS"
  # Package managers (Homebrew on Apple-Silicon then Intel) + system paths
  _extra="$_extra:/opt/homebrew/bin:/opt/homebrew/sbin"
  _extra="$_extra:/usr/local/bin:/usr/local/sbin"
  _extra="$_extra:/opt/podman/bin:/opt/finch/bin:/opt/colima/bin"
  # Per-user installs (Rancher Desktop, Docker Desktop "no-admin" mode,
  # Colima, SDKMAN-style local installs)
  _extra="$_extra:$HOME/.rd/bin"
  _extra="$_extra:$HOME/.docker/bin"
  _extra="$_extra:$HOME/.colima/bin"
  _extra="$_extra:$HOME/bin"
  _extra="$_extra:$HOME/.local/bin"
  # XDG-style finch / podman per-user installs on Linux
  _extra="$_extra:$HOME/.finch/bin"

  for _dir in $(printf '%s' "$_extra" | tr ':' '\n'); do
    [ -d "$_dir" ] || continue
    case ":$PATH:" in
      *":$_dir:"*) ;;                 # already present
      *) PATH="$PATH:$_dir" ;;
    esac
  done
  export PATH
  _ce_log "PATH after bootstrap: $PATH"
}

# Try to discover a usable container socket when no env var points to one.
# Sets DOCKER_HOST / CONTAINER_HOST as needed so subsequent `docker info`
# / `podman info` succeed even from a stripped-down shell.
_ce_bootstrap_socket() {
  # Docker family
  if [ -z "${DOCKER_HOST:-}" ]; then
    # Order matters — first match wins. Most are unix:// sockets created
    # by the GUI app of the day.
    for _sock in \
      "$HOME/.docker/run/docker.sock"          `# Docker Desktop 4.x macOS` \
      "$HOME/.rd/docker.sock"                  `# Rancher Desktop dockerd backend` \
      "$HOME/.colima/default/docker.sock"      `# Colima default profile` \
      "$HOME/.colima/docker.sock"              `# Colima legacy` \
      "$HOME/.orbstack/run/docker.sock"        `# OrbStack` \
      "/var/run/docker.sock"                   `# Linux + Docker Desktop fallback` \
      ; do
      if [ -S "$_sock" ]; then
        DOCKER_HOST="unix://$_sock"
        export DOCKER_HOST
        _ce_log "DOCKER_HOST auto-set to $DOCKER_HOST"
        break
      fi
    done
  fi

  # Podman rootless socket
  if [ -z "${CONTAINER_HOST:-}" ]; then
    _uid="$(id -u 2>/dev/null || echo)"
    for _sock in \
      "${XDG_RUNTIME_DIR:-}/podman/podman.sock" \
      "/run/user/${_uid:-0}/podman/podman.sock" \
      "$HOME/.local/share/containers/podman/machine/podman.sock" \
      ; do
      # Skip empty paths (e.g. XDG_RUNTIME_DIR unset under `env -i`).
      case "$_sock" in /podman/*|*"//"*) continue ;; esac
      [ -n "$_sock" ] || continue
      if [ -S "$_sock" ]; then
        CONTAINER_HOST="unix://$_sock"
        export CONTAINER_HOST
        _ce_log "CONTAINER_HOST auto-set to $CONTAINER_HOST"
        break
      fi
    done
  fi
}

# Run the two bootstrap passes BEFORE any probe.
_ce_bootstrap_path
_ce_bootstrap_socket

# Is the given engine's daemon actually reachable ?
_ce_engine_alive() {
  _ce_silent "$@" info
}

# Best-effort: start the GUI/CLI daemon for the given engine if it's
# installed but currently offline. Returns 0 as soon as `<engine> info`
# succeeds, 1 on timeout.
#
# Skipped when COMPOSE_NO_AUTOSTART=1 (CI, headless servers, …).
# Honoured: COMPOSE_AUTOSTART_TIMEOUT (default: 90 seconds).
_ce_try_start() {
  _engine="$1"
  if [ "${COMPOSE_NO_AUTOSTART:-0}" = "1" ]; then
    _ce_log "auto-start disabled (COMPOSE_NO_AUTOSTART=1) for $_engine"
    return 1
  fi
  _ce_have "$_engine" || return 1
  _ce_engine_alive "$_engine" && return 0   # already up

  _action=''
  _uname="$(uname -s 2>/dev/null || echo)"

  case "$_engine" in
    docker)
      # Try GUI apps first (macOS), then colima as a CLI fallback.
      if [ "$_uname" = "Darwin" ]; then
        if [ -d "/Applications/Docker.app" ]; then
          _action='open -ga Docker'
        elif [ -d "/Applications/Rancher Desktop.app" ]; then
          _action='open -ga "Rancher Desktop"'
        elif [ -d "/Applications/OrbStack.app" ]; then
          _action='open -ga OrbStack'
        elif _ce_have colima; then
          _action='colima start --runtime docker'
        fi
      else
        # Linux: most distributions need root to start dockerd; we leave
        # that to the user and only try the user-space alternatives.
        if _ce_have colima; then _action='colima start --runtime docker'; fi
      fi
      ;;
    podman)
      if _ce_have podman; then _action='podman machine start'; fi
      ;;
    nerdctl)
      if [ "$_uname" = "Darwin" ] && [ -d "/Applications/Rancher Desktop.app" ]; then
        _action='open -ga "Rancher Desktop"'
      elif _ce_have limactl; then
        _action='limactl start default'
      fi
      ;;
    finch)
      if _ce_have finch; then _action='finch vm start'; fi
      ;;
  esac

  if [ -z "$_action" ]; then
    _ce_log "no auto-start strategy known for $_engine"
    return 1
  fi

  printf '[container-runtime] %s daemon is offline — auto-starting via: %s\n' \
      "$_engine" "$_action" >&2

  # Run the start command in the background so the script can begin polling.
  # We deliberately don't capture stdout — GUI launchers print nothing useful,
  # CLI ones (colima/podman/finch) print progress to stderr which we keep.
  sh -c "$_action" >/dev/null 2>&1 &

  _timeout="${COMPOSE_AUTOSTART_TIMEOUT:-90}"
  _elapsed=0
  while [ "$_elapsed" -lt "$_timeout" ]; do
    if _ce_engine_alive "$_engine"; then
      printf '[container-runtime] %s daemon ready after %ss\n' "$_engine" "$_elapsed" >&2
      return 0
    fi
    sleep 2
    _elapsed=$((_elapsed + 2))
  done

  printf '[container-runtime] %s daemon did not come up within %ss — giving up\n' \
      "$_engine" "$_timeout" >&2
  return 1
}

# Combined: command available AND daemon alive (with at most one auto-start).
_ce_engine_usable() {
  _ce_have "$1" || return 1
  _ce_engine_alive "$1" && return 0
  _ce_try_start "$1"
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
  # We need the compose CLI to answer `version` AND the underlying daemon
  # to be usable (auto-starting it as a side effect when possible).
  if _ce_silent "$@" version && _ce_engine_usable "$_engine"; then
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
    if _ce_silent docker-compose version && _ce_engine_usable docker; then
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
    if _ce_silent podman-compose version && _ce_engine_usable podman; then
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


