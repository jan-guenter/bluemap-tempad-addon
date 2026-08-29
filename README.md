# BlueMap Tempad Add-on

A Java 21 BlueMap add-on for the exact `tempad-3.0.4-mc1.21.1` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Status: owner-accepted `0.1.0-alpha.1` release candidate. The exact profile
rerenders `timedoor_marker` and `chronomark` with their persisted attachment
color, then projects a persisted slot-zero Tempad above `workstation` with the
client's fixed-item pose and charge-stage textures.

## Build

Clone with `--recurse-submodules`, or initialize an existing checkout with
`git submodule update --init --recursive -- tooling/bluemap-addon-toolkit`.
The settings preflight accepts only the committed toolkit gitlink at commit
`6cd34a8368cc4ee8628fbe830a90ec5b14960629` and rejects an uninitialized,
changed, or dirty toolkit checkout.

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport \
  -PtempadJar=/path/to/tempad-1.21.1-3.0.4-all.jar \
  clean prototypeCheck build
```

`check` is the quick Java, checkstyle, and archive gate. `prototypeCheck` also
verifies the exact candidate identity and generated 12-cell gallery. See
`provenance/upstreams.json` for immutable artifact identities and the
[execution guide](docs/EXECUTION.md) for the prototype-to-release loop.

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
