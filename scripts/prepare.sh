#!/usr/bin/env bash
# Check out upstream at the pinned ref and apply our patches. Safe to re-run.
source "$(dirname "$0")/lib.sh"
echo "Upstream: $UPSTREAM_REPO @ $UPSTREAM_REF"
checkout_upstream
apply_patches
