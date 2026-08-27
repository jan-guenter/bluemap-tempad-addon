/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.tempad.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.tempad.activation.AddonRuntime;

/** Resource extension factory installed before resource-pack construction. */
final class ProfileResourceExtensionType
        implements ResourcePack.Extension<ProfileResourceExtension> {

    private static final Key KEY = Key.parse("bluemap_tempad:exact_profile");
    private final BlockRendererType renderer;
    private final AddonRuntime runtime;

    ProfileResourceExtensionType(
            BlockRendererType renderer,
            AddonRuntime runtime
    ) {
        this.renderer = renderer;
        this.runtime = runtime;
    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public ProfileResourceExtension create(ResourcePack pack) {
        return new ProfileResourceExtension(pack, renderer, runtime);
    }
}
