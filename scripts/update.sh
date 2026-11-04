#!/usr/bin/env bash
# Move to the newest upstream version.   ./scripts/update.sh          (follow UPSTREAM_TRACK)
#                                        ./scripts/update.sh <tag|sha> (pin something specific)
# Prints "changed" / "unchanged" on the last line (used by the CI workflow). Exit 2 = a patch no longer applies.
source "$(dirname "$0")/lib.sh"
target="${1:-}"
if [ -z "$target" ]; then
  if [ "$UPSTREAM_TRACK" = "master" ]; then
    target=$(git ls-remote "$UPSTREAM_REPO" refs/heads/master | cut -f1)
  else
    # newest tag that looks like a version number, e.g. 4.22.17
    tag=$(git ls-remote --tags --refs "$UPSTREAM_REPO" | cut -f2 | sed 's#refs/tags/##' \
          | grep -E '^[0-9]+(\.[0-9]+)+$' | sort -V | tail -1)
    target=$(git ls-remote "$UPSTREAM_REPO" "refs/tags/$tag" | cut -f1)
    echo "Latest upstream release tag: $tag"
  fi
else
  # allow tags/branches: resolve to a sha
  resolved=$(git ls-remote "$UPSTREAM_REPO" "$target" "refs/tags/$target" "refs/heads/$target" | head -1 | cut -f1 || true)
  target="${resolved:-$target}"
fi
if [ "$target" = "$UPSTREAM_REF" ]; then echo "Already on $UPSTREAM_REF"; echo unchanged; exit 0; fi
echo "Updating $UPSTREAM_REF -> $target"
sed -i "s/^UPSTREAM_REF=.*/UPSTREAM_REF=$target/" "$ROOT/UPSTREAM_REF"
UPSTREAM_REF=$target
checkout_upstream
if ! apply_patches; then echo "Patches need attention (see above)." >&2; exit 2; fi
echo changed
