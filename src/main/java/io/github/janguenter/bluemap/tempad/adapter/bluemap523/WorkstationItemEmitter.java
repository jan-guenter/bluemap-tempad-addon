/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.tempad.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.tempad.model.TempadRenderRules;
import io.github.janguenter.bluemap.tempad.model.TempadRenderRules.WorkstationItem;

/** Projects the persisted Tempad with the exact workstation fixed-item pose. */
final class WorkstationItemEmitter {

    private static final Key BASE = Key.parse("tempad:item/tempad/base");
    private static final Key ATTACHED =
            Key.parse("tempad:item/tempad/base_with_twister");
    private static final Key[] CHARGE = {
            Key.parse("tempad:item/tempad/charge_0"),
            Key.parse("tempad:item/tempad/charge_1"),
            Key.parse("tempad:item/tempad/charge_2"),
            Key.parse("tempad:item/tempad/charge_3"),
            Key.parse("tempad:item/tempad/charge_4")
    };

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    WorkstationItemEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    boolean emit(
            WorkstationItem item,
            String facing,
            BlockNeighborhood block,
            TileModelView target
    ) {
        if (!item.valid() || !item.present()) {
            return item.valid();
        }
        int facingDegrees = TempadRenderRules.workstationFacingDegrees(facing);
        Key base = item.attached() ? ATTACHED : BASE;
        Key charge = CHARGE[item.chargeStage()];
        if (facingDegrees < 0 || resourcePack.getTextures().get(base) == null
                || resourcePack.getTextures().get(charge) == null) {
            return false;
        }
        int start = target.getTileModel().size();
        emitLayer(base, 0.5000F, block, target);
        emitLayer(charge, 0.4985F, block, target);
        int count = target.getTileModel().size() - start;
        TileModel mesh = target.getTileModel();

        mesh.translate(start, count, -0.5F, -0.5F, -0.5F);
        mesh.scale(start, count, 0.5F, 0.5F, 0.5F);
        mesh.scale(start, count, 0.75F, 0.75F, 0.75F);
        mesh.rotate(start, count, -90F, 0F, 0F, 1F);
        mesh.translate(start, count, -1.5F / 16F, -1F / 16F, -2.5F / 16F);
        mesh.rotate(start, count, facingDegrees, 0F, 0F, 1F);
        mesh.rotate(start, count, 90F, 1F, 0F, 0F);
        mesh.translate(start, count, 0.5F, 0F, 0.5F);
        return true;
    }

    private void emitLayer(
            Key texture,
            float z,
            BlockNeighborhood block,
            TileModelView target
    ) {
        int start = target.add(4);
        TileModel mesh = target.getTileModel();
        setTriangle(mesh, start,
                0F, 0F, z, 1F, 0F, z, 1F, 1F, z,
                0F, 1F, 1F, 1F, 1F, 0F);
        setTriangle(mesh, start + 1,
                0F, 0F, z, 1F, 1F, z, 0F, 1F, z,
                0F, 1F, 1F, 0F, 0F, 0F);
        setTriangle(mesh, start + 2,
                1F, 1F, z, 1F, 0F, z, 0F, 0F, z,
                1F, 0F, 1F, 1F, 0F, 1F);
        setTriangle(mesh, start + 3,
                0F, 1F, z, 1F, 1F, z, 0F, 0F, z,
                0F, 0F, 1F, 0F, 0F, 1F);
        FaceLighting.Sample light = FaceLighting.sample(block, Direction.UP);
        int material = textures.get(texture);
        for (int index = start; index < start + 4; index++) {
            mesh.setMaterialIndex(index, material);
            mesh.setColor(index, 1F, 1F, 1F);
            mesh.setAOs(index, 1F, 1F, 1F);
            mesh.setSunlight(index, light.sunlight());
            mesh.setBlocklight(index, light.blocklight());
        }
    }

    private static void setTriangle(
            TileModel mesh,
            int index,
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float au, float av,
            float bu, float bv,
            float cu, float cv
    ) {
        mesh.setPositions(index, ax, ay, az, bx, by, bz, cx, cy, cz);
        mesh.setUvs(index, au, av, bu, bv, cu, cv);
    }

    static Key baseTexture(boolean attached) {
        return attached ? ATTACHED : BASE;
    }

    static Key chargeTexture(int stage) {
        return CHARGE[stage];
    }
}
