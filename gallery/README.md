# Tempad renderer gallery

This generated 12-cell gallery covers the selected block-entity render paths:

- default orange, red, and cyan anchor colors in both vertical orientations;
- base and Time Twister Tempads at empty, half, and full charge;
- an empty workstation and all four workstation facings;
- stock timedoor projector and metronome controls.

Each workstation includes its stock terminal block. The controller NBT uses
Tempad 3.0.4's persisted `Inventory` compound and slot-zero item components.

```bash
python gallery/generate.py
python gallery/generate.py --check
python gallery/lint.py
bash gallery/package.sh /tmp/tempad-gallery.zip
```

Generation stays deterministic and bounded. The data pack contains no copied
Tempad assets or captured meshes.
