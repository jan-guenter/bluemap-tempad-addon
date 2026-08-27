#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Compact Tempad block-entity renderer gallery cases."""

from __future__ import annotations

from dataclasses import dataclass


NAMESPACE = "tempad_gallery"
ENVELOPE = (170, 99, 170, 189, 103, 183)
CASE_COUNT = 12


@dataclass(frozen=True)
class Placement:
    case_id: str
    label: str
    x: int
    y: int
    z: int
    block_state: str
    expected: str


def marker(
    case_id: str,
    label: str,
    x: int,
    z: int,
    block_id: str,
    up: bool,
    color: str | None,
) -> Placement:
    attachment = (
        ""
        if color is None
        else f'{{"neoforge:attachments":{{"tempad:color":"{color}"}}}}'
    )
    expected = "tinted-default-orange" if color is None else f"tinted-{color}"
    return Placement(
        case_id,
        label,
        x,
        100,
        z,
        f"{block_id}[up={'true' if up else 'false'}]{attachment}",
        expected,
    )


def child_position(x: int, z: int, facing: str) -> tuple[int, int]:
    return {
        "north": (x - 1, z),
        "east": (x, z - 1),
        "south": (x + 1, z),
        "west": (x, z + 1),
    }[facing]


def workstation(
    case_id: str,
    label: str,
    x: int,
    z: int,
    facing: str,
    has_tape: bool,
    base_charge: int | None,
    twister_charge: int = 0,
    attached: bool = False,
) -> tuple[Placement, Placement]:
    nbt = ""
    expected = "stock-empty-workstation"
    if base_charge is not None:
        equipped = "1b" if attached else "0b"
        nbt = (
            "{Inventory:{Size:1,Items:[{Slot:0,id:\"tempad:tempad\",count:1,"
            "components:{\"tempad:twister_equipped\":"
            f"{equipped},\"tempad:chronon_content_tempad\":{base_charge},"
            "\"tempad:chronon_content_time_twister\":"
            f"{twister_charge}}}}}]}}}}"
        )
        expected = (
            "attached-tempad-visible" if attached else "base-tempad-visible"
        )
    controller = Placement(
        case_id,
        label,
        x,
        100,
        z,
        f"tempad:workstation[facing={facing},has_tape="
        f"{'true' if has_tape else 'false'}]{nbt}",
        expected,
    )
    child_x, child_z = child_position(x, z, facing)
    child = Placement(
        case_id,
        f"{label} terminal",
        child_x,
        100,
        child_z,
        f"tempad:workstation_child[facing={facing}]",
        "stock-terminal-visible",
    )
    return controller, child


PLACEMENTS = (
    marker(
        "timedoor-default-up",
        "default orange timedoor marker, up",
        172,
        172,
        "tempad:timedoor_marker",
        True,
        None,
    ),
    marker(
        "timedoor-red-down",
        "red timedoor marker, down",
        176,
        172,
        "tempad:timedoor_marker",
        False,
        "red",
    ),
    marker(
        "chronomark-default-down",
        "default orange chronomark, down",
        180,
        172,
        "tempad:chronomark",
        False,
        None,
    ),
    marker(
        "chronomark-cyan-up",
        "cyan chronomark, up",
        184,
        172,
        "tempad:chronomark",
        True,
        "cyan",
    ),
    *workstation(
        "workstation-base-0", "north base Tempad, empty", 172, 176,
        "north", False, 0
    ),
    *workstation(
        "workstation-base-50", "east base Tempad, half charge", 176, 176,
        "east", False, 3000
    ),
    *workstation(
        "workstation-attached-0", "south attached Tempad, empty", 180, 176,
        "south", True, 0, 0, True
    ),
    *workstation(
        "workstation-attached-100", "west attached Tempad, full charge", 184,
        176, "west", False, 6000, 3000, True
    ),
    *workstation(
        "workstation-empty", "empty workstation", 172, 180,
        "south", False, None
    ),
    Placement(
        "projector-off",
        "projector off without card",
        176,
        100,
        180,
        "tempad:timedoor_projector[facing=south,has_card=false,triggered=false]",
        "stock-projector-off",
    ),
    Placement(
        "projector-on",
        "projector on with card",
        180,
        100,
        180,
        "tempad:timedoor_projector[facing=south,has_card=true,triggered=true]",
        "stock-projector-on",
    ),
    Placement(
        "metronome",
        "metronome stock model",
        184,
        100,
        180,
        "tempad:metronome",
        "stock-metronome",
    ),
)
