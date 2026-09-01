/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.tempad.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;

import java.util.IdentityHashMap;
import java.util.Map;

/** Classloader-local wrapped-variant data keyed by one resource pack. */
final class RendererDataRegistry {

    private static final Map<ResourcePack, VariantRendererCatalog> DATA =
            new IdentityHashMap<>();

    private RendererDataRegistry() {
    }

    static synchronized void install(ResourcePack pack, VariantRendererCatalog catalog) {
        DATA.put(pack, catalog);
    }

    static synchronized VariantRendererCatalog get(ResourcePack pack) {
        return DATA.get(pack);
    }
}
