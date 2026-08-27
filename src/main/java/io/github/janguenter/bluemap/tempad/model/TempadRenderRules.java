/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.tempad.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Exact Tempad 3.0.4 static renderer rules and bounded NBT decoding. */
public final class TempadRenderRules {

    public static final int DEFAULT_MARKER_COLOR = 0xFF6A00;
    public static final int TEMPAD_CAPACITY = 6_000;
    public static final int TWISTER_CAPACITY = 3_000;
    public static final Set<String> TARGETS = Set.of(
            "tempad:timedoor_marker", "tempad:chronomark", "tempad:workstation"
    );
    private static final Map<String, Integer> NAMED_COLORS = Map.ofEntries(
            Map.entry("red", 0xFF0000), Map.entry("green", 0x008000),
            Map.entry("blue", 0x0000FF), Map.entry("yellow", 0xFFFF00),
            Map.entry("black", 0x000000), Map.entry("brown", 0xA52A2A),
            Map.entry("cyan", 0x00FFFF), Map.entry("gray", 0x808080),
            Map.entry("powderblue", 0xB0E0E6),
            Map.entry("lightgray", 0xD3D3D3), Map.entry("lime", 0x00FF00),
            Map.entry("magenta", 0xFF00FF), Map.entry("orange", 0xFFA500),
            Map.entry("pink", 0xFFC0CB), Map.entry("purple", 0x800080),
            Map.entry("white", 0xFFFFFF)
    );

    private TempadRenderRules() {
    }

    public static MarkerColor markerColor(Object rawAttachments) {
        if (rawAttachments == null) {
            return new MarkerColor(DEFAULT_MARKER_COLOR, true);
        }
        if (!(rawAttachments instanceof Map<?, ?> attachments)) {
            return new MarkerColor(DEFAULT_MARKER_COLOR, false);
        }
        Object rawColor = attachments.get("tempad:color");
        if (rawColor == null) {
            return new MarkerColor(DEFAULT_MARKER_COLOR, true);
        }
        if (!(rawColor instanceof String encoded)) {
            return new MarkerColor(DEFAULT_MARKER_COLOR, false);
        }
        Integer named = NAMED_COLORS.get(encoded.toLowerCase(java.util.Locale.ROOT));
        if (named != null) {
            return new MarkerColor(named, true);
        }
        if (encoded.matches("#[0-9a-fA-F]{6}")) {
            return new MarkerColor(Integer.parseInt(encoded.substring(1), 16), true);
        }
        if (encoded.matches("#[0-9a-fA-F]{8}")) {
            return new MarkerColor(Integer.parseInt(encoded.substring(3), 16), true);
        }
        return new MarkerColor(DEFAULT_MARKER_COLOR, false);
    }

    public static WorkstationItem workstationItem(Object rawInventory) {
        if (rawInventory == null) {
            return WorkstationItem.empty();
        }
        if (!(rawInventory instanceof Map<?, ?> inventory)) {
            return WorkstationItem.malformed();
        }
        Object rawItems = inventory.get("Items");
        if (!(rawItems instanceof List<?> items)) {
            return rawItems == null
                    ? WorkstationItem.empty() : WorkstationItem.malformed();
        }
        for (Object rawEntry : items) {
            if (!(rawEntry instanceof Map<?, ?> entry)) {
                return WorkstationItem.malformed();
            }
            Integer slot = integer(entry.get("Slot"));
            if (slot == null || slot != 0) {
                continue;
            }
            return decodeTempad(entry);
        }
        return WorkstationItem.empty();
    }

    public static int workstationFacingDegrees(String facing) {
        return switch (facing) {
            case "east" -> 0;
            case "south" -> 90;
            case "west" -> 180;
            case "north" -> 270;
            default -> -1;
        };
    }

    private static WorkstationItem decodeTempad(Map<?, ?> entry) {
        Object rawId = entry.get("id");
        Integer count = integer(entry.containsKey("count")
                ? entry.get("count") : entry.get("Count"));
        if (!"tempad:tempad".equals(rawId) || count == null || count != 1) {
            return WorkstationItem.malformed();
        }
        Object rawComponents = entry.get("components");
        if (rawComponents != null && !(rawComponents instanceof Map<?, ?>)) {
            return WorkstationItem.malformed();
        }
        Map<?, ?> components = rawComponents instanceof Map<?, ?> found
                ? found : Map.of();
        Boolean attached = bool(components.get("tempad:twister_equipped"));
        Integer base = integerOrZero(components.get("tempad:chronon_content_tempad"));
        Integer twister = integerOrZero(
                components.get("tempad:chronon_content_time_twister")
        );
        if (attached == null || base == null || twister == null
                || base < 0 || base > TEMPAD_CAPACITY
                || twister < 0 || twister > TWISTER_CAPACITY
                || !attached && twister != 0) {
            return WorkstationItem.malformed();
        }
        int maximum = TEMPAD_CAPACITY + (attached ? TWISTER_CAPACITY : 0);
        int content = base + twister;
        if (content > maximum) {
            return WorkstationItem.malformed();
        }
        int stage = content == 0
                ? 0
                : Math.min(4, (content * 4 + maximum - 1) / maximum);
        return new WorkstationItem(true, true, attached, stage);
    }

    private static Integer integerOrZero(Object value) {
        return value == null ? 0 : integer(value);
    }

    private static Integer integer(Object value) {
        if (!(value instanceof Number number)) {
            return null;
        }
        long raw = number.longValue();
        return raw < Integer.MIN_VALUE || raw > Integer.MAX_VALUE
                ? null : (int) raw;
    }

    private static Boolean bool(Object value) {
        if (value == null) {
            return Boolean.FALSE;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            int raw = number.intValue();
            return raw == 0 ? Boolean.FALSE : raw == 1 ? Boolean.TRUE : null;
        }
        return null;
    }

    public record MarkerColor(int rgb, boolean valid) {
    }

    public record WorkstationItem(
            boolean valid,
            boolean present,
            boolean attached,
            int chargeStage
    ) {

        public WorkstationItem {
            if (chargeStage < 0 || chargeStage > 4) {
                throw new IllegalArgumentException("charge stage is outside 0..4");
            }
        }

        static WorkstationItem empty() {
            return new WorkstationItem(true, false, false, 0);
        }

        static WorkstationItem malformed() {
            return new WorkstationItem(false, false, false, 0);
        }
    }
}
