#!/usr/bin/env bash
# Shared helpers. Source this, don't run it.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
UPSTREAM_DIR="${UPSTREAM_DIR:-$ROOT/.upstream}"
# shellcheck disable=SC1091
source "$ROOT/UPSTREAM_REF"

# Fetch $UPSTREAM_REF into $UPSTREAM_DIR (clean checkout, no local edits survive).
checkout_upstream() {
  if [ ! -d "$UPSTREAM_DIR/.git" ]; then
    git init -q "$UPSTREAM_DIR"
    git -C "$UPSTREAM_DIR" remote add origin "$UPSTREAM_REPO"
  fi
  git -C "$UPSTREAM_DIR" fetch -q --depth=1 origin "$UPSTREAM_REF"
  git -C "$UPSTREAM_DIR" checkout -q --force FETCH_HEAD
  git -C "$UPSTREAM_DIR" clean -qfdx
}

# Apply every patches/*.patch (sorted) onto the upstream checkout. Fails loudly on conflict.
apply_patches() {
  shopt -s nullglob
  local failed=0
  for p in "$ROOT"/patches/*.patch; do
    if git -C "$UPSTREAM_DIR" apply --3way --whitespace=nowarn "$p" 2>/dev/null; then
      echo "  patch ok:     $(basename "$p")"
    else
      echo "  patch FAILED: $(basename "$p")  (needs to be rebased onto the new upstream)" >&2
      failed=1
    fi
  done
  return $failed
}
