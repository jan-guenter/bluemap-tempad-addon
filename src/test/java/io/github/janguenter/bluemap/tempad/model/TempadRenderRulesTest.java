/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.tempad.model;

import io.github.janguenter.bluemap.tempad.model.TempadRenderRules.MarkerColor;
import io.github.janguenter.bluemap.tempad.model.TempadRenderRules.WorkstationItem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TempadRenderRulesTest {

    @Test
    void markerColorUsesExactDefaultsNamesAndEncodedRgb() {
        assertEquals(
                new MarkerColor(0xFF6A00, true),
                TempadRenderRules.markerColor(null)
        );
        assertEquals(
                new MarkerColor(0xFF0000, true),
                TempadRenderRules.markerColor(Map.of("tempad:color", "red"))
        );
        assertEquals(
                new MarkerColor(0x00FFFF, true),
                TempadRenderRules.markerColor(Map.of("tempad:color", "cyan"))
        );
        assertEquals(
                new MarkerColor(0xB0E0E6, true),
                TempadRenderRules.markerColor(Map.of("tempad:color", "powderblue"))
        );
        assertEquals(
                new MarkerColor(0xFFA500, true),
                TempadRenderRules.markerColor(Map.of("tempad:color", "orange"))
        );
        assertEquals(
                new MarkerColor(0xFF6A00, true),
                TempadRenderRules.markerColor(
                        Map.of("tempad:color", "#ffff6a00")
                )
        );
        assertEquals(
                new MarkerColor(0x12ABEF, true),
                TempadRenderRules.markerColor(Map.of("tempad:color", "#12abef"))
        );
    }

    @Test
    void malformedMarkerColorRequestsStockFallback() {
        assertFalse(TempadRenderRules.markerColor("red").valid());
        assertFalse(TempadRenderRules.markerColor(
                Map.of("tempad:color", "not-a-color")
        ).valid());
        assertFalse(TempadRenderRules.markerColor(
                Map.of("tempad:color", 0xFF0000)
        ).valid());
    }

    @Test
    void baseTempadUsesFiveCeilingSelectedChargeStages() {
        int[] contents = {0, 1_500, 3_000, 4_500, 6_000};
        for (int stage = 0; stage < contents.length; stage++) {
            WorkstationItem item = item(false, contents[stage], 0);
            assertTrue(item.valid());
            assertTrue(item.present());
            assertFalse(item.attached());
            assertEquals(stage, item.chargeStage());
        }
        assertEquals(1, item(false, 1, 0).chargeStage());
        assertEquals(1, item(false, 1_500, 0).chargeStage());
        assertEquals(2, item(false, 1_501, 0).chargeStage());
        assertEquals(3, item(false, 3_001, 0).chargeStage());
    }

    @Test
    void attachedTempadUsesCombinedNineThousandCapacity() {
        assertEquals(1, item(true, 2_250, 0).chargeStage());
        assertEquals(2, item(true, 2_251, 0).chargeStage());
        assertEquals(2, item(true, 4_500, 0).chargeStage());
        assertEquals(3, item(true, 6_000, 750).chargeStage());
        WorkstationItem full = item(true, 6_000, 3_000);
        assertTrue(full.attached());
        assertEquals(4, full.chargeStage());
    }

    @Test
    void emptyAndMalformedInventoriesStayBounded() {
        WorkstationItem empty = TempadRenderRules.workstationItem(
                Map.of("Size", 1, "Items", List.of())
        );
        assertTrue(empty.valid());
        assertFalse(empty.present());
        assertFalse(TempadRenderRules.workstationItem("bad").valid());
        assertFalse(item(false, 6_001, 0).valid());
        assertFalse(item(false, 6_000, 1).valid());
        assertFalse(item(true, 6_000, 3_001).valid());
    }

    @Test
    void workstationFacingMatchesClientPoseRotation() {
        assertEquals(0, TempadRenderRules.workstationFacingDegrees("east"));
        assertEquals(90, TempadRenderRules.workstationFacingDegrees("south"));
        assertEquals(180, TempadRenderRules.workstationFacingDegrees("west"));
        assertEquals(270, TempadRenderRules.workstationFacingDegrees("north"));
        assertEquals(-1, TempadRenderRules.workstationFacingDegrees("up"));
    }

    private static WorkstationItem item(
            boolean attached,
            int base,
            int twister
    ) {
        return TempadRenderRules.workstationItem(Map.of(
                "Size", 1,
                "Items", List.of(Map.of(
                        "Slot", 0,
                        "id", "tempad:tempad",
                        "count", 1,
                        "components", Map.of(
                                "tempad:twister_equipped", attached,
                                "tempad:chronon_content_tempad", base,
                                "tempad:chronon_content_time_twister", twister
                        )
                ))
        ));
    }
}
