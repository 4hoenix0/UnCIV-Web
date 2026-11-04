# Unciv on GitHub Pages

Builds [yairm210/Unciv](https://github.com/yairm210/Unciv) for the browser with
[gdx-teavm](https://github.com/xpenatan/gdx-teavm) (compiles the JVM game to JavaScript) and publishes it to GitHub Pages.

> **Status: scaffold, not yet a working build.** Everything here was written without being able to compile
> (the authoring sandbox can't reach Maven Central). Expect the first CI runs to fail with compile errors that you
> fix one by one - see *Known blockers* below. The repo layout and the update machinery are the finished part.

## How it stays easy to update

This repo contains **no copy of Unciv**. It pins an upstream commit in `UPSTREAM_REF` and, at build time:

1. checks out that exact commit (`scripts/prepare.sh`),
2. applies our small patches from `patches/` (only for code that can't run in a browser),
3. bolts the `web/` module on through a Gradle init script (`gradle/web-init.gradle.kts`) - upstream files are not edited for that,
4. runs the TeaVM build and publishes `dist/`.

| You want to... | Do this |
|---|---|
| Update to the latest Unciv | Nothing: the **Sync upstream Unciv** workflow runs daily and opens a PR. Merge it - the build runs on the PR, merging deploys. Or run it by hand from the Actions tab, or locally: `./scripts/update.sh` |
| Pin a specific version | `./scripts/update.sh 4.22.17` (tag, branch or sha) |
| Follow every upstream commit | set `UPSTREAM_TRACK=master` in `UPSTREAM_REF` |
| Build locally | `./scripts/build.sh`, then `python3 -m http.server -d dist` (needs JDK 21) |

### One-time setup
1. Push this repo to GitHub. 2. *Settings → Pages → Source: GitHub Actions*. 3. Run the **Build** workflow.

### When a patch fails
If upstream edits lines a patch touches, `update.sh` exits with `patch FAILED` and the sync workflow opens an issue.
Fix: `./scripts/prepare.sh` (checks out the old pinned ref), edit `.upstream/` by hand, `git -C .upstream diff > patches/NNNN-name.patch`,
then `./scripts/update.sh`. Keep patches tiny and few - that is what keeps updates painless.

## Known blockers (what's left to make it run)

TeaVM implements only a slice of the Java class library. Findings so far:

- **Threads/concurrency** - `Executors`, `ExecutorService`, `ThreadFactory`, `java.util.concurrent.locks`, and
  `java.lang.management` don't exist in TeaVM. `patches/0001` makes `Concurrency.kt` single-threaded (runs "background" work inline)
  and stubs `getGcCount`. Long operations (map generation, AI turns) will therefore freeze the tab while they run.
- **kotlinx.coroutines** - the JVM artifact uses locks/threads internally; it may not compile under TeaVM at all.
  If so, the fix is replacing the coroutine usage behind `Concurrency` (it's the single seam: `core/src/com/unciv/utils/Concurrency.kt`).
- **Networking (multiplayer, mod downloads)** - ktor's CIO engine is JVM-only. Needs a fetch()-based engine or to be stubbed out in a patch.
- **kotlin-reflect / `java.lang.reflect`** - used in ~170 places in core; TeaVM reflection is opt-in via `web/.../UncivReflectionPolicy.java`.
- **Files** - saves use gdx-teavm's browser storage; "save to file / load from file" (`WebSaverLoader`) is a stub.
- **Fonts** - `WebFont` draws glyphs on a canvas; metrics are approximate.
- **Size** - ~34 MB of assets are fetched on first load.

Workflow for fixing these: push, read the failing TeaVM error in the Actions log, add a small patch (or a class in `web/`), repeat.
