#!/usr/bin/env bash
# Full local build: ./scripts/build.sh  ->  dist/ (static site; open via `python3 -m http.server -d dist`)
source "$(dirname "$0")/lib.sh"
bash "$ROOT/scripts/prepare.sh"
cd "$UPSTREAM_DIR"
export UNCIV_WEB_MODULE_DIR="$ROOT/web"
# Unciv's own gradle.properties caps the Gradle JVM at 4 GB, far too small for the TeaVM compile, which runs inside it
# (outOfProcess is off in web/build.gradle.kts). --stacktrace so a failure shows *where* TeaVM gave up.
./gradlew --no-daemon --stacktrace \
  -Dorg.gradle.jvmargs="-Xmx12g -XX:MaxMetaspaceSize=1g" \
  -I "$ROOT/gradle/web-init.gradle.kts" :web:gdx_teavm_web_js_build "$@"
rm -rf "$ROOT/dist" && mkdir -p "$ROOT/dist"
cp -r "$ROOT/web/build/dist/js/webapp/." "$ROOT/dist/"
touch "$ROOT/dist/.nojekyll"
echo "Built -> $ROOT/dist"
