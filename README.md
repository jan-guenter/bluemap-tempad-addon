# BlueMap Tempad Add-on

A Java 21 BlueMap add-on for the exact `tempad-3.0.4-mc1.21.1` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Version `0.1.0-alpha.2` is the owner-accepted native BlueMap 5.23 release
candidate. It preserves the owner-accepted `0.1.0-alpha.1` contract. The exact profile
rerenders `timedoor_marker` and `chronomark` with their persisted attachment
color, then projects a persisted slot-zero Tempad above `workstation` with the
client's fixed-item pose and charge-stage textures.

## Build

Clone with `--recurse-submodules`, or initialize an existing checkout with
`git submodule update --init --recursive -- tooling/bluemap-addon-toolkit
modules/bluemap-addon-adapter-api`.
The settings preflight rejects either uninitialized, changed, or dirty exact
gitlink.

```bash
gradle --no-daemon -PbluemapSourcePath=/path/to/BlueMap-at-7e07f4e7 \
  -PtempadJar=/path/to/tempad-1.21.1-3.0.4-all.jar \
  -PreleaseTag=v0.1.0-alpha.2 clean prototypeCheck build \
  generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyReleaseCandidate
```

`check` is the quick Java, checkstyle, and archive gate. `prototypeCheck` also
verifies the exact candidate identity and generated 12-cell gallery. See
`provenance/upstreams.json` for immutable artifact identities and the
[execution guide](docs/EXECUTION.md) for the prototype-to-release loop.

The add-on compiles the four helpers from the exact Adapter API source-module
gitlink. Its standalone JAR is neither installed nor nested.

## Install

Place the candidate JAR in BlueMap's add-on pack directory and restart the
BlueMap JVM. Removal plus one restart restores stock behavior; the add-on
creates no custom world state.

Set `-Dbluemap.tempad.disabled=true` to leave the exact profile inactive.

## Scope boundary

Projector, workstation shell and child, metronome, unsupported inventory
contents, particles, and animation stay stock. Missing or malformed exact
renderer data takes the stock path. The default missing anchor color is
`#ff6a00`; malformed explicit colors do not replace stock output.

No Tempad binary, source, class, asset, captured mesh, or gallery is
bundled in the add-on.
