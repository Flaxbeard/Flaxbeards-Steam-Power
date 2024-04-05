package eiteam.esteemedinnovation.api.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A simple utility class for interacting with Minecraft's inventories, containers, and slots.
 */
public final class InventoryUtility {

    /**
     * Player-specific overload for {@link #consumeInventoryItem(Container, Item)}
     */
    public static void consumeInventoryItem(Player player, Item item) {
        consumeInventoryItem(player.getInventory(), item);
    }

    /**
     * Removes 1 of the provided item from the provided inventory. It will deplete the stack size as handled by vanilla.
     * @param inventory The inventory to remove the item from
     * @param item The item to deplete
     */
    public static void consumeInventoryItem(Container inventory, Item item) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stackInSlot = inventory.getItem(slot);
            if (!stackInSlot.isEmpty() && stackInSlot.getItem() == item) {
                stackInSlot.shrink(1);
                inventory.setItem(slot, stackInSlot);
            }
        }
    }

    /**
     * @param player The player to check the hotbar of
     * @param item The item to search for
     * @return Whether the provided item is in the player's hotbar.
     */
    public static boolean hasItemInHotbar(Player player, Item item) {
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            ItemStack stackInSlot = player.getInventory().getItem(i);
            if (stackInSlot.getItem() == item) {
                return true;
            }
        }
        return false;
    }
}
