/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.tempad.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** BlueNBT projection of only the attachment and workstation inventory data. */
public final class TempadBlockEntityData extends MCABlockEntity {

    @NBTName("neoforge:attachments")
    private Object attachments;

    @NBTName("Inventory")
    private Object inventory;

    public TempadBlockEntityData() {
    }

    Object attachments() {
        return attachments;
    }

    Object inventory() {
        return inventory;
    }
}
