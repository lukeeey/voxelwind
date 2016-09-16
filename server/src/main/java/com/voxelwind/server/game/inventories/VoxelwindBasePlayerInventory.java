package com.voxelwind.server.game.inventories;

import com.voxelwind.api.game.inventories.InventoryType;
import com.voxelwind.api.game.inventories.PlayerInventory;
import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.server.network.mcpe.packets.McpeMobEquipment;
import com.voxelwind.server.network.session.PlayerSession;

import java.util.Arrays;
import java.util.Optional;

public class VoxelwindBasePlayerInventory extends VoxelwindBaseInventory implements PlayerInventory {
    private final PlayerSession session;
    private final int[] hotbarLinks = new int[9];
    private int heldSlot = -1;

    public VoxelwindBasePlayerInventory(PlayerSession session) {
        // TODO: Verify
        super(InventoryType.PLAYER);
        this.session = session;
        getObserverList().add(session);
        Arrays.fill(hotbarLinks, -1);
    }

    @Override
    public int[] getHotbarLinks() {
        return Arrays.copyOf(hotbarLinks, hotbarLinks.length);
    }

    public void setLink(int hotbarSlot, int inventorySlot) {
        hotbarLinks[hotbarSlot] = inventorySlot;
    }

    @Override
    public int getHeldSlot() {
        return heldSlot;
    }

    @Override
    public Optional<ItemStack> getStackInHand() {
        int slot = heldSlot;
        if (slot == -1) {
            return Optional.empty();
        }
        int linked = hotbarLinks[slot];
        if (linked == -1) {
            return Optional.empty();
        }
        return getItem(linked);
    }

    public void setHeldSlot(int heldSlot, boolean sendToPlayer) {
        this.heldSlot = heldSlot;

        if (sendToPlayer) {
            McpeMobEquipment equipmentForSelf = new McpeMobEquipment();
            equipmentForSelf.setEntityId(0);
            equipmentForSelf.setHotbarSlot((byte) heldSlot);
            equipmentForSelf.setInventorySlot((byte) (hotbarLinks[heldSlot] + 9)); // Corrected for the benefit of MCPE
            equipmentForSelf.setStack(getStackInHand().orElse(null));
            session.getMcpeSession().addToSendQueue(equipmentForSelf);
        }

        McpeMobEquipment equipmentForAll = new McpeMobEquipment();
        equipmentForAll.setEntityId(session.getEntityId());
        equipmentForAll.setHotbarSlot((byte) heldSlot);
        equipmentForAll.setInventorySlot((byte) hotbarLinks[heldSlot]);
        equipmentForAll.setStack(getStackInHand().orElse(null));
        session.getLevel().getPacketManager().queuePacketForViewers(session, equipmentForAll);
    }
}
