/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.tempad.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.tempad.activation.AddonRuntime;
import io.github.janguenter.bluemap.tempad.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.tempad.profile.Tempad304Profile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/** Exact-artifact admission, installed-resource validation and target routing. */
final class ProfileResourceExtension implements ResourcePackExtension {

    private static final Set<Key> REQUIRED_TEXTURES = requiredTextures();

    private final ResourcePack resourcePack;
    private final BlockRendererType renderer;
    private final AddonRuntime runtime;
    private boolean admitted;

    ProfileResourceExtension(
            ResourcePack resourcePack,
            BlockRendererType renderer,
            AddonRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.renderer = renderer;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) {
        admitted = false;
        if (Boolean.getBoolean("bluemap.tempad.disabled")) {
            runtime.inactive("operator-disabled");
            return;
        }
        if (!ExactArtifactDetector.matchesAll(roots, Tempad304Profile.ARTIFACTS)) {
            runtime.inactive("exact-artifact-missing-or-duplicate");
            return;
        }
        admitted = true;
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        return admitted ? REQUIRED_TEXTURES : Set.of();
    }

    @Override
    public void bake() {
        if (!admitted) {
            return;
        }
        try {
            for (Key texture : REQUIRED_TEXTURES) {
                if (!validTexture(texture)) {
                    runtime.inactive("installed-render-resource-invalid");
                    return;
                }
            }
            VariantRendererCatalog variants = VariantRendererCatalog.wrap(
                    resourcePack, renderer
            );
            RendererDataRegistry.install(resourcePack, variants);
            runtime.activate();
            System.out.println("BlueMap Tempad add-on active: wrapped "
                    + variants.size() + " exact variants across 3 blocks.");
        } catch (IOException | RuntimeException exception) {
            runtime.inactive("route-install-" + exception.getClass().getSimpleName());
        }
    }

    private boolean validTexture(Key key) throws IOException {
        Texture texture = resourcePack.getTextures().get(key);
        if (texture == null) {
            return false;
        }
        BufferedImage image = texture.getTextureImage();
        return image != null && image.getWidth() == 16 && image.getHeight() == 16;
    }

    private static Set<Key> requiredTextures() {
        LinkedHashSet<Key> keys = new LinkedHashSet<>();
        keys.add(WorkstationItemEmitter.baseTexture(false));
        keys.add(WorkstationItemEmitter.baseTexture(true));
        for (int stage = 0; stage <= 4; stage++) {
            keys.add(WorkstationItemEmitter.chargeTexture(stage));
        }
        return Set.copyOf(keys);
    }
}
