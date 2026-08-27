/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.tempad.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import io.github.janguenter.bluemap.tempad.activation.AddonRuntime;

import java.util.List;

/** BlueMap 5.22 registration boundary. Family renderer registrations go here. */
public final class BlueMap522Adapter {

    private static final AddonRuntime RUNTIME = AddonRuntime.INSTANCE;
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            Key.parse("bluemap_tempad:exact_renderer"),
            BlueMap522Adapter::createRenderer
    );
    private static final ResourcePack.Extension<ProfileResourceExtension> EXTENSION =
            new ProfileResourceExtensionType(RENDERER, RUNTIME);
    private static final List<BlockEntityType> BLOCK_ENTITIES = List.of(
            blockEntity("timedoor_marker"), blockEntity("chronomark"),
            blockEntity("workstation")
    );

    private BlueMap522Adapter() {
    }

    /** Registers the exact profile, bounded renderer and NBT projection. */
    public static synchronized boolean install() {
        if (!RegistryGuard.canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)
                || BLOCK_ENTITIES.stream().anyMatch(type ->
                !RegistryGuard.canRegister(BlockEntityType.REGISTRY, type))) {
            RUNTIME.fail("registry-collision");
            return false;
        }
        if (!RegistryGuard.register(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.register(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.fail("registry-registration-failed");
            return false;
        }
        for (BlockEntityType type : BLOCK_ENTITIES) {
            if (!RegistryGuard.register(BlockEntityType.REGISTRY, type)) {
                RUNTIME.fail("block-entity-registration-failed");
                return false;
            }
        }
        return true;
    }

    private static BlockEntityType blockEntity(String path) {
        return new BlockEntityType.Impl(
                Key.parse("tempad:" + path), TempadBlockEntityData.class
        );
    }

    private static BlockRenderer createRenderer(
            ResourcePack pack,
            TextureGallery textures,
            RenderSettings settings
    ) {
        try {
            return new TempadRenderer(pack, textures, settings, RUNTIME);
        } catch (RuntimeException exception) {
            RUNTIME.inactive("renderer-construction-"
                    + exception.getClass().getSimpleName());
            return BlockRendererType.DEFAULT.create(pack, textures, settings);
        }
    }
}
