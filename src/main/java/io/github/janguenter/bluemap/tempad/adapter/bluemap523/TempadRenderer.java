/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.tempad.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.tempad.activation.AddonRuntime;
import io.github.janguenter.bluemap.tempad.model.TempadRenderRules;
import io.github.janguenter.bluemap.tempad.model.TempadRenderRules.MarkerColor;
import io.github.janguenter.bluemap.tempad.model.TempadRenderRules.WorkstationItem;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Stock-preserving renderer for the two marker BERs and workstation item BER. */
final class TempadRenderer implements BlockRenderer {

    private static final ThreadLocal<Boolean> STOCK_FALLBACK =
            ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final Set<String> DIAGNOSTICS = ConcurrentHashMap.newKeySet();

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;
    private final AddonRuntime runtime;
    private final VariantRendererCatalog variants;
    private final TintedModelEmitter markers;
    private final WorkstationItemEmitter items;
    private final Map<BlockRendererType, BlockRenderer> stockRenderers =
            new IdentityHashMap<>();

    TempadRenderer(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings,
            AddonRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
        this.runtime = runtime;
        variants = RendererDataRegistry.get(resourcePack);
        markers = new TintedModelEmitter(resourcePack, textures, settings);
        items = new WorkstationItemEmitter(resourcePack, textures);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        int start = target.getTileModel().size();
        Color initialColor = new Color().set(mapColor);
        try {
            if (!runtime.active() || variants == null) {
                stock(block, variant, target, mapColor);
                return;
            }
            String id = block.getBlockState().getId().getFormatted();
            if ("tempad:timedoor_marker".equals(id)
                    || "tempad:chronomark".equals(id)) {
                renderMarker(id, block, variant, target, mapColor);
            } else if ("tempad:workstation".equals(id)) {
                renderWorkstation(block, variant, target, mapColor);
            } else {
                stock(block, variant, target, mapColor);
            }
        } catch (MaxCapacityReachedException exception) {
            reset(target, start, mapColor, initialColor);
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            reset(target, start, mapColor, initialColor);
            String id = block.getBlockState().getId().getFormatted();
            diagnose(id, exception.getClass().getSimpleName());
            runtime.inactive("renderer-" + exception.getClass().getSimpleName());
            stockSafely(block, variant, target, mapColor, start, initialColor);
        }
    }

    private void renderMarker(
            String id,
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        TempadBlockEntityData data = block.getBlockEntity()
                instanceof TempadBlockEntityData found ? found : null;
        MarkerColor color = TempadRenderRules.markerColor(
                data == null ? null : data.attachments()
        );
        if (!color.valid()) {
            diagnose(id, "malformed-color-stock-fallback");
            stock(block, variant, target, mapColor);
            return;
        }
        if (!markers.emit(block, variant, target, mapColor, color.rgb())) {
            throw new IllegalStateException("marker model unavailable");
        }
    }

    private void renderWorkstation(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        stock(block, variant, target, mapColor);
        TempadBlockEntityData data = block.getBlockEntity()
                instanceof TempadBlockEntityData found ? found : null;
        WorkstationItem item = TempadRenderRules.workstationItem(
                data == null ? null : data.inventory()
        );
        if (!item.valid()) {
            diagnose("tempad:workstation", "malformed-item-omitted");
            return;
        }
        String facing = block.getBlockState().getProperties().get("facing");
        if (!items.emit(item, facing, block, target)) {
            diagnose("tempad:workstation", "item-resource-omitted");
        }
    }

    private void stock(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        if (STOCK_FALLBACK.get()) {
            return;
        }
        STOCK_FALLBACK.set(Boolean.TRUE);
        try {
            BlockRendererType type = variants == null
                    ? BlockRendererType.DEFAULT : variants.original(variant);
            stockRenderers.computeIfAbsent(
                    type,
                    found -> found.create(resourcePack, textures, settings)
            ).render(block, variant, target, mapColor);
        } finally {
            STOCK_FALLBACK.set(Boolean.FALSE);
        }
    }

    private void stockSafely(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor,
            int start,
            Color initialColor
    ) {
        try {
            stock(block, variant, target, mapColor);
        } catch (RuntimeException | LinkageError exception) {
            reset(target, start, mapColor, initialColor);
            runtime.inactive("stock-fallback-" + exception.getClass().getSimpleName());
        }
    }

    private static void reset(
            TileModelView target,
            int start,
            Color mapColor,
            Color initialColor
    ) {
        target.getTileModel().reset(start);
        target.initialize(start);
        mapColor.set(initialColor);
    }

    private static void diagnose(String id, String outcome) {
        String key = id + ':' + outcome;
        if (DIAGNOSTICS.add(key)) {
            System.out.println("BlueMap Tempad diagnostic: "
                    + id + " -> " + outcome);
        }
    }
}
