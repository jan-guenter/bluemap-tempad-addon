/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.tempad.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

/** Emits an installed block model while tinting only indexed source faces. */
final class TintedModelEmitter {

    private static final float BLOCK_SCALE = 1F / 16F;

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;
    private final MatrixM4f elementTransform = new MatrixM4f();

    TintedModelEmitter(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
    }

    boolean emit(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor,
            int tint
    ) {
        Model model = variant.getModel().getResource(resourcePack.getModels()::get);
        if (model == null || model.getElements() == null) {
            return false;
        }
        int modelStart = target.getTileModel().size();
        mapColor.set(0F, 0F, 0F, 0F, true);
        for (Element element : model.getElements()) {
            emitElement(block, target, mapColor, model, element, tint);
        }
        int count = target.getTileModel().size() - modelStart;
        if (count == 0) {
            return false;
        }
        if (variant.isTransformed()) {
            target.getTileModel().transform(
                    modelStart, count, variant.getTransformMatrix()
            );
        }
        if (mapColor.a > 0F) {
            mapColor.flatten().straight();
        }
        return true;
    }

    private void emitElement(
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor,
            Model model,
            Element element,
            int tint
    ) {
        Vector3f from = element.getFrom();
        Vector3f to = element.getTo();
        float x0 = from.getX();
        float y0 = from.getY();
        float z0 = from.getZ();
        float x1 = to.getX();
        float y1 = to.getY();
        float z1 = to.getZ();
        int start = target.getTileModel().size();
        emitFace(block, target, mapColor, model, element, Direction.DOWN,
                vertices(x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1), tint);
        emitFace(block, target, mapColor, model, element, Direction.UP,
                vertices(x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0), tint);
        emitFace(block, target, mapColor, model, element, Direction.NORTH,
                vertices(x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0), tint);
        emitFace(block, target, mapColor, model, element, Direction.SOUTH,
                vertices(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1), tint);
        emitFace(block, target, mapColor, model, element, Direction.WEST,
                vertices(x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0), tint);
        emitFace(block, target, mapColor, model, element, Direction.EAST,
                vertices(x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1), tint);
        int count = target.getTileModel().size() - start;
        if (count > 0) {
            elementTransform.copy(element.getRotation().getMatrix())
                    .scale(BLOCK_SCALE, BLOCK_SCALE, BLOCK_SCALE);
            target.getTileModel().transform(start, count, elementTransform);
        }
    }

    private void emitFace(
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor,
            Model model,
            Element element,
            Direction direction,
            Vertex[] vertices,
            int tint
    ) {
        Face face = element.getFaces().get(direction);
        if (face == null) {
            return;
        }
        FaceLighting.Sample light = FaceLighting.sample(block, direction);
        int visible = settings.isCaveDetectionUsesBlockLight()
                ? Math.max(light.sunlight(), light.blocklight()) : light.sunlight();
        if (block.isRemoveIfCave() && visible == 0) {
            return;
        }
        ResourcePath<Texture> texturePath = face.getTexture()
                .getTexturePath(model.getTextures()::get);
        Texture texture = texturePath == null
                ? null : texturePath.getResource(resourcePack.getTextures()::get);
        if (texture == null) {
            throw new IllegalStateException("installed marker texture is missing");
        }
        int start = target.add(2);
        TileModel mesh = target.getTileModel();
        setTriangle(mesh, start, vertices[0], vertices[1], vertices[2]);
        setTriangle(mesh, start + 1, vertices[0], vertices[2], vertices[3]);
        setUvs(mesh, start, face.getUv(), face.getRotation());
        int material = textures.get(texturePath);
        int color = face.getTintindex() >= 0 ? tint : 0xFFFFFF;
        float red = (color >>> 16 & 0xFF) / 255F;
        float green = (color >>> 8 & 0xFF) / 255F;
        float blue = (color & 0xFF) / 255F;
        for (int index = start; index < start + 2; index++) {
            mesh.setMaterialIndex(index, material);
            mesh.setColor(index, red, green, blue);
            mesh.setAOs(index, 1F, 1F, 1F);
            mesh.setSunlight(index, light.sunlight());
            mesh.setBlocklight(index, Math.max(
                    light.blocklight(), element.getLightEmission()
            ));
        }
        if (direction == Direction.UP) {
            Color sample = new Color().set(texture.getColorPremultiplied());
            if (face.getTintindex() >= 0) {
                sample.r *= red;
                sample.g *= green;
                sample.b *= blue;
            }
            mapColor.add(sample);
        }
    }

    private static void setUvs(
            TileModel mesh,
            int start,
            Vector4f raw,
            int rotation
    ) {
        float[][] corners = {
                {raw.getX() / 16F, raw.getW() / 16F},
                {raw.getZ() / 16F, raw.getW() / 16F},
                {raw.getZ() / 16F, raw.getY() / 16F},
                {raw.getX() / 16F, raw.getY() / 16F}
        };
        int step = Math.floorMod(Math.floorDiv(rotation, 90), 4);
        float[] first = corners[step];
        float[] second = corners[(step + 1) % 4];
        float[] third = corners[(step + 2) % 4];
        float[] fourth = corners[(step + 3) % 4];
        mesh.setUvs(start,
                first[0], first[1], second[0], second[1], third[0], third[1]);
        mesh.setUvs(start + 1,
                first[0], first[1], third[0], third[1], fourth[0], fourth[1]);
    }

    private static void setTriangle(
            TileModel mesh,
            int index,
            Vertex first,
            Vertex second,
            Vertex third
    ) {
        mesh.setPositions(index,
                first.x(), first.y(), first.z(),
                second.x(), second.y(), second.z(),
                third.x(), third.y(), third.z());
    }

    private static Vertex[] vertices(
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz
    ) {
        return new Vertex[]{
                new Vertex(ax, ay, az), new Vertex(bx, by, bz),
                new Vertex(cx, cy, cz), new Vertex(dx, dy, dz)
        };
    }

    private record Vertex(float x, float y, float z) {
    }
}
