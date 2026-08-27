#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Lint the generated Tempad gallery without starting Minecraft."""

from __future__ import annotations

import json
from pathlib import Path
import re
import sys

sys.dont_write_bytecode = True
import cases
import generate


ROOT = Path(__file__).resolve().parent


def main() -> int:
    for relative, payload in generate.generated_files().items():
        path = ROOT / relative
        if not path.is_file() or path.read_bytes() != payload:
            raise ValueError(f"generated file differs: {relative}")

    json.loads((ROOT / "datapack/pack.mcmeta").read_text(encoding="utf-8"))
    load_tag = json.loads(
        (ROOT / "datapack/data/minecraft/tags/function/load.json").read_text(
            encoding="utf-8"
        )
    )
    if load_tag != {"values": [f"{cases.NAMESPACE}:load"]}:
        raise ValueError("load tag differs from the exact namespace")

    case_ids = {placement.case_id for placement in cases.PLACEMENTS}
    if len(case_ids) != cases.CASE_COUNT:
        raise ValueError("gallery must contain exactly 12 visual cells")
    if len(cases.PLACEMENTS) != 17:
        raise ValueError("gallery must contain 17 block placements")
    blocks = [placement.block_state for placement in cases.PLACEMENTS]
    if sum("tempad:timedoor_marker" in block for block in blocks) != 2:
        raise ValueError("gallery must contain two timedoor markers")
    if sum("tempad:chronomark" in block for block in blocks) != 2:
        raise ValueError("gallery must contain two chronomarks")
    controllers = [block for block in blocks if block.startswith("tempad:workstation[")]
    children = [block for block in blocks if block.startswith("tempad:workstation_child[")]
    if len(controllers) != 5 or len(children) != 5:
        raise ValueError("gallery must contain five complete workstations")
    occupied = [block for block in controllers if "{Inventory:" in block]
    if len(occupied) != 4:
        raise ValueError("gallery must contain four occupied workstations")
    if any("{Inventory:{Size:1,Items:[" not in block for block in occupied):
        raise ValueError("workstation fixture must use nested Inventory NBT")

    minimum_x, minimum_y, minimum_z, maximum_x, maximum_y, maximum_z = (
        cases.ENVELOPE
    )
    for placement in cases.PLACEMENTS:
        if not (
            minimum_x <= placement.x <= maximum_x
            and minimum_y <= placement.y <= maximum_y
            and minimum_z <= placement.z <= maximum_z
        ):
            raise ValueError(f"placement escaped envelope: {placement.case_id}")

    function_root = ROOT / f"datapack/data/{cases.NAMESPACE}/function"
    functions = "\n".join(
        path.read_text(encoding="utf-8")
        for path in sorted(function_root.glob("*.mcfunction"))
    )
    if len(re.findall(r"^setblock ", functions, re.MULTILINE)) != 17:
        raise ValueError("gallery must place exactly 17 blocks")
    lowered = functions.lower()
    for forbidden in ("summon ", "data merge", "op ", "deop ", "stop "):
        if forbidden in lowered:
            raise ValueError(f"forbidden gallery command: {forbidden}")
    print("gallery lint passed: 12 cells, 17 bounded block placements")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, ValueError) as error:
        print(f"gallery lint failed: {error}", file=sys.stderr)
        raise SystemExit(1)
